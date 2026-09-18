package com.barcodewallet.app.ui

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalGetImage::class)
@androidx.compose.runtime.Composable
fun BarcodeScannerScreen(onScanned: (value: String, format: String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanned = remember { AtomicBoolean(false) }

    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !scanned.get()) {
                            val image = InputImage.fromMediaImage(
                                mediaImage, imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstOrNull()?.let { barcode ->
                                        barcode.rawValue?.let { value ->
                                            if (scanned.compareAndSet(false, true)) {
                                                val format = mlKitFormatToZxing(barcode.format)
                                                onScanned(value, format)
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }
        Text(
            "Point camera at barcode",
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            color = androidx.compose.ui.graphics.Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun mlKitFormatToZxing(format: Int): String = when (format) {
    Barcode.FORMAT_CODE_128   -> "CODE_128"
    Barcode.FORMAT_CODE_39    -> "CODE_39"
    Barcode.FORMAT_CODE_93    -> "CODE_93"
    Barcode.FORMAT_EAN_8      -> "EAN_8"
    Barcode.FORMAT_EAN_13     -> "EAN_13"
    Barcode.FORMAT_UPC_A      -> "UPC_A"
    Barcode.FORMAT_UPC_E      -> "UPC_E"
    Barcode.FORMAT_QR_CODE    -> "QR_CODE"
    Barcode.FORMAT_PDF417     -> "PDF_417"
    Barcode.FORMAT_AZTEC      -> "AZTEC"
    Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
    Barcode.FORMAT_ITF        -> "ITF"
    else                      -> "CODE_128"
}
