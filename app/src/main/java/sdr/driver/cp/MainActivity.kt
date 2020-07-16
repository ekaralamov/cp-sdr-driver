package sdr.driver.cp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        findViewById<TextView>(R.id.intro).text = getString(R.string.intro, getString(R.string.app_name))

        findViewById<TextView>(R.id.version).text =
            getString(R.string.about_version, BuildConfig.VERSION_NAME)

        findViewById<View>(R.id.license).setOnClickListener {
            LicenseActivity.start("file:///android_asset/gpl3.html", this)
        }

        findViewById<View>(R.id.third_party_software).setOnClickListener {
            startActivity(Intent(this, AttributionActivity::class.java))
        }

        findViewById<View>(R.id.web_link).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.app_web_site).toUri()))
        }
    }
}
