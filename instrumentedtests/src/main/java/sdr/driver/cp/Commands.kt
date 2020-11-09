package sdr.driver.cp

object Commands {

    val Ignored = byteArrayOf(-1, 0, 0, 0, 0)

    val Frequency = byteArrayOf(1, 0x05, -84, 0x74, 0x20)
    val SampleRate = byteArrayOf(2, 0, 0x11, -108, 0)
}
