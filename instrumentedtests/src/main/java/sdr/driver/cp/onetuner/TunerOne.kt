package sdr.driver.cp.onetuner

import sdr.driver.cp.findDevice

object TunerOne {

    val Device = findDevice(vendorID = 0x0bda, productID = 0x2832)
}
