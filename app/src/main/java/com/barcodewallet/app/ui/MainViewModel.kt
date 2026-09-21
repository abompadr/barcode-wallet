package com.barcodewallet.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.barcodewallet.app.data.BarcodeDatabase
import com.barcodewallet.app.data.BarcodeItem
import com.barcodewallet.app.data.PdfItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BarcodeDatabase.getInstance(app)
    private val barcodeDao = db.barcodeDao()
    private val pdfDao = db.pdfDao()

    // ── Barcodes ──────────────────────────────────────────────────────────────
    val barcodes = barcodeDao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addBarcode(name: String, value: String, format: String = "CODE_128") {
        viewModelScope.launch {
            barcodeDao.insert(BarcodeItem(name = name.trim(), value = value.trim(), format = format))
        }
    }

    fun deleteBarcode(item: BarcodeItem) {
        viewModelScope.launch { barcodeDao.delete(item) }
    }

    // ── PDFs ──────────────────────────────────────────────────────────────────
    val pdfs = pdfDao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addPdf(name: String, uri: Uri, isProtected: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val dir = File(app.filesDir, "pdfs").also { it.mkdirs() }
            val dest = File(dir, "${UUID.randomUUID()}.pdf")
            app.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { input.copyTo(it) }
            }
            pdfDao.insert(PdfItem(name = name.trim(), filePath = dest.absolutePath, protected = isProtected))
        }
    }

    fun deletePdf(item: PdfItem) {
        viewModelScope.launch(Dispatchers.IO) {
            File(item.filePath).delete()
            pdfDao.delete(item)
        }
    }

    fun renamePdf(item: PdfItem, newName: String) {
        viewModelScope.launch { pdfDao.update(item.copy(name = newName.trim())) }
    }

    // ── Password & session unlock ─────────────────────────────────────────────
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    private fun prefs() = EncryptedSharedPreferences.create(
        getApplication(),
        "secure_prefs",
        MasterKey.Builder(getApplication())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasPassword(): Boolean = prefs().contains("pw_hash")

    fun setPassword(password: String) {
        prefs().edit().putString("pw_hash", sha256(password)).apply()
    }

    fun unlock(password: String): Boolean {
        val ok = prefs().getString("pw_hash", null) == sha256(password)
        if (ok) _isUnlocked.value = true
        return ok
    }

    fun lock() { _isUnlocked.value = false }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
