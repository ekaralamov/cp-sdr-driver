package sdr.driver.cp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        findViewById<TextView>(R.id.intro).text = getString(R.string.intro, getString(R.string.app_name))

        findViewById<TextView>(R.id.version).text =
            getString(R.string.about_version, BuildConfig.VERSION_NAME)

        findViewById<View>(R.id.license).setOnClickListener {
            LicenseActivity.start(Dependencies.License.Gpl3, this)
        }
    }
}
