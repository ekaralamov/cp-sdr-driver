package sdr.driver.cp.test.buddy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import sdr.driver.cp.permissions.TunerAccessClient
import sdr.driver.cp.permissions.intent

class AccessWantingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            startActivityForResult(
                TunerAccessClient.intent(
                    checkNotNull(
                        intent.getParcelableExtra(
                            TunerAccessClient.Extra.Device
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
