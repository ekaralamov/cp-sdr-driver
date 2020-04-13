package app.ekaralamov.sdr.driver

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry

object TunerUri {
    fun build(vendorID: Int, productID: Int): Uri = Uri.Builder()
        .scheme("content")
        .authority(
            InstrumentationRegistry.getInstrumentation().targetContext
                .getString(R.string.provider_authority)
        )
        .appendPath(vendorID.toString(16))
        .appendPath(productID.toString(16))
        .build()
}
