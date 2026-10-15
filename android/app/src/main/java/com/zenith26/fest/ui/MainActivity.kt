package com.zenith26.fest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.zenith26.fest.R
import com.zenith26.fest.databinding.ActivityMainBinding
import com.zenith26.fest.ui.admin.AdminDashboardFragment
import com.zenith26.fest.ui.dialogs.ServerSettingsDialogFragment
import com.zenith26.fest.ui.student.StudentPortalFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var activeTab: String = TAB_EVENTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        if (savedInstanceState == null) {
            switchFragment(StudentPortalFragment.newInstance(), TAB_EVENTS)
        }
    }

    private fun setupNavigation() {
        binding.btnNavEvents.setOnClickListener {
            if (activeTab != TAB_EVENTS) {
                switchFragment(StudentPortalFragment.newInstance(), TAB_EVENTS)
                updateNavUI(isEvents = true)
            }
        }

        binding.btnNavAdmin.setOnClickListener {
            if (activeTab != TAB_ADMIN) {
                switchFragment(AdminDashboardFragment.newInstance(), TAB_ADMIN)
                updateNavUI(isEvents = false)
            }
        }

        binding.btnNavSettings.setOnClickListener {
            val dialog = ServerSettingsDialogFragment.newInstance()
            dialog.onSettingsSaved = {
                // Refresh the current visible fragment with new base URL
                val currentFrag = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFrag is StudentPortalFragment) {
                    currentFrag.fetchAndRenderEvents()
                } else if (currentFrag is AdminDashboardFragment) {
                    currentFrag.loadDashboardData()
                }
            }
            dialog.show(supportFragmentManager, ServerSettingsDialogFragment.TAG)
        }
    }

    private fun updateNavUI(isEvents: Boolean) {
        val activeBg = R.drawable.bg_pill_active
        val inactiveBg = R.drawable.bg_pill_inactive
        val activeColor = ContextCompat.getColor(this, R.color.white)
        val inactiveColor = ContextCompat.getColor(this, R.color.slate_300)

        if (isEvents) {
            binding.btnNavEvents.setBackgroundResource(activeBg)
            binding.btnNavEvents.setTextColor(activeColor)
            binding.btnNavAdmin.setBackgroundResource(inactiveBg)
            binding.btnNavAdmin.setTextColor(inactiveColor)
            activeTab = TAB_EVENTS
        } else {
            binding.btnNavAdmin.setBackgroundResource(activeBg)
            binding.btnNavAdmin.setTextColor(activeColor)
            binding.btnNavEvents.setBackgroundResource(inactiveBg)
            binding.btnNavEvents.setTextColor(inactiveColor)
            activeTab = TAB_ADMIN
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String) {
        activeTab = tag
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    companion object {
        private const val TAB_EVENTS = "tab_events"
        private const val TAB_ADMIN = "tab_admin"
    }
}
