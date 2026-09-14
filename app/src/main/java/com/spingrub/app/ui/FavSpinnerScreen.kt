package com.spingrub.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spingrub.app.data.Favorite
import com.spingrub.app.data.SpinGrubData
import com.spingrub.app.ui.theme.SegmentColors
import com.spingrub.app.util.Feedback
import com.spingrub.app.util.SoundFx

@Composable
fun FavSpinnerScreen(data: SpinGrubData) {
    val favorites = data.favorites
    val context = LocalContext.current
    val wheelState = remember { WheelState() }

    var landedIndex by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                "Favorites Wheel",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Black,
            )
            Text(
                "Swipe to spin your saved combos!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(24.dp))

            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No favorites yet!\nSpin the main wheels and tap \"Favorite this combo\" to save one. ⭐",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(fraction = 0.9f),
                    contentAlignment = Alignment.Center
                ) {
                    SpinnerWheel(
                        items = favorites.map { it.name },
                        wheelState = wheelState,
                        colors = SegmentColors,
                        enabled = !wheelState.spinning,
                        onSpinStart = { showResult = false },
                        onSettled = { idx ->
                            landedIndex = idx
                            showResult = true
                            if (data.hapticsEnabled) Feedback.tick(context)
                            SoundFx.ding(data.soundEnabled)
                        },
                        onTick = {
                            if (data.hapticsEnabled) Feedback.tick(context)
                            SoundFx.tick(data.soundEnabled)
                        },
                    )
                }
            }
        }

        val landed: Favorite? = landedIndex?.let { favorites.getOrNull(it) }
        ResultTitleBox(
            visible = showResult && landed != null,
            title = landed?.name.orEmpty(),
            subtitle = "Tonight's pick",
            recipe = landed?.recipe,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )
    }
}
