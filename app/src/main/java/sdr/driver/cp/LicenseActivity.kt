package sdr.driver.cp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import kotlin.math.roundToInt

class LicenseActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.license_activity)
        webView = findViewById(R.id.web_view)

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                title = webView.title
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                return true
            }
        }

        val url = "file:///android_asset/${getLicenseFileName()}.html"
        if (savedInstanceState == null)
            webView.loadUrl(url)
        else
            webView.loadAndRestoreScrollPosition(url, savedInstanceState.getFloat(ScrollPositionKey))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putFloat(ScrollPositionKey, webView.scrollPosition)
    }

    private fun getLicenseFileName(): String {
        @Suppress("MoveVariableDeclarationIntoWhen")
        val license = Dependencies.License.valueOf(intent.getStringExtra(LicenseExtra)!!)
        return when (license) {
            Dependencies.License.Apache2 -> "apache2"
            Dependencies.License.Gpl3 -> "gpl3"
            Dependencies.License.Lgpl3 -> "lgpl3"
        }
    }

    companion object {

        private val LicenseExtra = "${LicenseActivity::class.qualifiedName}.license"

        private val ScrollPositionKey = "${LicenseActivity::class.qualifiedName}.scrollposition"

        fun start(license: Dependencies.License, parent: Activity) {
            parent.startActivity(
                Intent(parent, LicenseActivity::class.java).putExtra(
                    LicenseExtra,
                    license.name
                )
            )
        }
    }
}

private val WebView.scrollPosition: Float
    get() = scrollY.toFloat() / contentHeight

private fun WebView.loadAndRestoreScrollPosition(
    url: String,
    scrollPosition: Float
) {
    visibility = View.INVISIBLE
    loadUrl(url)
    whenContentHeightIsCalculated {
        scrollY =
            (scrollPosition * contentHeight).roundToInt()
        visibility = View.VISIBLE
    }
}

private fun WebView.whenContentHeightIsCalculated(block: WebView.() -> Unit) {
    postDelayed(
        { block() },
        550
    )
}
