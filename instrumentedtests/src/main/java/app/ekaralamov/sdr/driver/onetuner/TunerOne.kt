package app.ekaralamov.sdr.driver.onetuner

import app.ekaralamov.sdr.driver.findDevice

object TunerOne {

    val Device = findDevice(vendorID = 0x0bda, productID = 0x2832)
}
