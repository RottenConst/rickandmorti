package org.example.rickandmorti.presentation.screens.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CharacterFilters(
    selected: CharacterFilter,
    onSelected: (CharacterFilter) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(
            CharacterFilter.entries,
            key = { it.name }
        ) { filter ->

            PortalFilterChip(
                text = filter.title,
                selected = filter == selected,
                onClick = {
                    onSelected(filter)
                }
            )
        }
    }
}

@Composable
fun PortalFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val background by animateColorAsState(
        if (selected)
            Color(0xFF76FF03)
        else
            Color(0xFF1E2630),
        label = "background"
    )

    val contentColor by animateColorAsState(
        if (selected)
            Color.Black
        else
            Color(0xFFB0BEC5),
        label = "content"
    )

    val borderColor by animateColorAsState(
        if (selected)
            Color(0xFFAEEA00)
        else
            Color(0xFF2C3643),
        label = "border"
    )

    var hovered by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        if (hovered) 1.16f else 1f,
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .hoverable(
                remember {
                    MutableInteractionSource()
                }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        onClick = onClick,
        color = background,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor
        ),
        shadowElevation =
            if (selected) 8.dp else 0.dp
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )

    }
}

enum class CharacterFilter(
    val title: String
) {
    ALL("All"),
    ALIVE("Alive"),
    DEAD("Dead"),
    HUMAN("Human"),
    ALIEN("Alien"),
    ROBOT("Robot")
}