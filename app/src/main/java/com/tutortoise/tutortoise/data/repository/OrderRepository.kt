package com.tutortoise.tutortoise.data.repository

import android.content.Context
import com.tutortoise.tutortoise.data.pref.ApiConfig

class OrderRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)
}