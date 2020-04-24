package app.ekaralamov.sdr.driver

import android.hardware.usb.UsbDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GetTunerAccessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device: UsbDevice? = intent.getParcelableExtra(GetTunerAccessDeviceExtra)
        if (device == null) {
            setResult(GetTunerAccessResult.IllegalArgument)
            finish()
            return
        }

        @Suppress("UNCHECKED_CAST")
        val viewModelProvider = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>) = when (modelClass) {
                GetTunerAccessViewModel::class.java -> DriverApplication.appComponent
                    .injectGetTunerAccessViewModelFactory().create(device)
                else -> throw Exception("unknown view model type")
            } as T
        })

        val getTunerAccessViewModel = viewModelProvider.get(GetTunerAccessViewModel::class.java)

        lifecycleScope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED) {
            val result =
                if (getTunerAccessViewModel.outcomeChannel.receive())
                    RESULT_OK
                else
                    GetTunerAccessResult.AccessDenied
            setResult(result)
            finish()
        }
    }
}
