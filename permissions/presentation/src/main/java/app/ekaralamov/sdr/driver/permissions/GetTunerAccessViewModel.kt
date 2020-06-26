package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    enum class Outcome {
        Granted,
        DeviceAccessDenied,
        ClientPermissionDenied,
        ClientPermissionDeniedPermanently
    }

    suspend fun outcome(): Outcome = outcomeChannel.receive()

    inner class GrantPermissionToClientQuestion internal constructor(private val delegate: GetTunerAccess.Result.GrantPermissionToClientQuestion) {

        val never: (() -> Unit)? =
            if (delegate.never == null)
                null
            else {
                {
                    viewModelScope.launch {
                        try {
                            _grantPermissionToClientQuestion.value = null
                            delegate.never!!()
                            outcomeChannel.send(Outcome.ClientPermissionDeniedPermanently)
                        } catch (throwable: Throwable) {
                            outcomeChannel.close(throwable)
                        }
                    }
                }
            }

        fun yes() {
            viewModelScope.launch {
                try {
                    _grantPermissionToClientQuestion.value = null
                    when (delegate.yes()) {
                        GetTunerAccess.Result.DeviceAccess.Granted -> Outcome.Granted
                        GetTunerAccess.Result.DeviceAccess.Denied -> Outcome.DeviceAccessDenied
                    }.sendVia(outcomeChannel)
                } catch (throwable: Throwable) {
                    outcomeChannel.close(throwable)
                }
            }
        }

        fun no() {
            viewModelScope.launch {
                try {
                    _grantPermissionToClientQuestion.value = null
                    delegate.no()
                    outcomeChannel.send(Outcome.ClientPermissionDenied)
                } catch (throwable: Throwable) {
                    outcomeChannel.close(throwable)
                }
            }
        }
    }

    private val outcomeChannel = Channel<Outcome>()
    private val _grantPermissionToClientQuestion =
        MutableStateFlow<GrantPermissionToClientQuestion?>(null)

    val grantPermissionToClientQuestion: StateFlow<GrantPermissionToClientQuestion?> =
        _grantPermissionToClientQuestion

    init {
        viewModelScope.launch {
            try {
                when (val result = getTunerAccess(clientPackageName, device)) {
                    GetTunerAccess.Result.DeviceAccess.Granted -> outcomeChannel.send(Outcome.Granted)
                    GetTunerAccess.Result.DeviceAccess.Denied -> outcomeChannel.send(Outcome.DeviceAccessDenied)
                    GetTunerAccess.Result.ClientPermissionDeniedPermanently -> outcomeChannel.send(Outcome.ClientPermissionDeniedPermanently)
                    is GetTunerAccess.Result.GrantPermissionToClientQuestion ->
                        _grantPermissionToClientQuestion.value = GrantPermissionToClientQuestion(result)
                }
            } catch (throwable: Throwable) {
                outcomeChannel.close(throwable)
            }
        }
    }

    companion object {
        private suspend fun <T> T.sendVia(channel: SendChannel<T>) = channel.send(this)
    }
}
