package dev.hansffu.ontime.database.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDepartureDao {
    @Query("SELECT * FROM BoardDeparture WHERE boardId = :boardId ORDER BY stopName, lineRef, destinationRef")
    fun observeForBoard(boardId: Long): Flow<List<BoardDeparture>>

    @Query("SELECT * FROM BoardDeparture WHERE boardId = :boardId ORDER BY stopName, lineRef, destinationRef")
    suspend fun getForBoard(boardId: Long): List<BoardDeparture>

    @Query(
        "SELECT boardId FROM BoardDeparture " +
            "WHERE stopId = :stopId AND lineRef = :lineRef AND destinationRef = :destinationRef"
    )
    fun observeBoardIdsForDeparture(
        stopId: String,
        lineRef: String,
        destinationRef: String,
    ): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(departure: BoardDeparture)

    @Query(
        "DELETE FROM BoardDeparture WHERE boardId = :boardId AND stopId = :stopId " +
            "AND lineRef = :lineRef AND destinationRef = :destinationRef"
    )
    suspend fun delete(
        boardId: Long,
        stopId: String,
        lineRef: String,
        destinationRef: String,
    )

    @Query("DELETE FROM BoardDeparture WHERE boardId = :boardId")
    suspend fun deleteForBoard(boardId: Long)
}

@Entity(primaryKeys = ["boardId", "stopId", "lineRef", "destinationRef"])
data class BoardDeparture(
    val boardId: Long,
    val stopId: String,
    val stopName: String,
    val stopLatitude: Double?,
    val stopLongitude: Double?,
    val lineRef: String,
    val destinationRef: String,
)
