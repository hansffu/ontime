package dev.hansffu.ontime.service

import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.model.BoardTimetableState
import dev.hansffu.ontime.model.Coordinates
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/** Shared by the foreground service and screens; screen lifetime never owns active-board polling. */
@Singleton
class BoardTimetableRepository internal constructor(
    private val source: BoardTimetableSource,
    private val elapsedMillis: () -> Long,
) {
    @Inject constructor(source: DefaultBoardTimetableSource) :
        this(source, { android.os.SystemClock.elapsedRealtime() })

    private class Cache {
        val state = MutableStateFlow<BoardTimetableState>(BoardTimetableState.Loading)
        val mutex = Mutex()
        var departures: List<BoardDeparture>? = null
        var lastAttempt: Long? = null
        var lastLocationAttempt: Long? = null
        var location: Coordinates? = null
    }

    private enum class Refresh { INITIAL, SCHEDULED, MANUAL, RESUME }
    private val caches = ConcurrentHashMap<Long, Cache>()
    private fun cache(boardId: Long) = caches.getOrPut(boardId, ::Cache)

    fun cachedState(boardId: Long): BoardTimetableState = cache(boardId).state.value

    fun observe(boardId: Long): Flow<BoardTimetableState> =
        combine(cache(boardId).state, source.observeInputs(boardId)) { state, inputs ->
            when (state) {
                is BoardTimetableState.Content -> state.copy(
                    boardName = inputs.board?.name ?: state.boardName,
                    rows = state.rows.filter { it.stored in inputs.departures }.map { row ->
                        row.copy(isFavorite = inputs.favorites.any {
                            it.stopId == row.stored.stopId &&
                                it.lineRef == row.stored.lineRef &&
                                it.destinationRef == row.stored.destinationRef
                        })
                    },
                )
                is BoardTimetableState.Error -> state.copy(
                    boardName = inputs.board?.name ?: state.boardName,
                )
                BoardTimetableState.Loading -> state
            }
        }.distinctUntilChanged()

    suspend fun openBoard(boardId: Long) {
        val active = source.getInputs(boardId).board?.active == true
        reload(boardId, if (active) Refresh.INITIAL else Refresh.RESUME)
    }

    suspend fun refreshOnResume(boardId: Long) {
        if (source.getInputs(boardId).board?.active != true) {
            reload(boardId, Refresh.RESUME)
        }
    }

    suspend fun refresh(boardId: Long) = reload(boardId, Refresh.MANUAL)

    // React to changed board membership, not renames, activation, or favorite toggles.
    suspend fun watchConfiguration(boardId: Long, allowLocation: () -> Boolean = { true }) {
        source.observeInputs(boardId)
            .map { it.board?.id to it.departures }
            .distinctUntilChanged()
            .collect { reload(boardId, Refresh.INITIAL, allowLocation()) }
    }

    suspend fun runActiveBoard(boardId: Long, allowLocation: () -> Boolean = { true }) = coroutineScope {
        launch { watchConfiguration(boardId, allowLocation) }
        while (isActive) {
            reload(boardId, Refresh.SCHEDULED, allowLocation())
            val nextRefresh = cache(boardId).mutex.withLock {
                (cache(boardId).lastAttempt ?: elapsedMillis()) + DEPARTURE_INTERVAL_MILLIS
            }
            // Measure start-to-start so network latency does not accumulate between ticks.
            delay((nextRefresh - elapsedMillis()).coerceAtLeast(1L))
        }
    }

    private suspend fun reload(boardId: Long, refresh: Refresh, allowLocation: Boolean = true) {
        val cache = cache(boardId)
        cache.mutex.withLock {
            val inputs = source.getInputs(boardId)
            val board = inputs.board ?: return
            val now = elapsedMillis()
            val unchanged = cache.departures == inputs.departures && cache.lastAttempt != null
            if (unchanged && refresh == Refresh.INITIAL) return
            if (unchanged && refresh == Refresh.SCHEDULED &&
                now - checkNotNull(cache.lastAttempt) < DEPARTURE_INTERVAL_MILLIS) return

            val previousState = cache.state.value
            val previousContent = previousState as? BoardTimetableState.Content
            val previousAttempt = cache.lastAttempt
            val previousDepartures = cache.departures
            cache.lastAttempt = now
            cache.departures = inputs.departures
            if (refresh == Refresh.MANUAL) {
                cache.state.value = previousContent?.copy(refreshing = true, refreshFailed = false)
                    ?: BoardTimetableState.Loading
            }
            try {
                if (allowLocation && (cache.lastLocationAttempt == null ||
                        now - checkNotNull(cache.lastLocationAttempt) >= LOCATION_INTERVAL_MILLIS)) {
                    cache.lastLocationAttempt = now
                    val location = try {
                        withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { source.getLocation() }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        null
                    }
                    if (location != null) cache.location = location
                }
                val rows = withTimeoutOrNull(DEPARTURE_TIMEOUT_MILLIS) {
                    source.getRows(inputs, cache.location)
                } ?: throw IOException("Board refresh timed out")
                cache.state.value = BoardTimetableState.Content(board.name, rows)
            } catch (cancellation: CancellationException) {
                cache.state.value = previousState
                cache.lastAttempt = previousAttempt
                cache.departures = previousDepartures
                throw cancellation
            } catch (_: Exception) {
                cache.state.value = previousContent?.copy(refreshing = false, refreshFailed = true)
                    ?: BoardTimetableState.Error(board.name)
            }
        }
    }

    companion object {
        internal const val DEPARTURE_INTERVAL_MILLIS = 60_000L
        internal const val LOCATION_INTERVAL_MILLIS = 120_000L
        private const val LOCATION_TIMEOUT_MILLIS = 15_000L
        private const val DEPARTURE_TIMEOUT_MILLIS = 40_000L
    }
}
