package hansffu.ontime.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface StopsApi {
    @Headers("ET-Client-Name: hansffu - ontime")
    @GET("/geocoder/v1/reverse")
    fun getNearbyStops(
            @Query("point.lat") latitude: Double,
            @Query("point.lon") longitude: Double,
            @Query("boundary.circle.radius") radius: Int,
            @Query("size") size: Int,
            @Query("layers") layers: String
    ): Single<Geocoding>
}

