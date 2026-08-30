package dev.hansffu.ontime.service

import dev.hansffu.ontime.database.dao.Board
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.model.BoardDepartureRow
import dev.hansffu.ontime.model.BoardTimetableState
import dev.hansffu.ontime.model.Coordinates
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardTimetableRepositoryTest {
    @Test fun serviceRefreshesWithoutAnyScreenOrStateCollector() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        runCurrent()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(listOf(0L, 60_000L), source.requests.map { it.second })
        assertEquals(listOf(0L), source.locations)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(listOf(0L, 60_000L, 120_000L), source.requests.map { it.second })
        assertEquals(listOf(0L, 120_000L), source.locations)
    }

    @Test fun networkLatencyDoesNotAccumulateInTheSchedule() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        source.fetch = { delay(5_000) }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        advanceTimeBy(120_001)
        runCurrent()
        assertEquals(listOf(0L, 60_000L, 120_000L), source.requests.map { it.second })
    }

    @Test fun openingAndResumingActiveBoardUseCacheWithoutFetching() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        runCurrent()
        val cached = repository.cachedState(1)
        advanceTimeBy(30_000)
        repeat(3) {
            repository.openBoard(1)
            repository.refreshOnResume(1)
            assertEquals(cached, repository.observe(1).first())
        }
        assertEquals(1, source.requests.size)
    }

    @Test fun scheduledLoadingKeepsContentAndNeverShowsSpinner() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        runCurrent()
        val before = repository.cachedState(1)
        val release = CompletableDeferred<Unit>()
        source.fetch = { release.await() }
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(2, source.requests.size)
        assertEquals(before, repository.cachedState(1))
        assertFalse((repository.cachedState(1) as BoardTimetableState.Content).refreshing)
        release.complete(Unit)
        runCurrent()
        assertFalse((repository.cachedState(1) as BoardTimetableState.Content).refreshing)
    }

    @Test fun manualRefreshStillShowsSpinnerAndPreservesRows() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        repository.openBoard(1)
        val before = repository.cachedState(1) as BoardTimetableState.Content
        val release = CompletableDeferred<Unit>()
        source.fetch = { release.await() }
        val job = launch { repository.refresh(1) }
        runCurrent()
        val during = repository.cachedState(1) as BoardTimetableState.Content
        assertTrue(during.refreshing)
        assertEquals(before.rows, during.rows)
        release.complete(Unit)
        job.join()
        assertFalse((repository.cachedState(1) as BoardTimetableState.Content).refreshing)
    }

    @Test fun failureKeepsCachedRowsAndRetriesOnNextTick() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        runCurrent()
        val before = repository.cachedState(1) as BoardTimetableState.Content
        source.fetch = { throw IOException("Offline") }
        advanceTimeBy(60_000)
        runCurrent()
        val failed = repository.cachedState(1) as BoardTimetableState.Content
        assertEquals(before.rows, failed.rows)
        assertTrue(failed.refreshFailed)
        assertFalse(failed.refreshing)
        repository.openBoard(1)
        assertEquals(2, source.requests.size)
        source.fetch = {}
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(3, source.requests.size)
        assertFalse((repository.cachedState(1) as BoardTimetableState.Content).refreshFailed)
    }

    @Test fun unavailableLocationIsAttemptedEveryTwoMinutesNotEveryTick() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        source.location = null
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        advanceTimeBy(120_001)
        runCurrent()
        assertEquals(3, source.requests.size)
        assertEquals(listOf(0L, 120_000L), source.locations)
    }

    @Test fun departuresContinueWithoutBackgroundLocationAccess() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) { false } }
        advanceTimeBy(120_001)
        runCurrent()
        assertEquals(3, source.requests.size)
        assertTrue(source.locations.isEmpty())
    }

    @Test fun favoritesAndRenameUpdateLocallyWithoutFetching() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        runCurrent()
        source.update(1) { copy(
            board = board?.copy(name = "Renamed"),
            favorites = listOf(FavoriteDeparture("20", "Skoyen", "stop-1")),
        ) }
        runCurrent()
        val content = repository.observe(1).first() as BoardTimetableState.Content
        assertEquals("Renamed", content.boardName)
        assertTrue(content.rows.single().isFavorite)
        assertEquals(1, source.requests.size)
    }

    @Test fun membershipChangesRefreshEvenWithoutAScreen() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        backgroundScope.launch { repository.runActiveBoard(1) }
        runCurrent()
        source.update(1) { copy(departures = emptyList()) }
        runCurrent()
        assertEquals(2, source.requests.size)
        assertTrue((repository.cachedState(1) as BoardTimetableState.Content).rows.isEmpty())
    }

    @Test fun switchingAndStoppingCancelOldPolling() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        val activeBoard = MutableStateFlow<Long?>(1L)
        backgroundScope.launch {
            activeBoard.collectLatest { id -> if (id != null) repository.runActiveBoard(id) }
        }
        runCurrent()
        advanceTimeBy(30_000)
        activeBoard.value = 2
        runCurrent()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(listOf(1L, 2L, 2L), source.requests.map { it.first })
        activeBoard.value = null
        runCurrent()
        advanceTimeBy(180_000)
        runCurrent()
        assertEquals(3, source.requests.size)
    }

    @Test fun cancellationDoesNotLeaveSpinnerOrPoisonInitialLoad() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        val release = CompletableDeferred<Unit>()
        source.fetch = { release.await() }
        val opening = launch { repository.openBoard(1) }
        runCurrent()
        opening.cancelAndJoin()
        source.fetch = {}
        repository.openBoard(1)
        assertEquals(2, source.requests.size)
        assertFalse((repository.cachedState(1) as BoardTimetableState.Content).refreshing)
    }

    @Test fun inactiveBoardStillRefreshesOnResumeWithoutSpinner() = runTest {
        val source = FakeSource { testScheduler.currentTime }
        source.update(1) { copy(board = board?.copy(active = false)) }
        val repository = BoardTimetableRepository(source) { testScheduler.currentTime }
        repository.openBoard(1)
        val before = repository.cachedState(1)
        val release = CompletableDeferred<Unit>()
        source.fetch = { release.await() }
        val resume = launch { repository.refreshOnResume(1) }
        runCurrent()
        assertEquals(2, source.requests.size)
        assertEquals(before, repository.cachedState(1))
        release.complete(Unit)
        resume.join()
    }

    private class FakeSource(private val clock: () -> Long) : BoardTimetableSource {
        private val inputs = MutableStateFlow((1L..2L).associateWith { id ->
            BoardTimetableInputs(
                Board(id = id, name = "Board $id", active = true),
                listOf(BoardDeparture(id, "stop-$id", "Stop $id", null, null, "20", "Skoyen")),
                emptyList(),
            )
        })
        val requests = mutableListOf<Pair<Long, Long>>()
        val locations = mutableListOf<Long>()
        var location: Coordinates? = Coordinates(59.9, 10.8)
        var fetch: suspend () -> Unit = {}

        fun update(id: Long, transform: BoardTimetableInputs.() -> BoardTimetableInputs) {
            inputs.value = inputs.value + (id to transform(inputs.value.getValue(id)))
        }

        override fun observeInputs(boardId: Long) = inputs.map { it.getValue(boardId) }
        override suspend fun getInputs(boardId: Long) = inputs.value.getValue(boardId)
        override suspend fun getLocation(): Coordinates? {
            locations += clock()
            return location
        }
        override suspend fun getRows(
            inputs: BoardTimetableInputs,
            location: Coordinates?,
        ): List<BoardDepartureRow> {
            requests += checkNotNull(inputs.board).id to clock()
            fetch()
            return inputs.departures.map {
                BoardDepartureRow(it, LineDeparture(LineDirectionRef(it.lineRef, it.destinationRef),
                    emptyList(), "000000"), null, false)
            }
        }
    }
}
