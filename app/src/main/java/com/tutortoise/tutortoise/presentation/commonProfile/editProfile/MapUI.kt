package com.tutortoise.tutortoise.presentation.commonProfile.editProfile

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MapUI(private val webView: WebView) {
    private var isInitialized = false
    private var isLoaded = false
    private var pendingLocation: LocationUpdate? = null

    private data class LocationUpdate(
        val latitude: Double,
        val longitude: Double,
        val locationText: String
    )

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize() {
        if (!isInitialized) {
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                setRenderPriority(WebSettings.RenderPriority.HIGH)
            }
            // Keep WebView GONE until we have a location
            webView.visibility = View.GONE
            webView.loadUrl("file:///android_asset/map.html")
            webView.webViewClient = createWebViewClient()
            isInitialized = true
        }
    }

    private fun createWebViewClient() = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            isLoaded = true
            pendingLocation?.let {
                updateLocation(it.latitude, it.longitude, it.locationText)
                pendingLocation = null
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, locationText: String) {
        if (isLoaded) {
            webView.visibility = View.VISIBLE // Only show when updating location
            webView.evaluateJavascript(
                """
                if (typeof updateMarker === 'function') {
                    updateMarker($latitude, $longitude, '$locationText');
                } else if (typeof initMap === 'function') {
                    initMap($latitude, $longitude, '$locationText');
                }
                """.trimIndent(),
                null
            )
        } else {
            pendingLocation = LocationUpdate(latitude, longitude, locationText)
        }
    }

    fun hide() {
        webView.visibility = View.GONE
        isLoaded = false
    }
}