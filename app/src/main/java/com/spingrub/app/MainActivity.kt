package com.spingrub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.spingrub.app.ui.FavSpinnerScreen
import com.spingrub.app.ui.SetupScreen
import com.spingrub.app.ui.SpinnerScreen
import com.spingrub.app.ui.theme.SpinGrubTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SpinGrubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpinGrubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpinGrubApp(viewModel)
                }
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    SPINNER("Spinner", Icons.Filled.Casino),
    FAV("Fav Spinner", Icons.Filled.Star),
    SETUP("Setup", Icons.Filled.Settings),
}

@Composable
private fun SpinGrubApp(viewModel: SpinGrubViewModel) {
    val data by viewModel.state.collectAsState()
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (Tab.entries[selected]) {
                Tab.SPINNER -> SpinnerScreen(
                    data = data,
                    onSaveFavorite = viewModel::saveFavorite,
                )
                Tab.FAV -> FavSpinnerScreen(data = data)
                Tab.SETUP -> SetupScreen(
                    data = data,
                    onAddItem = viewModel::addItem,
                    onRemoveItem = viewModel::removeItem,
                    onToggleSound = viewModel::setSound,
                    onToggleHaptics = viewModel::setHaptics,
                    onToggleConfetti = viewModel::setConfetti,
                    onReset = viewModel::resetToDefaults,
                )
            }
        }
    }
}
