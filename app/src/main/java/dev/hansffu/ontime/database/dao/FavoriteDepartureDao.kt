package dev.hansffu.ontime.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDepartureDao {
    @Query("SELECT * FROM FavoriteDeparture")
    fun observeAll(): Flow<List<FavoriteDeparture>>

    @Query("SELECT * FROM FavoriteDeparture")
    suspend fun getAll(): List<FavoriteDeparture>

    @Query("SELECT * FROM FavoriteDeparture WHERE stopId = :stopId")
    fun getByStopId(stopId: String): Flow<List<FavoriteDeparture>>

    @Query("SELECT * FROM FavoriteDeparture WHERE lineRef = :lineRef AND destinationRef = :destinationRef AND stopId = :stopId")
    fun getById(lineRef: String, destinationRef: String, stopId: String): FavoriteDeparture?

    @Insert
    fun insertAll(vararg favorites: FavoriteDeparture)

    @Delete
    fun delete(favorite: FavoriteDeparture)
}

@Entity(primaryKeys = ["lineRef", "destinationRef", "stopId"])
data class FavoriteDeparture(
    val lineRef: String,
    val destinationRef: String,
    val stopId: String,
)
