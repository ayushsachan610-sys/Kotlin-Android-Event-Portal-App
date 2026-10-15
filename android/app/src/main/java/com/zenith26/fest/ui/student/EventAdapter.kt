package com.zenith26.fest.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zenith26.fest.R
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.databinding.ItemEventCardBinding
import com.zenith26.fest.utils.CategoryHelper

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val context = binding.root.context

            // Icon
            binding.tvEventIcon.text = event.icon ?: "⚡"

            // Category badge styling
            CategoryHelper.applyCategoryBadge(binding.tvEventCategory, event.category)

            // Fee indicator
            if (event.fee > 0) {
                binding.tvEventFee.text = "₹${event.fee}"
                binding.tvEventFee.setTextColor(ContextCompat.getColor(context, R.color.amber_400))
            } else {
                binding.tvEventFee.text = "Free"
                binding.tvEventFee.setTextColor(ContextCompat.getColor(context, R.color.emerald_400))
            }

            // Title and Description
            binding.tvEventTitle.text = event.title
            binding.tvEventDescription.text = event.description ?: "Join us for an exciting fest experience."

            // Schedule & Venue
            binding.tvEventSchedule.text = "${event.eventDate} • ${event.eventTime}"
            binding.tvEventVenue.text = event.venue

            // Seat status badge
            val density = context.resources.displayMetrics.density
            val padH = (8 * density).toInt()
            val padV = (4 * density).toInt()
            if (event.isFull) {
                binding.tvSeatBadge.text = "FULL"
                binding.tvSeatBadge.setBackgroundResource(R.drawable.bg_badge_full)
                binding.tvSeatBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (event.availableSeats <= 10) {
                binding.tvSeatBadge.text = "${event.availableSeats} / ${event.capacity} seats left"
                binding.tvSeatBadge.setBackgroundResource(R.drawable.bg_badge_seats_left)
                binding.tvSeatBadge.setTextColor(ContextCompat.getColor(context, R.color.amber_300))
            } else {
                binding.tvSeatBadge.text = "${event.availableSeats} / ${event.capacity} seats"
                binding.tvSeatBadge.background = null
                binding.tvSeatBadge.setTextColor(ContextCompat.getColor(context, R.color.slate_400))
            }
            binding.tvSeatBadge.setPadding(padH, padV, padH, padV)

            // Click listener
            binding.btnViewDetails.setOnClickListener {
                onEventClick(event)
            }
            binding.root.setOnClickListener {
                onEventClick(event)
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
