package com.barcodewallet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barcodewallet.app.data.BarcodeItem
import com.barcodewallet.app.ui.AddScreen
import com.barcodewallet.app.ui.BarcodeViewModel
import com.barcodewallet.app.ui.DisplayScreen
import com.barcodewallet.app.ui.HomeScreen

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

private sealed interface Screen {
    data object Home : Screen
    data object Add : Screen
    data class Display(val item: BarcodeItem) : Screen
}

@Composable
private fun BarcodeWalletApp(vm: BarcodeViewModel = viewModel()) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (val s = screen) {
        is Screen.Home -> HomeScreen(
            viewModel = vm,
            onAdd = { screen = Screen.Add },
            onDisplay = { screen = Screen.Display(it) }
        )
        is Screen.Add -> AddScreen(
            viewModel = vm,
            onBack = { screen = Screen.Home }
        )
        is Screen.Display -> DisplayScreen(
            item = s.item,
            onBack = { screen = Screen.Home }
        )
    }
}
