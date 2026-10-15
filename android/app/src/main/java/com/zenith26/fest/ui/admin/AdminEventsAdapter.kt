package com.zenith26.fest.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zenith26.fest.R
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.databinding.ItemAdminEventRowBinding
import com.zenith26.fest.utils.CategoryHelper
import java.util.Locale

class AdminEventsAdapter : ListAdapter<Event, AdminEventsAdapter.AdminEventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminEventViewHolder {
        val binding = ItemAdminEventRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminEventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdminEventViewHolder(
        private val binding: ItemAdminEventRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val context = binding.root.context

            // Icon & Title
            binding.tvAdminEventIcon.text = event.icon ?: "⚡"
            binding.tvAdminEventTitle.text = event.title

            // Category badge
            CategoryHelper.applyCategoryBadge(binding.tvAdminEventCategory, event.category)

            // Schedule & Venue
            binding.tvAdminEventSchedule.text = "${event.eventDate} • ${event.eventTime}"
            binding.tvAdminEventVenue.text = event.venue

            // Occupancy
            val registered = event.registeredCount
            binding.tvAdminOccupancy.text = "$registered / ${event.capacity} seats"

            // Utilization %
            val utilPct = event.utilizationPct
            val formattedUtil = String.format(Locale.US, "%.1f%%", utilPct)
            binding.tvAdminUtilizationPct.text = formattedUtil

            // High utilization (>=90%) highlight
            val isOver90 = utilPct >= 90f
            val isFull = event.isFull

            if (isOver90) {
                binding.cardAdminEvent.setBackgroundResource(R.drawable.bg_card_danger)
                binding.tvAdminUtilizationPct.setTextColor(ContextCompat.getColor(context, R.color.rose_400))
            } else if (utilPct > 50f) {
                binding.cardAdminEvent.setBackgroundResource(R.drawable.bg_card)
                binding.tvAdminUtilizationPct.setTextColor(ContextCompat.getColor(context, R.color.amber_300))
            } else {
                binding.cardAdminEvent.setBackgroundResource(R.drawable.bg_card)
                binding.tvAdminUtilizationPct.setTextColor(ContextCompat.getColor(context, R.color.slate_300))
            }

            // Status Badge
            if (isFull) {
                binding.tvAdminStatusBadge.text = "FULL"
                binding.tvAdminStatusBadge.setBackgroundResource(R.drawable.bg_badge_full)
                binding.tvAdminStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (isOver90) {
                binding.tvAdminStatusBadge.text = "Near Capacity"
                binding.tvAdminStatusBadge.setBackgroundResource(R.drawable.bg_badge_seats_left)
                binding.tvAdminStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.amber_300))
            } else {
                binding.tvAdminStatusBadge.text = "Available"
                binding.tvAdminStatusBadge.setBackgroundResource(R.drawable.bg_badge_gaming)
                binding.tvAdminStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_300))
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }
}
