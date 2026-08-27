package dev.hansffu.ontime.database.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(board: Board): Long

    @Update
    suspend fun update(board: Board)

    @Query("DELETE FROM Board WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Entity
data class Board(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val activationStopId: String? = null,
    val activationStopName: String? = null,
    val activationLatitude: Double? = null,
    val activationLongitude: Double? = null,
    val maxDistanceMeters: Int? = null,
    val startMinuteOfDay: Int? = null,
    val endMinuteOfDay: Int? = null,
)
