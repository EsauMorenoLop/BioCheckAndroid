package net.biocheck.biocheckmovil.service

import com.google.gson.GsonBuilder
import net.biocheck.biocheckmovil.Preferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Time
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Created by Gabriel Martinez on 09/01/2018.
 */
class ApiServiceGenerator {
    companion object {

        private var gson = GsonBuilder()
                //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .setDateFormat("yyyy-MM-dd@HH:mm:ss.SSSZ")
                .setLenient()
                .registerTypeAdapter(Time::class.java, TimeAdapter())
                .create()

//        var okHttpClient = OkHttpClient.Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .writeTimeout(5, TimeUnit.SECONDS)
//                .readTimeout(10, TimeUnit.SECONDS)
//                .build()
        private var okHttpClient = HttpsTrust.getUnsafeOkHttpClient()

        var builder = Retrofit.Builder()
                .baseUrl(Preferences.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
        var retrofit = builder.build()

        fun <T> createService(serviceClass: Class<T>): T {
            return retrofit.create(serviceClass)
        }
    }
}