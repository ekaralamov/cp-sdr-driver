package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import timber.log.Timber

class GetTunerAccessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device: UsbDevice? = intent.getParcelableExtra(GetTunerAccessConstants.DeviceExtra)
        if (device == null) {
            setResult(GetTunerAccessConstants.Result.IllegalArgument)
            finish()
            return
        }

        val clientPackageName = callingPackage
        if (clientPackageName == null) {
            Timber.d("must be started for result")
            finish()
            return
        }

        @Suppress("UNCHECKED_CAST")
        val viewModelProvider = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                when (modelClass) {
                    GetTunerAccessViewModel::class.java -> {
                        PermissionsComponent.instance.injectGetTunerAccessViewModelFactory()
                            .create(clientPackageName, device)
                    }
                    else -> {
                        throw Exception("unknown view model type")
                    }
                } as T
        })

        val getTunerAccessViewModel = viewModelProvider.get(GetTunerAccessViewModel::class.java)

        lifecycleScope.launch {
            val result =
                if (getTunerAccessViewModel.outcomeChannel.receive())
                    RESULT_OK
                else
                    GetTunerAccessConstants.Result.AccessDenied
            setResult(result)
            finish()
        }
    }
}
