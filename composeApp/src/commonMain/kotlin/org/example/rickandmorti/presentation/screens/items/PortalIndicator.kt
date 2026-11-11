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
import androidx.compose.runtime.remember
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
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

private const val PathPoints = 90
private const val ParticleCount = 36
private const val DegreesPerParticle = 360f / ParticleCount

private val ParticleColors = listOf(
    Color(0xFFD4FF00),
    Color(0xFFB2FF59),
    Color(0xFF76FF03)
)

private class PortalPathCache(
    val cos: FloatArray,
    val sin: FloatArray,
    val angles: FloatArray
)

@Composable
fun PortalIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    isAnimated: Boolean = true
) {
    if (isAnimated) {
        AnimatedPortalIndicator(modifier = modifier, size = size)
    } else {
        StaticPortalIndicator(modifier = modifier, size = size)
    }
}

@Composable
private fun AnimatedPortalIndicator(
    modifier: Modifier,
    size: Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portal")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val noisePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing)
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

    PortalContent(
        modifier = modifier,
        size = size,
        rotation = rotation,
        noisePhase = noisePhase,
        pulse = pulse
    )
}

@Composable
private fun StaticPortalIndicator(
    modifier: Modifier,
    size: Dp
) {
    PortalContent(
        modifier = modifier,
        size = size,
        rotation = 0f,
        noisePhase = 0f,
        pulse = 1f
    )
}

@Composable
private fun PortalContent(
    modifier: Modifier,
    size: Dp,
    rotation: Float,
    noisePhase: Float,
    pulse: Float
) {
    val pathCache = remember {
        val cosArr = FloatArray(PathPoints + 1)
        val sinArr = FloatArray(PathPoints + 1)
        val angleArr = FloatArray(PathPoints + 1)
        for (i in 0..PathPoints) {
            val angle = (i.toFloat() / PathPoints) * (PI * 2).toFloat()
            cosArr[i] = cos(angle)
            sinArr[i] = sin(angle)
            angleArr[i] = angle
        }
        PortalPathCache(cosArr, sinArr, angleArr)
    }

    val particleAnglesRad = remember {
        FloatArray(ParticleCount) { i ->
            (i * DegreesPerParticle / 180f * PI).toFloat()
        }
    }

    val portalPaths = remember {
        Array(4) { Path() }
    }

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
            val rotationRad = rotation.toRadians()

            repeat(4) { layer ->
                val radius = this.size.minDimension * (0.42f - layer * 0.05f)
                val path = portalPaths[layer]

                updatePortalPath(
                    path = path,
                    cache = pathCache,
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

            repeat(ParticleCount) { particle ->
                val baseAngle = particleAnglesRad[particle] + rotationRad
                val noiseOffset = sin(noisePhase + particle).absoluteValue
                val radius = this.size.minDimension * 0.42f + noiseOffset * 14f
                val x = center.x + cos(baseAngle) * radius
                val y = center.y + sin(baseAngle) * radius

                drawCircle(
                    color = ParticleColors[particle % 3],
                    radius = 2f + noiseOffset * 3f,
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

private fun updatePortalPath(
    path: Path,
    cache: PortalPathCache,
    center: Offset,
    radius: Float,
    phase: Float
) {
    path.reset()
    val size = cache.cos.size
    for (i in 0 until size) {
        val cosA = cache.cos[i]
        val sinA = cache.sin[i]
        val angle = cache.angles[i]
        val distortion =
            sin(angle * 5f + phase) * 10f +
                    cos(angle * 11f - phase * 1.2f) * 7f +
                    sin(angle * 19f + phase * 0.8f) * 5f +
                    cos(angle * 31f + phase * 1.7f) * 3f

        val finalRadius = radius + distortion
        val x = center.x + cosA * finalRadius
        val y = center.y + sinA * finalRadius
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
}

private fun Float.toRadians(): Float =
    (this / 180f * PI).toFloat()

@Preview
@Composable
fun PortalIndicatorPreview() {
    Surface(
        color = Color(0xFF151515)
    ) {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PortalIndicator(isAnimated = true)
        }
    }
}

@Preview
@Composable
fun PortalIndicatorSizesPreview() {
    Surface(
        color = Color(0xFF151515)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PortalIndicator(size = 40.dp)
            PortalIndicator(size = 80.dp)
            PortalIndicator(size = 120.dp)
        }
    }
}
