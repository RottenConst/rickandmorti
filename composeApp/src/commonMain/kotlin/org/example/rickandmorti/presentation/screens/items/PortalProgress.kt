package org.example.rickandmorti.presentation.screens.items

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PortalProgress(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {

    val infiniteTransition = rememberInfiniteTransition(
        label = "portal"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6000,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    val noisePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            )
        ),
        label = "noise"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
        ) {

            val center = this.size.center

            drawCircle(
                color = Color(0xFF76FF03).copy(alpha = 0.15f),
                radius = this.size.minDimension * 0.55f,
                center = center
            )

            drawCircle(
                color = Color(0xFFB2FF59).copy(alpha = 0.1f),
                radius = this.size.minDimension * 0.42f,
                center = center
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {

            val center = this.size.center

            repeat(4) { layer ->

                val radius =
                    this.size.minDimension *
                            (0.42f - layer * 0.05f)

                val path = createPortalPath(
                    center = center,
                    radius = radius,
                    phase = noisePhase + layer
                )

                drawPath(
                    path = path,
                    color = when (layer) {
                        0 -> Color(0xFFD4FF00)
                        1 -> Color(0xFF9EFF00)
                        2 -> Color(0xFF5CFF43)
                        else -> Color(0xFF00E676)
                    },
                    style = Stroke(
                        width = (12f - layer * 2f).dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            repeat(60) { particle ->
                val angle = particle * 6f + rotation
                val radius = this.size.minDimension * 0.42f + (abs(sin(noisePhase + particle)) * 14f)
                val x = center.x + cos(angle.toRadians()) * radius
                val y = center.y + sin(angle.toRadians()) * radius

                drawCircle(
                    color = listOf(
                        Color(0xFFD4FF00),
                        Color(0xFFB2FF59),
                        Color(0xFF76FF03)
                    )[particle % 3],
                    radius = 2f + (abs(sin(noisePhase + particle)) * 3f),
                    center = Offset(x, y)
                )
            }

            drawCircle(
                color = Color(0xFF103000),
                radius = this.size.minDimension * 0.22f,
                center = center
            )

            drawCircle(
                color = Color(0xFF1B5E20),
                radius = this.size.minDimension * 0.18f,
                center = center
            )
        }
    }
}

private fun createPortalPath(
    center: Offset,
    radius: Float,
    phase: Float
): Path {

    val path = Path()
    val points = 180

    for (i in 0..points) {

        val angle = (i.toFloat() / points) * (PI * 2).toFloat()
        val distortion =
            sin(angle * 5f + phase) * 10f +
                    cos(angle * 11f - phase * 1.2f) * 7f +
                    sin(angle * 19f + phase * 0.8f) * 5f +
                    cos(angle * 31f + phase * 1.7f) * 3f

        val finalRadius = radius + distortion
        val x = center.x + cos(angle) * finalRadius
        val y = center.y + sin(angle) * finalRadius
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    path.close()
    return path
}

private fun Float.toRadians(): Float =
    (this / 180f * PI).toFloat()

@Preview
@Composable
fun PortalProgressPreview() {
    Surface(
        color = Color(0xFF151515)
    ) {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PortalProgress()
        }
    }
}

@Preview
@Composable
fun PortalProgressSizesPreview() {
    Surface(
        color = Color(0xFF151515)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PortalProgress(size = 40.dp)
            PortalProgress(size = 80.dp)
            PortalProgress(size = 120.dp)
        }
    }
}
