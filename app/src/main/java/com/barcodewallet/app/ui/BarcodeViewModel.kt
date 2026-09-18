package com.barcodewallet.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.barcodewallet.app.data.BarcodeDatabase
import com.barcodewallet.app.data.BarcodeItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarcodeViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = BarcodeDatabase.getInstance(app).barcodeDao()

    val barcodes = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun add(name: String, value: String, format: String = "CODE_128") {
        viewModelScope.launch {
            dao.insert(BarcodeItem(name = name.trim(), value = value.trim(), format = format))
        }
    }

    fun delete(item: BarcodeItem) {
        viewModelScope.launch { dao.delete(item) }
    }
}
