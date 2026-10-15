package com.zenith26.fest.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zenith26.fest.data.model.Registration
import com.zenith26.fest.databinding.ItemAdminRegistrationRowBinding
import com.zenith26.fest.utils.DateHelper

class AdminRegistrationsAdapter(
    private val onCancelClick: (Registration) -> Unit
) : ListAdapter<Registration, AdminRegistrationsAdapter.RegistrationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistrationViewHolder {
        val binding = ItemAdminRegistrationRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RegistrationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegistrationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RegistrationViewHolder(
        private val binding: ItemAdminRegistrationRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(registration: Registration) {
            binding.tvRegId.text = "#${registration.id}"
            binding.tvRegStudentName.text = registration.studentName
            binding.tvRegRoll.text = registration.rollNumber
            binding.tvRegEventTitle.text = registration.eventTitle
            binding.tvRegEmail.text = registration.email
            binding.tvRegPhone.text = registration.phone ?: "—"
            binding.tvRegTimestamp.text = DateHelper.formatIsoTimestamp(registration.registeredAt)

            binding.btnCancelReg.setOnClickListener {
                onCancelClick(registration)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Registration>() {
            override fun areItemsTheSame(oldItem: Registration, newItem: Registration): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Registration, newItem: Registration): Boolean {
                return oldItem == newItem
            }
        }
    }
}
