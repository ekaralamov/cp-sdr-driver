package sdr.driver.cp.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ManagePermissionsViewModel @Inject constructor(
    private val listAccessResolutions: ListAccessResolutions,
    private val revokePackageAccess: RevokePackageAccess,
    private val grantPackageAccess: GrantPackageAccess
) : ViewModel() {

    private val _accessResolutions = MutableStateFlow<Result<List<Pair<String, Boolean>>>?>(null)
    val accessResolutions: StateFlow<Result<List<Pair<String, Boolean>>>?> = _accessResolutions

    init {
        viewModelScope.launch {
            try {
                listAccessResolutions().collect { _accessResolutions.value = Result.success(it) }
            } catch (throwable: Throwable) {
                _accessResolutions.value = Result.failure(throwable)
            }
        }
    }

    fun revokeAccess(packageName: String) = runBlocking {
        revokePackageAccess(packageName)
    }

    fun grantAccess(packageName: String) = runBlocking {
        grantPackageAccess(packageName)
    }
}
