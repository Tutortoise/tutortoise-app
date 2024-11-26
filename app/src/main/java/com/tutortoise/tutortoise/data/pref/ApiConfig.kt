package com.tutortoise.tutortoise.data.pref

import android.content.Context
import com.tutortoise.tutortoise.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response

object ApiConfig {
    private val BASE_URL = BuildConfig.BASE_URL
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    private fun createOkHttpClient(context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Synchronized
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit?.create(ApiService::class.java)
        }
        return apiService!!
    }

    fun parseError(response: Response<*>): ErrorResponse? {
        return try {
            val converter = retrofit?.responseBodyConverter<ErrorResponse>(
                ErrorResponse::class.java,
                arrayOfNulls(0)
            )
            converter?.convert(response.errorBody()!!)
        } catch (e: Exception) {
            null
        }
    }
}