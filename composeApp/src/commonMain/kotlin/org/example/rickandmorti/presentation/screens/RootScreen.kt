package org.example.rickandmorti.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import org.example.rickandmorti.presentation.navigation.RootComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    component: RootComponent,
) {
    val activeTab by component.activeTab.subscribeAsState()
    val canGoBack by component.canGoBack.subscribeAsState()

    val child by component.stack.subscribeAsState()
    val activeChild = child.active.instance

    val titleText: String by remember(activeChild, activeTab) {
        derivedStateOf {
            when (activeChild) {
                is RootComponent.Child.Detail -> activeChild.component.character.value.name
                else -> when (activeTab) {
                    RootComponent.Tab.LIST -> "Characters"
                    RootComponent.Tab.FAVORITES -> "Favorites"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = titleText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { component.goBack() }) {
                            Icon(imageVector = TablerIcons.ArrowLeft, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Показываем сердце только если активен Detail
                    if (activeChild is RootComponent.Child.Detail) {
                        val character = activeChild.component.character.value
                        val favorites by activeChild.component.favorites.subscribeAsState()
                        val isFavorite = favorites.contains(character.id)

                        IconButton(
                            onClick = { activeChild.component.toggleFavorite(character) }
                        ) {
                            Icon(
                                imageVector = TablerIcons.Heart,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) Color.Red else LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (component.stack.value.active.instance !is RootComponent.Child.Detail) {
                CustomBottomBar(
                    tabs = RootComponent.Tab.entries.toTypedArray(),
                    selectedTab = activeTab,
                    onTabSelected = component::onTabSelected,
                )
            }
        }
    ) { paddingValues ->
        Children(
            stack = component.stack,
            modifier = Modifier.padding(paddingValues),
            animation = stackAnimation(slide())
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Detail -> DetailScreen(component = instance.component)
                is RootComponent.Child.List -> ListScreen(component = instance.component)
                is RootComponent.Child.Favorites -> ListScreen(component = instance.component)
            }
        }
    }
}

@Composable
private fun CustomBottomBar(
    tabs: Array<RootComponent.Tab>,
    selectedTab: RootComponent.Tab,
    onTabSelected: (RootComponent.Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val icon = when (tab) {
                RootComponent.Tab.LIST -> TablerIcons.Home
                RootComponent.Tab.FAVORITES -> TablerIcons.Heart
            }

            var isHovered by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }

            // Цвет иконки и текста
            val targetColor = if (tab == selectedTab) {
                MaterialTheme.colorScheme.primary
            } else if (isHovered) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
            val animatedColor by animateColorAsState(targetValue = targetColor, label = "bottom_bar_icon_color")

            // Фон при активной вкладке или наведении
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    tab == selectedTab -> MaterialTheme.colorScheme.primaryContainer
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> Color.Transparent
                },
                label = "bottom_bar_bg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (tab == selectedTab || isHovered) {
                            Modifier
                                .padding(4.dp)
                                .background(backgroundColor)
                                .padding(8.dp)
                        } else {
                            Modifier.padding(8.dp)
                        }
                    )
                    .pointerInput(tab) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Enter) {
                                    isHovered = true
                                } else if (event.type == PointerEventType.Exit) {
                                    isHovered = false
                                }
                            }
                        }
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null // Убираем стандартное ripple, если хотите кастомное поведение
                    ) {
                        onTabSelected(tab)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        tint = animatedColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        color = animatedColor,
                        fontSize = if (tab == selectedTab) 12.sp else 11.sp,
                        fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}