package com.zenith26.fest.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.zenith26.fest.R
import com.zenith26.fest.data.network.ApiClient
import com.zenith26.fest.data.network.ApiConfig
import com.zenith26.fest.databinding.DialogServerSettingsBinding
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ServerSettingsDialogFragment : DialogFragment() {

    private var _binding: DialogServerSettingsBinding? = null
    private val binding get() = _binding!!

    var onSettingsSaved: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Zenith26)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogServerSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUrl = ApiConfig.getBaseUrl(requireContext())
        binding.etServerUrl.setText(currentUrl)

        binding.btnSettingsClose.setOnClickListener {
            dismiss()
        }

        binding.btnPresetCloud.setOnClickListener {
            binding.etServerUrl.setText(ApiConfig.PERMANENT_CLOUD_URL)
        }

        binding.btnPresetEmulator.setOnClickListener {
            binding.etServerUrl.setText(ApiConfig.DEFAULT_EMULATOR_URL)
        }

        binding.btnPresetEmulator8082.setOnClickListener {
            binding.etServerUrl.setText(ApiConfig.ALT_PORT_8082_URL)
        }

        binding.btnPresetWifi.setOnClickListener {
            binding.etServerUrl.setText(ApiConfig.DEFAULT_LAN_URL)
        }

        binding.btnPresetWifi8082.setOnClickListener {
            binding.etServerUrl.setText("http://172.16.53.144:8082/")
        }

        binding.btnPresetLocalhost.setOnClickListener {
            binding.etServerUrl.setText(ApiConfig.DEFAULT_LOCALHOST_URL)
        }

        binding.btnTestConnection.setOnClickListener {
            testServerConnection()
        }

        binding.btnSaveServerSettings.setOnClickListener {
            val enteredUrl = binding.etServerUrl.text.toString().trim()
            if (enteredUrl.isEmpty() || (!enteredUrl.startsWith("http://") && !enteredUrl.startsWith("https://"))) {
                Toast.makeText(requireContext(), "Please enter a valid HTTP/HTTPS URL.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiConfig.setBaseUrl(requireContext(), enteredUrl)
            Toast.makeText(requireContext(), "Backend URL updated to: ${ApiConfig.getBaseUrl(requireContext())}", Toast.LENGTH_SHORT).show()
            onSettingsSaved?.invoke()
            dismiss()
        }
    }

    private fun testServerConnection() {
        val testUrl = binding.etServerUrl.text.toString().trim()
        val endpoint = if (testUrl.endsWith("/")) "${testUrl}api/stats" else "$testUrl/api/stats"

        binding.tvConnectionTestResult.visibility = View.VISIBLE
        binding.tvConnectionTestResult.text = "Testing connection to $endpoint..."
        binding.tvConnectionTestResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_300))

        lifecycleScope.launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder().url(endpoint).build()
                val response = kotlinx.coroutines.Dispatchers.IO.let {
                    kotlinx.coroutines.withContext(it) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    binding.tvConnectionTestResult.text = "✅ Connection successful! Server responded with HTTP ${response.code()}."
                    binding.tvConnectionTestResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.emerald_400))
                } else {
                    binding.tvConnectionTestResult.text = "⚠️ Server reached, but returned HTTP ${response.code()}: ${response.message}"
                    binding.tvConnectionTestResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.amber_400))
                }
            } catch (e: Exception) {
                binding.tvConnectionTestResult.text = "❌ Failed to connect: ${e.localizedMessage ?: "Connection refused"}\nVerify FastAPI is running on host machine."
                binding.tvConnectionTestResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.rose_400))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ServerSettingsDialog"
        fun newInstance() = ServerSettingsDialogFragment()
    }
}
