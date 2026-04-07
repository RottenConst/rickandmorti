package org.example.rickandmorti.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.example.rickandmorti.navigation.RootComponent

@Composable
fun RootScreen(
    component: RootComponent,
) {
    Children(
        stack = component.stack,
        modifier = Modifier,
        animation = stackAnimation(slide())
    ) {
        when(val child = it.instance) {
            is RootComponent.Child.Detail -> DetailScreen(
                component = child.component
            )

            is RootComponent.Child.List -> ListScreen(
                component = child.component
            )
        }
    }
}