package sdr.driver.cp

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector

object UsbDeviceAccessDialog {

    fun answerWithYes() {
        val device = UiDevice.getInstance(getInstrumentation())

        val okButton =
            device.findObject(UiSelector().packageName("com.android.systemui").text("OK"))
        try {
            okButton.click()
        } catch (permissionPossiblyAlreadyGranted: UiObjectNotFoundException) {
        }

        val possiblyBroughtToFrontPreexistingTunerConnectDialogButton =
            device.findObject(UiSelector().packageName("com.android.systemui").text("CANCEL"))
        try {
            possiblyBroughtToFrontPreexistingTunerConnectDialogButton.click()
        } catch (noPreexistingDeviceConnectDialog: UiObjectNotFoundException) {
        }
    }
}
