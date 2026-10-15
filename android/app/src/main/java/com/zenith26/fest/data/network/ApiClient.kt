package com.zenith26.fest.data.network

import android.content.Context
import com.google.gson.Gson
import com.zenith26.fest.data.model.ApiErrorResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var apiService: ZenithApiService? = null
    private val gson = Gson()

    fun getApiService(context: Context): ZenithApiService {
        if (apiService == null) {
            val baseUrl = ApiConfig.getBaseUrl(context)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            apiService = retrofit!!.create(ZenithApiService::class.java)
        }
        return apiService!!
    }

    fun resetClient() {
        retrofit = null
        apiService = null
    }

    /**
     * Parses the errorBody from a Retrofit Response and extracts the FastAPI 'detail' string.
     */
    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBodyString = response.errorBody()?.string()
            if (!errorBodyString.isNullOrEmpty()) {
                val errorResponse = gson.fromJson(errorBodyString, ApiErrorResponse::class.java)
                errorResponse.getErrorMessage()
            } else {
                "HTTP ${response.code()}: ${response.message()}"
            }
        } catch (e: Exception) {
            "HTTP ${response.code()}: ${response.message()}"
        }
    }
}
