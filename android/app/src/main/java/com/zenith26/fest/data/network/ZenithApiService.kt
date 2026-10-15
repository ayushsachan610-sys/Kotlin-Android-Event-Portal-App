package com.zenith26.fest.data.network

import com.zenith26.fest.data.model.CreateEventRequest
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.Registration
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.model.RegistrationResponse
import com.zenith26.fest.data.model.StatsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ZenithApiService {

    /**
     * GET /api/events
     * Fetch list of events with live computed available_seats.
     * Supports optional category filter and title search query.
     */
    @GET("api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<Event>>

    /**
     * GET /api/events/{event_id}
     * Fetch single event details by its ID.
     */
    @GET("api/events/{event_id}")
    suspend fun getEventDetail(
        @Path("event_id") eventId: Int
    ): Response<Event>

    /**
     * POST /api/events/{event_id}/register
     * Register a student for an event with strict server-side validation.
     */
    @POST("api/events/{event_id}/register")
    suspend fun registerForEvent(
        @Path("event_id") eventId: Int,
        @Body payload: RegistrationRequest
    ): Response<RegistrationResponse>

    /**
     * GET /api/registrations
     * Fetch all registrations with event titles.
     * Supports optional filtering by event_id.
     */
    @GET("api/registrations")
    suspend fun getRegistrations(
        @Query("event_id") eventId: Int? = null
    ): Response<List<Registration>>

    /**
     * GET /api/stats
     * Fetch live aggregated operational metrics.
     */
    @GET("api/stats")
    suspend fun getStats(): Response<StatsResponse>

    /**
     * POST /api/events
     * Admin create new fest event.
     */
    @POST("api/events")
    suspend fun createEvent(
        @Body payload: CreateEventRequest
    ): Response<Event>

    /**
     * DELETE /api/registrations/{registration_id}
     * Admin cancel a registration and restore seat.
     */
    @DELETE("api/registrations/{registration_id}")
    suspend fun cancelRegistration(
        @Path("registration_id") registrationId: Int
    ): Response<Map<String, Any>>
}
