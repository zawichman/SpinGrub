package com.spingrub.app.ui

import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Controls the rotation state of a wheel and exposes spin() with fling physics.
 * angle is stored in degrees. The pointer sits at the top (12 o'clock).
 */
class WheelState {
    var angle by mutableFloatStateOf(Random.nextFloat() * 360f)
        internal set

    /** True while a momentum spin animation is running. Manual dragging is always allowed. */
    var spinning by androidx.compose.runtime.mutableStateOf(false)
        internal set

    /**
     * Spin with a given velocity (degrees/second). Applies exponential
     * deceleration so higher velocity => longer, faster spin.
     * onSettled is invoked with the index the pointer lands on.
     */
    suspend fun spin(
        velocityDegPerSec: Float,
        segmentCount: Int,
        onSettled: (Int) -> Unit,
        onTick: () -> Unit = {}
    ) {
        if (segmentCount <= 0) return
        spinning = true
        val v0 = velocityDegPerSec.coerceIn(360f, 4200f)
        // Deceleration (deg/s^2). Larger => stops sooner.
        val decel = 900f
        val start = angle
        // total travel distance from kinematics: v^2 / (2a)
        val travel = (v0 * v0) / (2f * decel)
        val duration = (v0 / decel).coerceIn(1.2f, 6f) // seconds
        var lastTickAngle = start
        animate(
            initialValue = 0f,
            targetValue = travel,
            initialVelocity = v0,
            typeConverter = Float.VectorConverter,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = (duration * 1000).toInt(),
                easing = { fraction ->
                    // decelerate easing: 1 - (1-t)^2
                    1f - (1f - fraction) * (1f - fraction)
                }
            )
        ) { value, _ ->
            angle = (start + value) % 360f
            // haptic-style tick each segment boundary crossing
            if (kotlin.math.abs(angle - lastTickAngle) >= 360f / segmentCount) {
                lastTickAngle = angle
                onTick()
            }
        }
        spinning = false
        onSettled(indexAtPointer(segmentCount))
    }

    /** Which segment index is currently under the top pointer. */
    fun indexAtPointer(segmentCount: Int): Int {
        if (segmentCount <= 0) return 0
        val seg = 360f / segmentCount
        // Segment 0 is drawn starting at -90 (top) going clockwise.
        // Pointer is fixed at top; wheel rotates by `angle`.
        val normalized = ((-angle) % 360f + 360f) % 360f
        return (normalized / seg).toInt() % segmentCount
    }
}

/**
 * A colorful wheel with labeled segments. Supports swipe-to-spin (velocity based)
 * and programmatic spins. [enabled] gates gestures.
 */
@Composable
fun SpinnerWheel(
    items: List<String>,
    wheelState: WheelState,
    modifier: Modifier = Modifier,
    colors: List<Color>,
    enabled: Boolean = true,
    onSpinStart: () -> Unit = {},
    onSettled: (Int) -> Unit = {},
    onTick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val segCount = items.size
    // Keep the latest gating flag without re-keying pointerInput (which would
    // cancel an in-progress drag). Read .value inside gesture callbacks.
    val enabledState = androidx.compose.runtime.rememberUpdatedState(enabled)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            // Key ONLY on items so the gesture detector is never torn down
            // mid-drag when `enabled` toggles. That teardown was the freeze bug.
            .pointerInput(items) {
                if (segCount == 0) return@pointerInput
                val center = Offset(size.width / 2f, size.height / 2f)
                var lastAngleDeg = 0f
                var lastTimeNs = 0L
                var velDegPerSec = 0f
                var dragging = false
                detectDragGestures(
                    onDragStart = { pos ->
                        // Ignore new drags while a momentum spin is animating,
                        // or when the parent has disabled interaction.
                        if (wheelState.spinning || !enabledState.value) {
                            dragging = false
                        } else {
                            dragging = true
                            onSpinStart()
                            lastAngleDeg = angleFromCenter(pos, center)
                            lastTimeNs = System.nanoTime()
                            velDegPerSec = 0f
                        }
                    },
                    onDrag = { change, _ ->
                        if (dragging) {
                            val a = angleFromCenter(change.position, center)
                            var delta = a - lastAngleDeg
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f
                            wheelState.angle = (wheelState.angle + delta + 360f) % 360f

                            val now = System.nanoTime()
                            val dt = (now - lastTimeNs) / 1_000_000_000f
                            if (dt > 0f) {
                                val instant = delta / dt
                                // smooth so the fling reflects recent motion, not one jittery frame
                                velDegPerSec = velDegPerSec * 0.6f + instant * 0.4f
                            }
                            lastAngleDeg = a
                            lastTimeNs = now
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        val speed = kotlin.math.abs(velDegPerSec)
                        if (dragging && speed >= 120f) {
                            scope.launch {
                                wheelState.spin(
                                    velocityDegPerSec = speed,
                                    segmentCount = segCount,
                                    onSettled = onSettled,
                                    onTick = onTick,
                                )
                            }
                        }
                        dragging = false
                    },
                    onDragCancel = { dragging = false }
                )
            }
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            drawWheel(items, wheelState.angle, colors)
        }
        // Fixed pointer at top
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            drawPointer()
        }
    }
}

private fun angleFromCenter(pos: Offset, center: Offset): Float {
    val dx = pos.x - center.x
    val dy = pos.y - center.y
    return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
}

private fun DrawScope.drawWheel(
    items: List<String>,
    angle: Float,
    colors: List<Color>,
) {
    val count = items.size
    if (count == 0) return
    val diameter = size.minDimension
    val radius = diameter / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val sweep = 360f / count

    rotate(degrees = angle, pivot = center) {
        for (i in 0 until count) {
            val startAngle = -90f + i * sweep
            drawArc(
                color = colors[i % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(diameter, diameter)
            )
            // separators
            val rad = Math.toRadians((startAngle).toDouble())
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    center.x + radius * cos(rad).toFloat(),
                    center.y + radius * sin(rad).toFloat()
                ),
                strokeWidth = 3f
            )
        }
        // labels drawn along each segment mid-angle using native canvas
        val nativeCanvas = drawContext.canvas.nativeCanvas
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = (radius * 0.10f).coerceIn(22f, 60f)
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
            isFakeBoldText = true
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.argb(140, 0, 0, 0))
        }
        for (i in 0 until count) {
            val mid = -90f + (i + 0.5f) * sweep
            nativeCanvas.save()
            nativeCanvas.translate(center.x, center.y)
            nativeCanvas.rotate(mid)
            val label = items[i]
            val maxChars = 14
            val text = if (label.length > maxChars) label.take(maxChars - 1) + "…" else label
            nativeCanvas.drawText(text, radius * 0.92f, paint.textSize / 3f, paint)
            nativeCanvas.restore()
        }
    }

    // Outer ring
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center,
        style = Stroke(width = radius * 0.06f)
    )
    // Hub
    drawCircle(color = Color.White, radius = radius * 0.14f, center = center)
    drawCircle(color = Color(0xFF2B2D42), radius = radius * 0.10f, center = center)
}

private fun DrawScope.drawPointer() {
    val diameter = size.minDimension
    val radius = diameter / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val topY = center.y - radius
    val w = radius * 0.10f
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x - w, topY - w)
        lineTo(center.x + w, topY - w)
        lineTo(center.x, topY + w * 1.6f)
        close()
    }
    drawPath(path, color = Color(0xFF2B2D42))
    drawPath(
        path,
        color = Color.White,
        style = Stroke(width = 4f)
    )
}
