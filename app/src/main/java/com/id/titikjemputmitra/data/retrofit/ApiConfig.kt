package com.id.titikjemputmitra.data.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.id.titikjemputmitra.BuildConfig
import com.id.titikjemputmitra.data.UserPreference
import okhttp3.Interceptor
import android.content.Context

class ApiConfig  {

    companion object{
        fun getApiService(context: Context) : ApiService {
            val loggingInterceptor  = HttpLoggingInterceptor().apply{
                level = if (BuildConfig.DEBUG){
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            val authInterceptor = Interceptor { chain ->
                val req = chain.request()
                val pref = UserPreference(context)
                val token = pref.getUser().apikey // Mengambil token yang tersimpan

                // Tambahkan header Authorization jika token tersedia
                val requestHeaders = req.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(requestHeaders)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}