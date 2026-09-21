package com.barcodewallet.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.barcodewallet.app.R
import com.barcodewallet.app.data.PdfItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DocumentsScreen(
    viewModel: MainViewModel,
    onOpen: (PdfItem) -> Unit,
    outerPadding: PaddingValues = PaddingValues()
) {
    val pdfs by viewModel.pdfs.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()

    val unprotected = pdfs.filter { !it.protected }
    val protected = pdfs.filter { it.protected }

    // Dialog state
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingDelete by remember { mutableStateOf<PdfItem?>(null) }
    var pendingRename by remember { mutableStateOf<PdfItem?>(null) }
    var renameText by remember { mutableStateOf("") }

    // Unlock / set-password dialogs
    if (showSetPasswordDialog) {
        SetPasswordDialog(
            onConfirm = { pw ->
                viewModel.setPassword(pw)
                viewModel.unlock(pw)
                showSetPasswordDialog = false
                pendingAction?.invoke()
                pendingAction = null
            },
            onDismiss = { showSetPasswordDialog = false; pendingAction = null }
        )
    }

    if (showUnlockDialog) {
        UnlockDialog(
            onConfirm = { pw ->
                val ok = viewModel.unlock(pw)
                if (ok) {
                    showUnlockDialog = false
                    pendingAction?.invoke()
                    pendingAction = null
                }
                ok
            },
            onDismiss = { showUnlockDialog = false; pendingAction = null }
        )
    }

    // Delete confirmation
    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePdf(item); pendingDelete = null }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Rename dialog
    pendingRename?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingRename = null },
            title = { Text(stringResource(R.string.rename)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.document_name_hint)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) viewModel.renamePdf(item, renameText)
                    pendingRename = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRename = null }) { Text("Cancel") }
            }
        )
    }

    fun requireUnlock(action: () -> Unit) {
        if (isUnlocked) { action(); return }
        pendingAction = action
        if (viewModel.hasPassword()) showUnlockDialog = true else showSetPasswordDialog = true
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.documents)) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                bottom = maxOf(80.dp, outerPadding.calculateBottomPadding())
            )
        ) {
            // ── Unprotected section ───────────────────────────────────────────
            if (unprotected.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.documents_section))
                }
                items(unprotected, key = { it.id }) { pdf ->
                    PdfCard(
                        item = pdf,
                        onClick = { onOpen(pdf) },
                        onLongClick = {
                            renameText = pdf.name
                            pendingRename = pdf
                        },
                        onDelete = { pendingDelete = pdf }
                    )
                }
            }

            // ── Protected section ─────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.protected_section),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnlocked) {
                        TextButton(onClick = { viewModel.lock() }) {
                            Icon(Icons.Default.Lock, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.lock))
                        }
                    }
                }
            }

            if (isUnlocked) {
                if (protected.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_protected_documents),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(protected, key = { it.id }) { pdf ->
                        PdfCard(
                            item = pdf,
                            onClick = { onOpen(pdf) },
                            onLongClick = {
                                renameText = pdf.name
                                pendingRename = pdf
                            },
                            onDelete = { pendingDelete = pdf }
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .combinedClickable(onClick = { requireUnlock {} }),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.tap_to_unlock),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            if (unprotected.isEmpty() && pdfs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_documents),
                            style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PdfCard(
    item: PdfItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PDF",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ── Password dialogs ──────────────────────────────────────────────────────────

@Composable
private fun UnlockDialog(
    onConfirm: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_password)) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = false },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                    label = { Text(stringResource(R.string.enter_password)) },
                    isError = error
                )
                if (error) {
                    Text(
                        stringResource(R.string.wrong_password),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!onConfirm(password)) error = true
            }) { Text(stringResource(R.string.unlock)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SetPasswordDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = "" },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                    label = { Text(stringResource(R.string.set_password)) }
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = "" },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                    label = { Text(stringResource(R.string.confirm_password)) },
                    isError = error.isNotEmpty()
                )
                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    password.isBlank() -> error = "Password cannot be empty"
                    password != confirm -> error = "Passwords do not match"
                    else -> onConfirm(password)
                }
            }) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
