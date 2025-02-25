package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.jinja_business

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.cwp.jinja_hub.R
import com.google.type.Color

class JinjaBusinessFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewHolder: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.jinja_business_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views from layout.
        webView = view.findViewById(R.id.webView)
        progressBar = view.findViewById(R.id.progressBar)
        viewHolder = view.findViewById(R.id.view)

        // Configure WebView to load URLs within the app.
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                // Show the progress bar until the page is fully loaded.
                if (newProgress < 100) {
                    progressBar.progress = newProgress
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                    viewHolder.setBackgroundResource(R.color.light_grey)
                }
            }
        }

        // Enable JavaScript if needed (be cautious of XSS vulnerabilities).
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false

        // Load the desired URL.
        webView.loadUrl("https://mymultistreams.com/Home.aspx")

        // Handle back presses: go back in WebView history if possible.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    // If no history exists, let the system handle the back press.
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
