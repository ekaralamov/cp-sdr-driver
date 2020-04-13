package app.ekaralamov.sdr.driver.test.buddy

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            startActivityForResult(
                Intent().apply {
                    component = ComponentName(
                        "app.ekaralamov.sdr.driver",
                        "app.ekaralamov.sdr.driver.GetTunerAccessActivity"
                    )
                    data = intent.data
                },
                0
            )
//        contentResolver.delete(intent.data ?: throw IllegalArgumentException(), null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ActivityRegistry.set(resultCode == RESULT_OK, intent.getIntExtra(RequestKeyExtra, 0))
        finish()
    }

    companion object {

        internal const val RequestKeyExtra = "request key"
    }
}
