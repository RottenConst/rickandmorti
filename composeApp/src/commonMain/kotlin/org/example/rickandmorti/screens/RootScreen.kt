package org.example.rickandmorti.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import org.example.rickandmorti.navigation.RootComponent

@Composable
fun RootScreen(
    component: RootComponent,
) {
    val activeTab by component.activeTab.subscribeAsState()

    Scaffold(
        bottomBar = {
            CustomBottomBar(
                tabs = RootComponent.Tab.entries.toTypedArray(),
                selectedTab = activeTab,
                onTabSelected = component::onTabSelected,
            )
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
                androidx.compose.material3.MaterialTheme.colorScheme.primary
            } else if (isHovered) {
                androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
            val animatedColor by animateColorAsState(targetValue = targetColor, label = "bottom_bar_icon_color")

            // Фон при активной вкладке или наведении
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    tab == selectedTab -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    isHovered -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> androidx.compose.ui.graphics.Color.Transparent
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