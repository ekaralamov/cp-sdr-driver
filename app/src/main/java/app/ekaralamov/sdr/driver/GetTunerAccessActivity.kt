package app.ekaralamov.sdr.driver

import android.app.Activity
import android.os.Bundle

class GetTunerAccessActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}

//fun findDevice(vendorID: Int, productID: Int): UsbDevice {
//    val usbManager = InstrumentationRegistry.getInstrumentation().context
//        .getSystemService(Context.USB_SERVICE) as UsbManager
//    return usbManager
//        .deviceList.values.single { it.vendorId == vendorID && it.productId == productID }
//}
