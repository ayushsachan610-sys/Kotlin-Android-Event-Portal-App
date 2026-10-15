package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("fee") val fee: Int = 0,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("available_seats") val availableSeats: Int
) {
    val isFull: Boolean
        get() = availableSeats <= 0

    val feeDisplay: String
        get() = if (fee > 0) "₹$fee" else "Free"

    val registeredCount: Int
        get() = maxOf(0, capacity - availableSeats)

    val utilizationPct: Float
        get() = if (capacity > 0) ((registeredCount.toFloat() / capacity.toFloat()) * 100f) else 0f
}
