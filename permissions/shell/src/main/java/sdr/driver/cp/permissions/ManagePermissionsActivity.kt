package sdr.driver.cp.permissions

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber

class ManagePermissionsActivity : ComponentActivity() {

    private inner class MyViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.permission_item, parent, false)),
        View.OnClickListener,
        Runnable {

        lateinit var data: AppPermissionDisplayData
        private var clicked = false

        init {
            itemView.setOnClickListener(this)
        }

        private val switch = itemView.findViewById<Switch>(R.id.the_switch)
        private val icon = itemView.findViewById<ImageView>(R.id.icon)

        fun bind(appPermissionDisplayData: AppPermissionDisplayData) = with(appPermissionDisplayData) {
            if (!::data.isInitialized || data.packageName != packageName) {
                clicked = false
                icon.setImageDrawable(appIcon)
                switch.text = appName
                switch.isChecked = permissionGranted
            } else if (clicked)
                switch.post(this@MyViewHolder)
            else {
                switch.isChecked = permissionGranted
            }
            data = this
        }

        override fun onClick(v: View) =
            try{
                with(data) {
                    if (permissionGranted)
                        viewModel.revokeAccess(packageName)
                    else
                        viewModel.grantAccess(packageName)
                }
                clicked = true
            } catch (throwable: Throwable) {
                displayErrorMessage(throwable)
            }

        override fun run() {
            if (!clicked)
                return
            clicked = false
            if (switch.isChecked != data.permissionGranted)
                switch.toggle()
        }
    }

    private lateinit var viewModel: ManagePermissionsViewModel

    private val permissionsAdapter = object : RecyclerView.Adapter<MyViewHolder>() {

        private var permissionsList: List<AppPermissionDisplayData> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent)

        override fun getItemCount() = permissionsList.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) =
            holder.bind(permissionsList[position])

        fun set(permissionsList: List<AppPermissionDisplayData>) {
            this.permissionsList = permissionsList
            notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.manage_permissions_activity)

        actionBar!!.subtitle = getString(R.string.manage_permissions_subtitle)

        viewModel = obtainViewModel()

        initRecyclerView()

        launchPermissionsListCoroutine()
    }

    private fun initRecyclerView() = findViewById<RecyclerView>(R.id.recycler_view).run {
        adapter = permissionsAdapter
        addItemDecoration(
            DividerItemDecoration(
                this@ManagePermissionsActivity,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun launchPermissionsListCoroutine() = lifecycleScope.launchWhenStarted {
        viewModel.accessResolutions
            .mapToDisplayDataResult()
            .collect { display(it) }
    }

    private fun Flow<Result<List<Pair<String, Boolean>>>?>.mapToDisplayDataResult(): Flow<Result<List<AppPermissionDisplayData>>?> =
        mapLatest { result ->
            if (result == null)
                null
            else result.fold(
                onFailure = {
                    @Suppress("UNCHECKED_CAST")
                    result as Result<List<AppPermissionDisplayData>>
                },
                onSuccess = { list ->
                    try {
                        Result.success(
                            withContext(Dispatchers.Default) {
                                list.toDisplayDataList().filterNotNull().sorted()
                            }
                        )
                    } catch (throwable: Throwable) {
                        Result.failure(throwable)
                    }
                }
            )
        }

    private suspend fun List<Pair<String, Boolean>>.toDisplayDataList(): List<AppPermissionDisplayData?> =
        coroutineScope {
            map { async { it.toDisplayData() } }.awaitAll()
        }

    private suspend fun Pair<String, Boolean>.toDisplayData(): AppPermissionDisplayData? =
        try {
            coroutineScope {
                val iconJob = async(Dispatchers.IO) {
                    packageManager.getApplicationInfo(first, 0).loadIcon(packageManager)
                }
                val name = withContext(Dispatchers.IO) {
                    packageManager.resolveAppName(first).toString()
                }
                AppPermissionDisplayData(
                    appIcon = iconJob.await(),
                    appName = name,
                    permissionGranted = second,
                    packageName = first
                )
            }
        } catch (packageUninstalledException: PackageManager.NameNotFoundException) {
            null
        }

    private fun display(result: Result<List<AppPermissionDisplayData>>?) {
        if (result == null)
            return
        result.fold(
            onSuccess = { list ->
                permissionsAdapter.set(permissionsList = list)
                findViewById<TextView>(R.id.empty_or_error_view).run {
                    if (list.isEmpty()) {
                        text = getString(
                            R.string.manage_permissions_no_clients,
                            getString(R.string.app_name)
                        )
                        visibility = View.VISIBLE
                    } else
                        visibility = View.INVISIBLE
                }
            },
            onFailure = { throwable ->
                Timber.e(throwable)
                permissionsAdapter.set(permissionsList = emptyList())
                findViewById<TextView>(R.id.empty_or_error_view).run {
                    text = getString(
                        R.string.manage_permissions_loading_failed,
                        throwable.localizedMessage
                    )
                    visibility = View.VISIBLE
                }
            }
        )
    }

    private fun obtainViewModel(): ManagePermissionsViewModel {
        @Suppress("UNCHECKED_CAST")
        val viewModelProvider = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                PermissionsComponent.instance.injectManagePermissionsViewModel() as T
        })
        return viewModelProvider.get(ManagePermissionsViewModel::class.java)
    }

    private fun displayErrorMessage(throwable: Throwable) {
        Timber.e(throwable)
        AlertDialog.Builder(this)
            .setMessage(
                getString(
                    R.string.manage_permissions_change_failed,
                    throwable.localizedMessage
                )
            )
            .setNeutralButton(R.string.generic_close_button, null)
            .show()
    }
}

private class AppPermissionDisplayData(
    val appIcon: Drawable,
    val appName: String,
    val permissionGranted: Boolean,
    val packageName: String
)

private fun List<AppPermissionDisplayData>.sorted(): List<AppPermissionDisplayData> =
     sortedWith(Comparator { a, b -> a.appName.compareTo(b.appName, ignoreCase = true) })
