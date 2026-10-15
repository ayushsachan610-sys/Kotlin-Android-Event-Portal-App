package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class Registration(
    @SerializedName("id") val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("event_title") val eventTitle: String,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("roll_number") val rollNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("registered_at") val registeredAt: String
)
