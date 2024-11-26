package com.tutortoise.tutortoise.data.pref

import android.content.Context
import com.tutortoise.tutortoise.data.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authRepository = AuthRepository(context)
        val token = authRepository.getToken()

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}