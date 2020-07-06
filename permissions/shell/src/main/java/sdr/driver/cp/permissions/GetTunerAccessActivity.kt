package sdr.driver.cp.permissions

import android.app.AlertDialog
import android.app.Dialog
import android.hardware.usb.UsbDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class GetTunerAccessActivity : ComponentActivity() {

    private lateinit var viewModel: GetTunerAccessViewModel
    private lateinit var clientPackageName: String

    private var clientPermissionQuestionDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device: UsbDevice? = intent.getParcelableExtra(TunerAccessClient.Extra.Device)
        if (device == null) {
            setResult(TunerAccessClient.Result.IllegalArgument)
            finish()
            return
        }

        val clientPackageName = callingPackage
        if (clientPackageName == null) {
            Timber.e("must be started for result")
            finish()
            return
        }
        this.clientPackageName = clientPackageName

        @Suppress("UNCHECKED_CAST")
        val viewModelProvider = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                PermissionsComponent.instance.injectGetTunerAccessViewModelFactory()
                    .create(clientPackageName, device) as T
        })

        viewModel = viewModelProvider.get(GetTunerAccessViewModel::class.java)

        launchOutcomeCoroutine()
        launchClientPermissionQuestionCoroutine()
    }

    private fun launchOutcomeCoroutine() = lifecycleScope.launch {
        setResult(
            try {
                when (viewModel.outcome()) {
                    GetTunerAccessViewModel.Outcome.Granted -> RESULT_OK
                    GetTunerAccessViewModel.Outcome.DeviceAccessDenied -> TunerAccessClient.Result.DeviceAccessDenied
                    GetTunerAccessViewModel.Outcome.ClientPermissionDenied -> TunerAccessClient.Result.ClientPermissionDenied
                    GetTunerAccessViewModel.Outcome.ClientPermissionDeniedPermanently -> TunerAccessClient.Result.ClientPermissionDeniedPermanently
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                TunerAccessClient.Result.Error
            }
        )
        finish()
    }

    private fun launchClientPermissionQuestionCoroutine() = lifecycleScope.launch {
        viewModel.grantPermissionToClientQuestion
            .onEach {
                clientPermissionQuestionDialog?.run {
                    dismiss()
                    clientPermissionQuestionDialog = null
                }
            }.filterNotNull()
            .flowOn(Dispatchers.Main)
            .mapLatest { it to packageManager.resolveAppName(clientPackageName) }
            .flowOn(Dispatchers.IO)
            .collect { (question, clientAppName) ->
                clientPermissionQuestionDialog = AlertDialog.Builder(this@GetTunerAccessActivity)
                    .setMessage(
                        getString(
                            R.string.client_permission_question,
                            clientAppName,
                            getString(R.string.app_name)
                        )
                    )
                    .setPositiveButton(R.string.client_permission_question_yes, null)
                    .setNeutralButton(R.string.client_permission_question_no, null)
                    .apply {
                        question.never?.run {
                            setNegativeButton(R.string.client_permission_question_never, null)
                        }
                    }
                    .setCancelable(false)
                    .show()
                    .apply {
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { question.yes() }
                        getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { question.no() }
                        question.never?.let { never ->
                            getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { never() }
                        }
                    }
            }
    }

    override fun onDestroy() {
        clientPermissionQuestionDialog?.dismiss()

        super.onDestroy()
    }
}
