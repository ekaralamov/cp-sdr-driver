package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class GetTunerAccessViewModel @AssistedInject constructor(
    @Assisted private val clientPackageName: String,
    @Assisted private val device: UsbDevice,
    private val getTunerAccess: GetTunerAccess
) : ViewModel() {

    @AssistedInject.Factory
    interface Factory {

        fun create(clientPackageName: String, device: UsbDevice): GetTunerAccessViewModel
    }

    private val _outcomeChannel = Channel<Boolean>()
    val outcomeChannel: ReceiveChannel<Boolean>
        get() = _outcomeChannel

    init {
        viewModelScope.launch {
            (getTunerAccess(
                clientPackageName,
                device
            ) == GetTunerAccess.Result.DeviceAccess.Granted)
                .sendVia(_outcomeChannel)
        }
    }

    companion object {
        private suspend fun <T> T.sendVia(channel: SendChannel<T>) = channel.send(this)
    }
}
