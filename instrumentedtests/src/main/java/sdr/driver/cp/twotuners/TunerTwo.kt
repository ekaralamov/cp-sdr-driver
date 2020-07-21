package sdr.driver.cp.twotuners

import sdr.driver.cp.findDevice

object TunerTwo {

    val Device = findDevice(vendorID = 0x0bda, productID = 0x2838)
}
