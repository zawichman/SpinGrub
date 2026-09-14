package com.spingrub.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.spingrub.app.data.Category
import com.spingrub.app.data.SpinGrubData
import com.spingrub.app.ui.theme.SegmentColors
import com.spingrub.app.util.Feedback
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SpinnerScreen(
    data: SpinGrubData,
    onSaveFavorite: (name: String, meat: String, method: String, sauce: String) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // One WheelState per category, retained across recompositions.
    val wheelStates = remember { Category.ordered.associateWith { WheelState() } }

    // Landed results per category (null until settled at least once).
    var results by remember { mutableStateOf<Map<Category, String?>>(Category.ordered.associateWith { null }) }
    var showResult by remember { mutableStateOf(false) }
    var spinningCount by remember { mutableStateOf(0) }
    var playConfetti by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var favName by remember { mutableStateOf("") }

    fun itemsFor(c: Category) = data.itemsFor(c)

    fun onOneSettled(c: Category, index: Int) {
        val items = itemsFor(c)
        val value = items.getOrNull(index)
        results = results.toMutableMap().apply { put(c, value) }
        spinningCount = (spinningCount - 1).coerceAtLeast(0)
        if (data.hapticsEnabled) Feedback.tick(context)
        if (spinningCount == 0) {
            showResult = true
            if (data.confettiEnabled) {
                playConfetti = false
                playConfetti = true
            }
        }
    }

    fun spinAll() {
        showResult = false
        playConfetti = false
        spinningCount = Category.ordered.count { itemsFor(it).isNotEmpty() }
        if (spinningCount == 0) return
        Category.ordered.forEach { c ->
            val items = itemsFor(c)
            if (items.isEmpty()) return@forEach
            val state = wheelStates.getValue(c)
            // Each wheel gets a similar-but-random power.
            val velocity = 1600f + Random.nextFloat() * 2200f
            scope.launch {
                state.spin(
                    velocityDegPerSec = velocity,
                    segmentCount = items.size,
                    onSettled = { idx -> onOneSettled(c, idx) },
                    onTick = { if (data.hapticsEnabled) Feedback.tick(context) }
                )
            }
        }
    }

    val allLanded = Category.ordered.all { results[it] != null }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "SpinGrub",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
            )
            Text(
                "Swipe a wheel, or hit Spin All!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(12.dp))

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Category.ordered.forEach { c ->
                        Box(modifier = Modifier.weight(1f)) {
                            WheelBlock(c, itemsFor(c), wheelStates.getValue(c),
                                enabled = spinningCount == 0,
                                onSpinStart = { showResult = false; playConfetti = false; spinningCount++ },
                                onSettled = { idx -> onOneSettled(c, idx) },
                                onTick = { if (data.hapticsEnabled) Feedback.tick(context) })
                        }
                    }
                }
            } else {
                Category.ordered.forEach { c ->
                    WheelBlock(c, itemsFor(c), wheelStates.getValue(c),
                        enabled = spinningCount == 0,
                        onSpinStart = { showResult = false; playConfetti = false; spinningCount++ },
                        onSettled = { idx -> onOneSettled(c, idx) },
                        onTick = { if (data.hapticsEnabled) Feedback.tick(context) })
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (spinningCount == 0) spinAll() },
                enabled = spinningCount == 0,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Filled.Casino, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SPIN ALL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    favName = ""
                    showSaveDialog = true
                },
                enabled = allLanded && spinningCount == 0,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Favorite this combo")
            }
            Spacer(Modifier.height(24.dp))
        }

        // Animated result title box overlays the wheels (top-center).
        ResultTitleBox(
            visible = showResult && spinningCount == 0,
            title = results[Category.MEAT]?.let { meat ->
                "$meat ${results[Category.METHOD].orEmpty()}"
            }.orEmpty().trim(),
            subtitle = "You got",
            recipe = if (allLanded) Category.ordered.map { results[it].orEmpty() } else null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(top = 80.dp),
        )

        ConfettiOverlay(play = playConfetti)
    }

    if (showSaveDialog) {
        val meat = results[Category.MEAT].orEmpty()
        val method = results[Category.METHOD].orEmpty()
        val sauce = results[Category.SAUCE].orEmpty()
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Name this combo") },
            text = {
                Column {
                    Text("$meat • $method • $sauce", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = favName,
                        onValueChange = { favName = it },
                        placeholder = { Text("e.g. Friday Feast") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveFavorite(favName, meat, method, sauce)
                    showSaveDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WheelBlock(
    category: Category,
    items: List<String>,
    wheelState: WheelState,
    enabled: Boolean,
    onSpinStart: () -> Unit,
    onSettled: (Int) -> Unit,
    onTick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "${category.emoji} ${category.title}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(4.dp))
        if (items.isEmpty()) {
            Text(
                "No items — add some in Setup!",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            SpinnerWheel(
                items = items,
                wheelState = wheelState,
                colors = SegmentColors,
                enabled = enabled,
                onSpinStart = onSpinStart,
                onSettled = onSettled,
                onTick = onTick,
            )
        }
    }
}
