package app.ekaralamov.sdr.driver

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class GetTunerAccessViewModel @AssistedInject constructor(
    private val getDevicePermission: GetDevicePermission,
    @Assisted private val device: UsbDevice
) : ViewModel() {

    @AssistedInject.Factory
    interface Factory {

        fun create(device: UsbDevice): GetTunerAccessViewModel
    }

    private val _outcomeChannel = Channel<Boolean>()
    val outcomeChannel: ReceiveChannel<Boolean>
        get() = _outcomeChannel

    init {
        viewModelScope.launch(Dispatchers.Unconfined) {
            getDevicePermission(device).sendVia(_outcomeChannel)
        }
    }

    companion object {
        private suspend fun <T> T.sendVia(channel: SendChannel<T>) = channel.send(this)
    }
}
