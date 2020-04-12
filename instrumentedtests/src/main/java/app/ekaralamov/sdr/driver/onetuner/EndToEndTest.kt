package app.ekaralamov.sdr.driver.onetuner

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ekaralamov.sdr.driver.BuddyRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @get:Rule
    val buddyRule = BuddyRule()

    @Test
    fun openUri() {
//        val uri = buildContentUri(TunerOneId)
//        InstrumentationRegistry.getInstrumentation().targetContext.grantUriPermission(
//            "app.ekaralamov.sdr.driver.test.buddy",
//            uri,
//            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        )

//        InstrumentationRegistry.getInstrumentation().context.startActivity(
//            Intent().apply {
//                component = ComponentName(
//                    "app.ekaralamov.sdr.driver.test.buddy",
//                    "app.ekaralamov.sdr.driver.test.buddy.MainActivity"
//                )
//                action = Intent.ACTION_MAIN
//                data = uri
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//        )
//
//        Thread.sleep(5000)

    }
}
