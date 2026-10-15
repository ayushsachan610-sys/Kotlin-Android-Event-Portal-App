package com.zenith26.fest.ui.student

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zenith26.fest.R
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.network.ApiClient
import com.zenith26.fest.data.network.ApiConfig
import com.zenith26.fest.databinding.FragmentStudentPortalBinding
import kotlinx.coroutines.launch

class StudentPortalFragment : Fragment() {

    private var _binding: FragmentStudentPortalBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventAdapter: EventAdapter
    private var currentCategory = "All"
    private var currentSearch = ""

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentPortalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCategoryFilters()
        setupSearchInput()
        setupSwipeRefresh()
        setupStateButtons()

        fetchAndRenderEvents()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            openEventDetailsModal(event.id)
        }
        binding.recyclerEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_500, R.color.fuchsia_500)
        binding.swipeRefresh.setOnRefreshListener {
            fetchAndRenderEvents()
        }
    }

    private var categoryPills: List<Pair<TextView, String>> = emptyList()

    private fun setupCategoryFilters() {
        categoryPills = listOf(
            binding.pillCatAll to "All",
            binding.pillCatTech to "Technical",
            binding.pillCatCultural to "Cultural",
            binding.pillCatSports to "Sports",
            binding.pillCatGaming to "Gaming",
            binding.pillCatWorkshop to "Workshop"
        )

        categoryPills.forEach { (textView, categoryName) ->
            textView.setOnClickListener {
                if (currentCategory != categoryName) {
                    currentCategory = categoryName
                    updateCategoryPillUI(categoryName)
                    fetchAndRenderEvents()
                }
            }
        }
    }

    private fun updateCategoryPillUI(selectedCategory: String) {
        val context = context ?: return
        categoryPills.forEach { (textView, categoryName) ->
            if (categoryName.equals(selectedCategory, ignoreCase = true)) {
                textView.setBackgroundResource(R.drawable.bg_pill_active)
                textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                textView.setBackgroundResource(R.drawable.bg_pill_inactive)
                textView.setTextColor(ContextCompat.getColor(context, R.color.slate_300))
            }
        }
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    currentSearch = s?.toString()?.trim() ?: ""
                    fetchAndRenderEvents()
                }
                searchHandler.postDelayed(searchRunnable!!, 250) // 250ms debounce matching web
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupStateButtons() {
        binding.btnResetFilters.setOnClickListener {
            binding.etSearch.text?.clear()
            currentSearch = ""
            currentCategory = "All"
            updateCategoryPillUI("All")
            fetchAndRenderEvents()
        }

        binding.btnRetry.setOnClickListener {
            fetchAndRenderEvents()
        }
    }

    fun fetchAndRenderEvents() {
        // Show loading state
        if (!binding.swipeRefresh.isRefreshing) {
            binding.layoutLoading.visibility = View.VISIBLE
        }
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val api = ApiClient.getApiService(requireContext())
                val categoryParam = if (currentCategory == "All") null else currentCategory
                val searchParam = if (currentSearch.isEmpty()) null else currentSearch

                val response = api.getEvents(category = categoryParam, search = searchParam)

                binding.layoutLoading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful && response.body() != null) {
                    val events = response.body()!!

                    if (events.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        eventAdapter.submitList(emptyList())
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        eventAdapter.submitList(events)
                    }
                } else {
                    showErrorState("Server returned HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                binding.layoutLoading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                val currentUrl = ApiConfig.getBaseUrl(requireContext())
                showErrorState("Make sure the FastAPI backend is running at $currentUrl\n\n(${e.localizedMessage ?: "Connection error"})")
            }
        }
    }

    private fun showErrorState(details: String) {
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorDetails.text = details
        eventAdapter.submitList(emptyList())
    }

    private fun openEventDetailsModal(eventId: Int) {
        val bottomSheet = EventDetailBottomSheetDialogFragment.newInstance(eventId)
        bottomSheet.onRegistrationSuccess = {
            // Immediate live refresh
            fetchAndRenderEvents()
        }
        bottomSheet.show(parentFragmentManager, EventDetailBottomSheetDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }

    companion object {
        fun newInstance() = StudentPortalFragment()
    }
}
