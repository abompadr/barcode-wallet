package com.barcodewallet.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.barcodewallet.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isProtected by remember { mutableStateOf(false) }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            val rawName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
            selectedFileName = rawName
            // Pre-fill name from filename (strip .pdf extension) if user hasn't typed one yet
            if (name.isBlank()) {
                name = rawName.removeSuffix(".pdf")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_document)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.document_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { pdfPicker.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedUri == null) stringResource(R.string.choose_pdf) else selectedFileName)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.protect_with_password),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(checked = isProtected, onCheckedChange = { isProtected = it })
                }
                if (isProtected) {
                    Text(
                        "This document will be stored in the protected section. " +
                            "A password is required to view it. " +
                            "If you haven't set a password yet, you will be asked to create one " +
                            "the first time you open the protected section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.addPdf(name, selectedUri!!, isProtected)
                    onBack()
                },
                enabled = name.isNotBlank() && selectedUri != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
