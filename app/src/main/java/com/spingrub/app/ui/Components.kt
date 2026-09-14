package com.spingrub.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.spingrub.app.R

/**
 * Animated title box that pops in with a spring when [visible] and text is present.
 * Optionally shows a recipe (list of items) beneath the title.
 */
@Composable
fun ResultTitleBox(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    recipe: List<String>? = null,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && title.isNotBlank(),
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn() + slideInVertically { it / 3 },
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                )
                recipe?.let {
                    Text(
                        text = it.joinToString("  •  "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Full-screen confetti burst that plays once when [play] flips true. */
@Composable
fun ConfettiOverlay(play: Boolean) {
    if (!play) return
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti)
    )
    val composition = compositionResult.value
    val progressState = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = play,
        iterations = 1,
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LottieAnimation(
            composition = composition,
            progress = { progressState.value },
            modifier = Modifier.fillMaxSize()
        )
    }
}
