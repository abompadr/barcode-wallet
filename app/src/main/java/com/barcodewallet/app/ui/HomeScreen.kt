package com.barcodewallet.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.barcodewallet.app.R
import com.barcodewallet.app.data.BarcodeItem
import com.barcodewallet.app.util.BarcodeRenderer

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: BarcodeViewModel,
    onAdd: () -> Unit,
    onDisplay: (BarcodeItem) -> Unit
) {
    val barcodes by viewModel.barcodes.collectAsState()
    var pendingDelete by remember { mutableStateOf<BarcodeItem?>(null) }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(item)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_barcode))
            }
        }
    ) { padding ->
        if (barcodes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_barcodes), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(barcodes, key = { it.id }) { item ->
                    BarcodeCard(
                        item = item,
                        onClick = { onDisplay(item) },
                        onLongClick = { pendingDelete = item }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BarcodeCard(
    item: BarcodeItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bitmap: Bitmap? = remember(item.value) {
        BarcodeRenderer.render(item.value, 600, 150)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
            } ?: Text("(invalid barcode)", style = MaterialTheme.typography.bodySmall)
        }
    }
}
