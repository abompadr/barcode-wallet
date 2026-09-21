package com.barcodewallet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barcodewallet.app.data.BarcodeItem
import com.barcodewallet.app.data.PdfItem
import com.barcodewallet.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                BarcodeWalletApp()
            }
        }
    }
}

private sealed interface BarcodeScreen {
    data object Home : BarcodeScreen
    data object Add : BarcodeScreen
    data class Display(val item: BarcodeItem) : BarcodeScreen
}

private sealed interface DocumentScreen {
    data object List : DocumentScreen
    data object Add : DocumentScreen
    data class View(val item: PdfItem) : DocumentScreen
}

@Composable
private fun BarcodeWalletApp(vm: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var barcodeScreen by remember { mutableStateOf<BarcodeScreen>(BarcodeScreen.Home) }
    var documentScreen by remember { mutableStateOf<DocumentScreen>(DocumentScreen.List) }

    val showBottomBar = barcodeScreen is BarcodeScreen.Home && documentScreen is DocumentScreen.List

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                        label = { Text("Barcodes") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Description, contentDescription = null) },
                        label = { Text("Documents") }
                    )
                }
            }
        }
    ) { _ ->
        when (selectedTab) {
            0 -> when (val s = barcodeScreen) {
                is BarcodeScreen.Home -> HomeScreen(
                    viewModel = vm,
                    onAdd = { barcodeScreen = BarcodeScreen.Add },
                    onDisplay = { barcodeScreen = BarcodeScreen.Display(it) }
                )
                is BarcodeScreen.Add -> AddScreen(
                    viewModel = vm,
                    onBack = { barcodeScreen = BarcodeScreen.Home }
                )
                is BarcodeScreen.Display -> DisplayScreen(
                    item = s.item,
                    onBack = { barcodeScreen = BarcodeScreen.Home }
                )
            }
            1 -> when (val s = documentScreen) {
                is DocumentScreen.List -> DocumentsScreen(
                    viewModel = vm,
                    onAdd = { documentScreen = DocumentScreen.Add },
                    onOpen = { documentScreen = DocumentScreen.View(it) }
                )
                is DocumentScreen.Add -> AddDocumentScreen(
                    viewModel = vm,
                    onBack = { documentScreen = DocumentScreen.List }
                )
                is DocumentScreen.View -> PdfViewerScreen(
                    item = s.item,
                    onBack = { documentScreen = DocumentScreen.List }
                )
            }
        }
    }
}
