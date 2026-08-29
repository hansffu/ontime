package dev.hansffu.ontime.database.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Query("SELECT * FROM Board ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<Board>>

    @Query("SELECT * FROM Board WHERE id = :id")
    fun observeById(id: Long): Flow<Board?>

    @Query("SELECT * FROM Board WHERE id = :id")
    suspend fun getById(id: Long): Board?

    @Query("SELECT * FROM Board WHERE active = 1 LIMIT 1")
    fun observeActive(): Flow<Board?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(board: Board): Long

    @Update
    suspend fun update(board: Board)

    @Query("DELETE FROM Board WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun activate(id: Long) {
        clearActive()
        setActive(id)
    }

    @Query("UPDATE Board SET active = 0")
    suspend fun clearActive()

    @Query("UPDATE Board SET active = 1 WHERE id = :id")
    suspend fun setActive(id: Long)

    @Query("UPDATE Board SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}

@Entity
data class Board(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val activationStopId: String? = null,
    val activationStopName: String? = null,
    val activationLatitude: Double? = null,
    val activationLongitude: Double? = null,
    val maxDistanceMeters: Int? = 3_000,
    val startMinuteOfDay: Int? = 6 * 60,
    val endMinuteOfDay: Int? = 9 * 60,
    val distanceEnabled: Boolean = false,
    val timeEnabled: Boolean = false,
    val active: Boolean = false,
)
