package hansffu.ontime.service

import android.location.Location
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.fix
import arrow.integrations.retrofit.adapter.runAsync
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import hansffu.ontime.StopPlaceQuery
import hansffu.ontime.api.Feature
import hansffu.ontime.api.Properties
import hansffu.ontime.api.StopsApi
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

private val TAG = "StopService"


class StopService {
//    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
//            .apply { level = HttpLoggingInterceptor.Level.BODY }
//    private val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

    private val retrofit: Retrofit = Retrofit.Builder()
//            .client(client)
            .baseUrl("https://api.entur.io")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private val stopsApi: StopsApi = retrofit.create(StopsApi::class.java)

    private val apolloClient = ApolloClient.builder()
            .serverUrl("https://api.entur.io/journey-planner/v2/graphql")
            .build()


    fun findStopsNear(location: Location): IO<List<Properties>> = stopsApi
            .getNearbyStops(location.latitude, location.longitude, 2, 20, "venue")
            .runAsync(IO.async()).fix()
            .map { it.body()!!.features.map(Feature::properties) }

    fun getDepartures(id: String): Observable<StopPlaceQuery.Data> {
        val watcher = apolloClient.query(
                StopPlaceQuery.builder()
                        .id(id)
                        .build())
        return Rx2Apollo.from(watcher)
                .subscribeOn(Schedulers.io())
                .map { it.data() }
                .filterNotNull()
                .filter(Objects::nonNull)
    }

}

fun <T> Observable<T?>.filterNotNull(): Observable<T> = filter { it != null }.map { it!! }
