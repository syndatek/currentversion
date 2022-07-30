package com.carditek.kesar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.carditek.kesar.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : WebViewFragment() {
    private lateinit var binding: FragmentSettingsBinding
    override fun url(): String = "https://ecg.carditek.com/#/settings"
    override fun webView(): WebView = binding.settingsWebview

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }
}
