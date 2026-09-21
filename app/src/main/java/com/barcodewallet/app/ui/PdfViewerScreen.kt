package com.barcodewallet.app.ui

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.barcodewallet.app.data.PdfItem
import java.io.File
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(item: PdfItem, onBack: () -> Unit) {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        val original = window?.attributes?.screenBrightness ?: -1f
        window?.attributes = window?.attributes?.also { it.screenBrightness = 1f }
        onDispose {
            window?.attributes = window?.attributes?.also { it.screenBrightness = original }
        }
    }

    val pages = remember(item.filePath) {
        renderPdfPages(item.filePath)
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
        }
    ) { padding ->
        if (pages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cannot display this PDF.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(pages) { _, bmp ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun renderPdfPages(filePath: String): List<Bitmap> {
    return try {
        val file = File(filePath)
        if (!file.exists()) return emptyList()
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        val bitmaps = mutableListOf<Bitmap>()
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val scale = 2f
            val bmp = Bitmap.createBitmap(
                (page.width * scale).toInt(),
                (page.height * scale).toInt(),
                Bitmap.Config.ARGB_8888
            )
            bmp.eraseColor(android.graphics.Color.WHITE)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmaps.add(bmp)
        }
        renderer.close()
        fd.close()
        bitmaps
    } catch (e: Exception) {
        emptyList()
    }
}
