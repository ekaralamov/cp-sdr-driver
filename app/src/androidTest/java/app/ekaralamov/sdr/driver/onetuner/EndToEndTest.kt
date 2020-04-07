package app.ekaralamov.sdr.driver.onetuner

import android.content.ComponentName
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.ekaralamov.sdr.driver.buildContentUri
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {
    @Test
    fun openUri() {
        val uri = buildContentUri(TunerOneId)
        InstrumentationRegistry.getInstrumentation().targetContext.grantUriPermission(
            "app.ekaralamov.sdr.driver.testutils",
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        InstrumentationRegistry.getInstrumentation().context.startActivity(
            Intent().apply {
                component = ComponentName(
                    "app.ekaralamov.sdr.driver.testutils",
                    "app.ekaralamov.sdr.driver.testutils.MainActivity"
                )
                action = Intent.ACTION_MAIN
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )

        Thread.sleep(5000)
    }
}
