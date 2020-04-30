package app.ekaralamov.sdr.driver.opening

data class DeviceAddress(val path: String, val vendorId: Int, val productId: Int) {

    companion object
}
