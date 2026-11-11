package org.example.rickandmorti.presentation.screens.items

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import org.jetbrains.compose.resources.painterResource
import rickandmorti.composeapp.generated.resources.Res
import rickandmorti.composeapp.generated.resources.rick_and_morty_logo

@Composable
fun HeroHeaderCollapse(
    gridState: LazyGridState,
    characterCount: Int,
    modifier: Modifier = Modifier
) {
    val collapseProgress = rememberCollapseProgress(gridState)

    val height = lerp(220.dp, 80.dp, collapseProgress)
    val portalSize = lerp(170.dp, 52.dp, collapseProgress)
    val titleSize = lerp(64.dp, 32.dp, collapseProgress)
    val cornerRadius = lerp(32.dp, 0.dp, collapseProgress)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF10151B))
    ) {
        SpaceBackground(collapseProgress = collapseProgress)

        PortalArea(
            portalSize = portalSize,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        )

        HeroGradient()

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    horizontal = 24.dp,
                    vertical = 20.dp
                )
        ) {
            Image(
                painter = painterResource(
                    Res.drawable.rick_and_morty_logo
                ),
                contentDescription = null,
                modifier = Modifier
                    .height(
                        titleSize
                    )
                    .graphicsLayer {
                        alpha = 1f - collapseProgress * 0.15f
                    }
            )

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - collapseProgress
                    translationY = collapseProgress * -30f
                }
            ) {
                Text(
                    text = "Explore Infinite Universes",
                    color = Color(0xFFB0BEC5)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "$characterCount Characters",
                    color = Color(0xFF76FF03),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SpaceBackground(
    collapseProgress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "space-bg-transition")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                30000,
                easing = LinearEasing
            )
        ),
        label = "star-offset"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = collapseProgress * -50f
            }
    ) {
        repeat(200) { index ->
            val x = (index * 61f + offset) % size.width
            val y = (index * 37f) % size.height

            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 1.4f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun PortalArea(
    portalSize: Dp,
    modifier: Modifier = Modifier
) {

    val infiniteTransition =
        rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(
            tween(
                12000,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier
            .size(140.dp)
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {

        PortalIndicator(
            modifier = Modifier.fillMaxSize(),
            size = portalSize
        )
    }
}

@Composable
private fun HeroGradient() {
    val gradient = remember {
        Brush.horizontalGradient(
            listOf(
                Color(0xEE10151B),
                Color(0x8810151B),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    )
}

@Composable
private fun rememberCollapseProgress(
    state: LazyGridState,
    collapsePx: Dp = 180.dp
): Float {
    val density = LocalDensity.current

    return remember(state, density, collapsePx) {
        val targetPx = with(density) { collapsePx.toPx() }
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) {
                1f
            } else {
                (state.firstVisibleItemScrollOffset / targetPx).coerceIn(0f, 1f)
            }
        }
    }.value
}
