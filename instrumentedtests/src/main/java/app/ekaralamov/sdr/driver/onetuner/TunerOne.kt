package app.ekaralamov.sdr.driver.onetuner

import app.ekaralamov.sdr.driver.Tuner

object TunerOne {

    private const val VendorID = 0x0bda
    private const val ProductID = 0x2832

    val Device = Tuner.findDevice(vendorID = VendorID, productID = ProductID)
}
