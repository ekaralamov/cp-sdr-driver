package sdr.driver.cp.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

class ViewModelTestContainer(viewModel: ViewModel) {

    private val viewModelStore = ViewModelStore()
    private var cleared = false

    init {
        ViewModelProvider(
            viewModelStore,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel?> create(modelClass: Class<T>) = viewModel as T
            }
        ).get(ViewModel::class.java)
    }

    fun clear() {
        viewModelStore.clear()
        cleared = true
    }

    fun close() {
        if (!cleared)
            viewModelStore.clear()
    }
}
