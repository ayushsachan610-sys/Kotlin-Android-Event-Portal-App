package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("detail") val detail: Any? = null
) {
    fun getErrorMessage(): String {
        return when (detail) {
            is String -> detail
            is List<*> -> detail.joinToString("\n") { it.toString() }
            null -> "Unknown server error"
            else -> detail.toString()
        }
    }
}
