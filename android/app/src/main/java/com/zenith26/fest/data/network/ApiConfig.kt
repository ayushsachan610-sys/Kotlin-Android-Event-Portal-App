package com.zenith26.fest.data.network

import android.content.Context
import android.content.SharedPreferences

object ApiConfig {
    private const val PREFS_NAME = "zenith26_api_prefs"
    private const val KEY_BASE_URL = "api_base_url"

    // Default for Android Emulator to communicate with host machine (Port 8021)
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8021/"
    const val DEFAULT_LOCALHOST_URL = "http://127.0.0.1:8021/"
    const val DEFAULT_LAN_URL = "http://172.16.53.144:8021/"
    const val ALT_PORT_8000_URL = "http://10.0.2.2:8000/"
    const val ALT_PORT_8082_URL = "http://10.0.2.2:8082/"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getBaseUrl(context: Context): String {
        val saved = getPrefs(context).getString(KEY_BASE_URL, null)
        var url = saved ?: DEFAULT_EMULATOR_URL
        if (!url.endsWith("/")) {
            url += "/"
        }
        return url
    }

    fun setBaseUrl(context: Context, newUrl: String) {
        var cleanUrl = newUrl.trim()
        if (!cleanUrl.endsWith("/")) {
            cleanUrl += "/"
        }
        getPrefs(context).edit().putString(KEY_BASE_URL, cleanUrl).apply()
        ApiClient.resetClient()
    }
}
