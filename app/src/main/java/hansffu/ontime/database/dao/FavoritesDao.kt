package hansffu.ontime.database.dao

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

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