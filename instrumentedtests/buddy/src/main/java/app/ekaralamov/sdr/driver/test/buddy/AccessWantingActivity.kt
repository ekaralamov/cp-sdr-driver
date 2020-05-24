package app.ekaralamov.sdr.driver.test.buddy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.ekaralamov.sdr.driver.permissions.TunerAccessClient
import app.ekaralamov.sdr.driver.permissions.intent

class AccessWantingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            startActivityForResult(
                TunerAccessClient.intent(
                    checkNotNull(
                        intent.getParcelableExtra(
                            TunerAccessClient.DeviceExtra
                        )
                    )
                ),
                0
            )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ActivityRegistry.set(resultCode, intent.getIntExtra(RequestKeyExtra, 0))
        finish()
    }

    companion object {

        internal const val RequestKeyExtra = "request key"
    }
}
