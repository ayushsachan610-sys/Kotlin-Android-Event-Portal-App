package com.zenith26.fest.ui.student

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zenith26.fest.R
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.network.ApiClient
import com.zenith26.fest.databinding.BottomSheetEventDetailBinding
import com.zenith26.fest.utils.CategoryHelper
import kotlinx.coroutines.launch

class EventDetailBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEventDetailBinding? = null
    private val binding get() = _binding!!

    private var currentEvent: Event? = null
    var onRegistrationSuccess: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = arguments?.getInt(ARG_EVENT_ID) ?: return

        binding.btnModalClose.setOnClickListener {
            dismiss()
        }

        loadEventDetails(eventId)
        setupFormSubmission()
    }

    private fun loadEventDetails(eventId: Int) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getApiService(requireContext())
                val response = api.getEventDetail(eventId)
                if (response.isSuccessful && response.body() != null) {
                    val event = response.body()!!
                    currentEvent = event
                    populateEventUI(event)
                } else {
                    showModalAlert("Could not load event details: ${response.message()}", isSuccess = false)
                }
            } catch (e: Exception) {
                showModalAlert("Network error: Could not connect to backend server.", isSuccess = false)
            }
        }
    }

    private fun populateEventUI(event: Event) {
        val context = requireContext()

        binding.tvModalIcon.text = event.icon ?: "⚡"
        binding.tvModalTitle.text = event.title
        CategoryHelper.applyCategoryBadge(binding.tvModalCategory, event.category)

        if (event.fee > 0) {
            binding.tvModalFee.text = "Fee: ₹${event.fee}"
            binding.tvModalFee.setTextColor(ContextCompat.getColor(context, R.color.amber_400))
        } else {
            binding.tvModalFee.text = "Free Entry"
            binding.tvModalFee.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
        }

        binding.tvModalDatetime.text = "${event.eventDate} at ${event.eventTime}"
        binding.tvModalVenue.text = event.venue
        binding.tvModalDescription.text = event.description ?: "No detailed description provided."

        // Seat status
        if (event.isFull) {
            binding.tvModalSeats.text = "FULL (0 seats available)"
            binding.tvModalSeats.setTextColor(ContextCompat.getColor(context, R.color.rose_400))
            binding.btnRegSubmit.isEnabled = false
            binding.btnRegSubmit.alpha = 0.5f
            binding.btnRegSubmit.text = getString(R.string.btn_registration_closed)
        } else {
            binding.tvModalSeats.text = "${event.availableSeats} seats available (out of ${event.capacity})"
            binding.tvModalSeats.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
            binding.btnRegSubmit.isEnabled = true
            binding.btnRegSubmit.alpha = 1.0f
            binding.btnRegSubmit.text = getString(R.string.btn_confirm_registration)
        }
    }

    private fun setupFormSubmission() {
        binding.btnRegSubmit.setOnClickListener {
            val event = currentEvent ?: return@setOnClickListener

            val name = binding.etRegName.text.toString().trim()
            val roll = binding.etRegRoll.text.toString().trim()
            val phone = binding.etRegPhone.text.toString().trim().ifEmpty { null }
            val email = binding.etRegEmail.text.toString().trim()

            // Hide previous alerts
            binding.layoutModalAlert.visibility = View.GONE

            // Client-Side Validation (Matching Web app.js)
            if (name.isEmpty() || roll.isEmpty() || email.isEmpty()) {
                showModalAlert(
                    "Please fill in all required fields (Name, Roll Number, and Email).",
                    isSuccess = false
                )
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                showModalAlert(
                    "Please provide a valid email containing '@'.",
                    isSuccess = false
                )
                return@setOnClickListener
            }

            // Submitting state
            binding.btnRegSubmit.isEnabled = false
            binding.btnRegSubmit.text = getString(R.string.btn_submitting)

            lifecycleScope.launch {
                try {
                    val api = ApiClient.getApiService(requireContext())
                    val payload = RegistrationRequest(
                        studentName = name,
                        rollNumber = roll,
                        email = email,
                        phone = phone
                    )

                    val response = api.registerForEvent(event.id, payload)

                    if (response.isSuccessful && response.body() != null) {
                        val regResponse = response.body()!!
                        showModalAlert(
                            "🎉 Successfully registered for ${event.title}! Registration ID: #${regResponse.id}",
                            isSuccess = true
                        )

                        // Clear inputs
                        binding.etRegName.text?.clear()
                        binding.etRegRoll.text?.clear()
                        binding.etRegPhone.text?.clear()
                        binding.etRegEmail.text?.clear()

                        // Trigger refresh in background
                        onRegistrationSuccess?.invoke()
                        loadEventDetails(event.id)

                        // Auto-dismiss after 1.8 seconds matching web experience
                        Handler(Looper.getMainLooper()).postDelayed({
                            dismiss()
                        }, 1800)

                    } else {
                        // Server-side validation error (400, 404, 409)
                        val errorMsg = ApiClient.parseErrorMessage(response)
                        showModalAlert(errorMsg, isSuccess = false)
                        binding.btnRegSubmit.isEnabled = true
                        binding.btnRegSubmit.text = getString(R.string.btn_confirm_registration)
                    }
                } catch (e: Exception) {
                    showModalAlert("Network error: Could not connect to backend server.", isSuccess = false)
                    binding.btnRegSubmit.isEnabled = true
                    binding.btnRegSubmit.text = getString(R.string.btn_confirm_registration)
                }
            }
        }
    }

    private fun showModalAlert(message: String, isSuccess: Boolean) {
        val context = context ?: return
        binding.layoutModalAlert.visibility = View.VISIBLE
        binding.tvModalAlertMessage.text = message

        if (isSuccess) {
            binding.layoutModalAlert.setBackgroundResource(R.drawable.bg_alert_success)
            binding.tvModalAlertMessage.setTextColor(ContextCompat.getColor(context, R.color.emerald_300))
            binding.ivAlertIcon.setImageResource(R.drawable.ic_check_circle)
            binding.ivAlertIcon.setColorFilter(ContextCompat.getColor(context, R.color.emerald_400))
        } else {
            binding.layoutModalAlert.setBackgroundResource(R.drawable.bg_alert_error)
            binding.tvModalAlertMessage.setTextColor(ContextCompat.getColor(context, R.color.rose_300))
            binding.ivAlertIcon.setImageResource(R.drawable.ic_error)
            binding.ivAlertIcon.setColorFilter(ContextCompat.getColor(context, R.color.rose_400))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EventDetailBottomSheet"
        private const val ARG_EVENT_ID = "arg_event_id"

        fun newInstance(eventId: Int): EventDetailBottomSheetDialogFragment {
            return EventDetailBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EVENT_ID, eventId)
                }
            }
        }
    }
}
