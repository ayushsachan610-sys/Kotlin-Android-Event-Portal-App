package com.zenith26.fest.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zenith26.fest.R
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.Registration
import com.zenith26.fest.data.network.ApiClient
import com.zenith26.fest.databinding.FragmentAdminDashboardBinding
import com.zenith26.fest.databinding.ItemCategoryBreakdownPillBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsAdapter: AdminEventsAdapter
    private lateinit var registrationsAdapter: AdminRegistrationsAdapter

    private var cachedEvents: List<Event> = emptyList()
    private var selectedFilterEventId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupTabs()
        setupActions()

        loadDashboardData()
    }

    private fun setupAdapters() {
        eventsAdapter = AdminEventsAdapter()
        binding.recyclerAdminEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
        }

        registrationsAdapter = AdminRegistrationsAdapter { reg ->
            confirmAndCancelRegistration(reg)
        }
        binding.recyclerAdminRegistrations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = registrationsAdapter
        }
    }

    private fun setupTabs() {
        val activeBg = R.drawable.bg_pill_active
        val inactiveBg = R.drawable.bg_pill_inactive
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.slate_400)

        binding.tabAdminEvents.setOnClickListener {
            binding.tabAdminEvents.setBackgroundResource(activeBg)
            binding.tabAdminEvents.setTextColor(activeColor)
            binding.tabAdminRegistrations.setBackgroundResource(inactiveBg)
            binding.tabAdminRegistrations.setTextColor(inactiveColor)

            binding.contentTabEvents.visibility = View.VISIBLE
            binding.contentTabRegistrations.visibility = View.GONE
        }

        binding.tabAdminRegistrations.setOnClickListener {
            binding.tabAdminRegistrations.setBackgroundResource(activeBg)
            binding.tabAdminRegistrations.setTextColor(activeColor)
            binding.tabAdminEvents.setBackgroundResource(inactiveBg)
            binding.tabAdminEvents.setTextColor(inactiveColor)

            binding.contentTabEvents.visibility = View.GONE
            binding.contentTabRegistrations.visibility = View.VISIBLE
        }
    }

    private fun setupActions() {
        binding.adminSwipeRefresh.setColorSchemeResources(R.color.brand_500, R.color.fuchsia_500)
        binding.adminSwipeRefresh.setOnRefreshListener {
            loadDashboardData()
        }

        binding.btnRefreshDashboard.setOnClickListener {
            loadDashboardData()
        }

        binding.btnAddEvent.setOnClickListener {
            val dialog = AddEventDialogFragment.newInstance()
            dialog.onEventCreated = {
                loadDashboardData()
            }
            dialog.show(parentFragmentManager, AddEventDialogFragment.TAG)
        }
    }

    fun loadDashboardData() {
        binding.tvLastUpdated.text = "Updating..."

        lifecycleScope.launch {
            try {
                val api = ApiClient.getApiService(requireContext())

                val statsDeferred = async { api.getStats() }
                val eventsDeferred = async { api.getEvents() }
                val registrationsDeferred = async { api.getRegistrations(selectedFilterEventId) }

                val statsRes = statsDeferred.await()
                val eventsRes = eventsDeferred.await()
                val regRes = registrationsDeferred.await()

                binding.adminSwipeRefresh.isRefreshing = false

                // 1. Render Stats
                if (statsRes.isSuccessful && statsRes.body() != null) {
                    val stats = statsRes.body()!!
                    binding.tvStatTotalEvents.text = stats.totalEvents.toString()
                    binding.tvStatTotalRegistrations.text = stats.totalRegistrations.toString()
                    binding.tvStatUtilizationPct.text = "${stats.overallUtilizationPct}%"
                    binding.tvStatCapacitySub.text = "${stats.totalRegistrations} / ${stats.totalCapacity} total seats"
                    binding.tvStatUpcomingEvents.text = stats.upcomingEventsCount.toString()

                    renderCategoryBreakdown(stats.eventsByCategory)
                }

                // 2. Render Events (Sorted by Utilization % Descending)
                if (eventsRes.isSuccessful && eventsRes.body() != null) {
                    val events = eventsRes.body()!!
                    cachedEvents = events

                    // Sort events by utilization percentage descending by default
                    val sortedEvents = events.sortedByDescending { it.utilizationPct }
                    eventsAdapter.submitList(sortedEvents)

                    setupEventFilterSpinner(events)
                }

                // 3. Render Registrations
                if (regRes.isSuccessful && regRes.body() != null) {
                    val regs = regRes.body()!!
                    registrationsAdapter.submitList(regs)
                    binding.tvRegistrationCountIndicator.text = "Showing ${regs.size} record(s)"
                }

                val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
                binding.tvLastUpdated.text = "Updated ${timeFormat.format(Date())}"

            } catch (e: Exception) {
                binding.adminSwipeRefresh.isRefreshing = false
                binding.tvLastUpdated.text = "Error updating"
                Toast.makeText(requireContext(), "Failed to refresh dashboard: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderCategoryBreakdown(categories: Map<String, Int>) {
        binding.layoutCategoryBreakdown.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (categories.isEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                text = "No category data"
                setTextColor(ContextCompat.getColor(context, R.color.slate_500))
                textSize = 12f
            }
            binding.layoutCategoryBreakdown.addView(emptyTv)
            return
        }

        categories.forEach { (categoryName, count) ->
            val pillBinding = ItemCategoryBreakdownPillBinding.inflate(
                inflater,
                binding.layoutCategoryBreakdown,
                false
            )
            pillBinding.tvCategoryName.text = categoryName
            pillBinding.tvCategoryCount.text = "$count ${if (count == 1) "event" else "events"}"
            binding.layoutCategoryBreakdown.addView(pillBinding.root)
        }
    }

    private fun setupEventFilterSpinner(events: List<Event>) {
        val options = mutableListOf("All Events (${events.size})")
        events.forEach { ev ->
            options.add("${ev.title} (${ev.category})")
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        binding.spinnerFilterEvent.adapter = adapter

        binding.spinnerFilterEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newFilterId = if (position == 0) null else events.getOrNull(position - 1)?.id
                if (selectedFilterEventId != newFilterId) {
                    selectedFilterEventId = newFilterId
                    loadRegistrationsOnly(newFilterId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadRegistrationsOnly(eventId: Int?) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getApiService(requireContext())
                val res = api.getRegistrations(eventId)
                if (res.isSuccessful && res.body() != null) {
                    val regs = res.body()!!
                    registrationsAdapter.submitList(regs)
                    binding.tvRegistrationCountIndicator.text = "Showing ${regs.size} record(s)"
                }
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }

    private fun confirmAndCancelRegistration(registration: Registration) {
        AlertDialog.Builder(requireContext(), R.style.Theme_Zenith26)
            .setTitle("Cancel Registration")
            .setMessage("Are you sure you want to cancel the registration for \"${registration.studentName}\" (ID #${registration.id})?\n\nThis will instantly free up 1 seat for \"${registration.eventTitle}\".")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                executeCancelRegistration(registration.id)
            }
            .setNegativeButton("Keep", null)
            .show()
    }

    private fun executeCancelRegistration(registrationId: Int) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getApiService(requireContext())
                val response = api.cancelRegistration(registrationId)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Registration #$registrationId cancelled successfully.", Toast.LENGTH_SHORT).show()
                    loadDashboardData()
                } else {
                    val error = ApiClient.parseErrorMessage(response)
                    Toast.makeText(requireContext(), "Failed to cancel: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: Could not contact server.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AdminDashboardFragment()
    }
}
