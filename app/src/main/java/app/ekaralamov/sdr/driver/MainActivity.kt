package app.ekaralamov.sdr.driver

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        findViewById<TextView>(R.id.intro).text = getString(R.string.intro, getString(R.string.app_name))
        findViewById<TextView>(R.id.version).text =
            getString(R.string.about_version, BuildConfig.VERSION_NAME)
    }
}
