package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("registered_at") val registeredAt: String
)
