package hansffu.ontime.api

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface StopsApi {
    @Headers("ET-Client-Name: hansffu - ontime")
    @GET("/geocoder/v1/reverse")
    suspend fun getNearbyStops(
            @Query("point.lat") latitude: Double,
            @Query("point.lon") longitude: Double,
            @Query("boundary.circle.radius") radius: Int,
            @Query("size") size: Int,
            @Query("layers") layers: String,
            @Query("multiModal") multiModal: String
    ): Geocoding
}

