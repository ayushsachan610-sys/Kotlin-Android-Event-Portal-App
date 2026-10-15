package com.zenith26.fest.ui.admin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.zenith26.fest.R
import com.zenith26.fest.data.model.CreateEventRequest
import com.zenith26.fest.data.network.ApiClient
import com.zenith26.fest.databinding.DialogAddEventBinding
import kotlinx.coroutines.launch

class AddEventDialogFragment : DialogFragment() {

    private var _binding: DialogAddEventBinding? = null
    private val binding get() = _binding!!

    var onEventCreated: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Zenith26)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddModalClose.setOnClickListener {
            dismiss()
        }

        setupCategorySpinner()
        setupSubmitButton()
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Technical", "Cultural", "Sports", "Gaming", "Workshop")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerNewCategory.adapter = adapter
    }

    private fun setupSubmitButton() {
        binding.btnSaveEvent.setOnClickListener {
            val title = binding.etNewTitle.text.toString().trim()
            val icon = binding.etNewIcon.text.toString().trim().ifEmpty { "🎪" }
            val category = binding.spinnerNewCategory.selectedItem?.toString() ?: "Technical"
            val venue = binding.etNewVenue.text.toString().trim()
            val date = binding.etNewDate.text.toString().trim()
            val time = binding.etNewTime.text.toString().trim()
            val capacityStr = binding.etNewCapacity.text.toString().trim()
            val feeStr = binding.etNewFee.text.toString().trim()
            val description = binding.etNewDescription.text.toString().trim().ifEmpty { null }

            val capacity = capacityStr.toIntOrNull()
            val fee = feeStr.toIntOrNull() ?: 0

            // Client-side validation
            if (title.isEmpty() || venue.isEmpty() || date.isEmpty() || time.isEmpty() || capacity == null || capacity <= 0) {
                showAlert("Please fill in all required fields and ensure capacity is greater than 0.", isSuccess = false)
                return@setOnClickListener
            }

            binding.btnSaveEvent.isEnabled = false
            binding.btnSaveEvent.text = "Creating event..."

            lifecycleScope.launch {
                try {
                    val api = ApiClient.getApiService(requireContext())
                    val payload = CreateEventRequest(
                        title = title,
                        category = category,
                        venue = venue,
                        eventDate = date,
                        eventTime = time,
                        capacity = capacity,
                        fee = fee,
                        icon = icon,
                        description = description
                    )

                    val response = api.createEvent(payload)

                    if (response.isSuccessful && response.body() != null) {
                        val created = response.body()!!
                        showAlert("🎉 Event \"${created.title}\" created successfully!", isSuccess = true)

                        onEventCreated?.invoke()

                        Handler(Looper.getMainLooper()).postDelayed({
                            dismiss()
                        }, 1200)

                    } else {
                        val errorMsg = ApiClient.parseErrorMessage(response)
                        showAlert(errorMsg, isSuccess = false)
                        binding.btnSaveEvent.isEnabled = true
                        binding.btnSaveEvent.text = "Save and Publish Event"
                    }
                } catch (e: Exception) {
                    showAlert("Network error: Could not contact backend server.", isSuccess = false)
                    binding.btnSaveEvent.isEnabled = true
                    binding.btnSaveEvent.text = "Save and Publish Event"
                }
            }
        }
    }

    private fun showAlert(message: String, isSuccess: Boolean) {
        val context = context ?: return
        binding.layoutAddEventAlert.visibility = View.VISIBLE
        binding.tvAddEventAlertMsg.text = message

        if (isSuccess) {
            binding.layoutAddEventAlert.setBackgroundResource(R.drawable.bg_alert_success)
            binding.tvAddEventAlertMsg.setTextColor(ContextCompat.getColor(context, R.color.emerald_300))
        } else {
            binding.layoutAddEventAlert.setBackgroundResource(R.drawable.bg_alert_error)
            binding.tvAddEventAlertMsg.setTextColor(ContextCompat.getColor(context, R.color.rose_300))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddEventDialogFragment"
        fun newInstance() = AddEventDialogFragment()
    }
}
