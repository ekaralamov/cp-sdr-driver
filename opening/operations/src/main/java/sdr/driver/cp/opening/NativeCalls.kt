package sdr.driver.cp.opening

internal object NativeCalls {

    init {
        System.loadLibrary("rtlsdr")
    }

    external fun isDeviceSupported(vendorID: Int, productID: Int): Boolean

    external fun open(devFD: Int): Long

    external fun pumpCommands(nativeSessionHandle: Long, inputFD: Int)

    external fun pumpData(nativeSessionHandle: Long, outputFD: Int)

    external fun stopPumps(nativeSessionHandle: Long)

    external fun close(nativeSessionHandle: Long)
}

fun isDeviceSupported(vendorID: Int, productID: Int) =
    NativeCalls.isDeviceSupported(
        vendorID = vendorID,
        productID = productID
    )
