package com.barcodewallet.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.barcodewallet.app.data.BarcodeItem
import com.barcodewallet.app.util.BarcodeRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(item: BarcodeItem, onBack: () -> Unit) {
    val view = LocalView.current

    // Boost brightness while this screen is shown
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        val original = window?.attributes?.screenBrightness ?: -1f
        window?.attributes = window?.attributes?.also { it.screenBrightness = 1f }
        onDispose {
            window?.attributes = window?.attributes?.also { it.screenBrightness = original }
        }
    }

    val bitmap: Bitmap? = remember(item.value, item.format) {
        BarcodeRenderer.render(item.value, item.format, 900, 300)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(160.dp)
                    )
                } ?: Text("Cannot render barcode for this value.")
                Spacer(Modifier.height(16.dp))
                Text(item.value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
