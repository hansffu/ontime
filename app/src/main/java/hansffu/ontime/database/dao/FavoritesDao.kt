package hansffu.ontime.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavoritesDao {
    @Query("select * from FavoriteStop")
    fun getAll(): LiveData<List<FavoriteStop>>

    @Query("SELECT * FROM FavoriteStop WHERE STOP_ID = :id")
    fun getById(id: String): FavoriteStop?

    @Insert
    fun insertAll(vararg favorites: FavoriteStop)

    @Delete
    fun delete(favorite: FavoriteStop)
}

@Entity
data class FavoriteStop(
    @PrimaryKey @ColumnInfo(name = "STOP_ID") val id: String,
    @ColumnInfo(name = "STOP_NAME") val name: String,
)