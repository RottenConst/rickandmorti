package org.example.rickandmorti.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.rickandmorti.data.Character
import org.example.rickandmorti.screens.items.ItemCharacter

class DefaultRootComponent(
    componentContext: ComponentContext
): RootComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = nav,
        serializer = Config.serializer(),
        initialConfiguration = Config.List,
        handleBackButton = true,
        childFactory = ::child
    )

    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child = when(config) {
        Config.List -> RootComponent.Child.List(
            DefaultListComponent(
                componentContext = componentContext,
                characterClicked = { character ->
                    nav.pushNew(Config.Detail(character))
                }
            )
        )

        is Config.Detail -> RootComponent.Child.Detail(
            DefaultDetailComponent(
                componentContext = componentContext,
                character = config.character,
                onFinished = { nav.pop() }
                )
        )
    }


    @Serializable
    private sealed interface Config { // 6
        @Serializable
        data object List : Config

        @Serializable
        data class Detail(val character: Character) : Config
    }
}

@Composable
fun RootComponent(
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

            is RootComponent.Child.List -> ListContent(
                component = child.component
            )
        }
    }
}