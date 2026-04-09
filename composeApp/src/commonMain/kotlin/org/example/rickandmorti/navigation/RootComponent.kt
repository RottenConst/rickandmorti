package org.example.rickandmorti.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.data.Character
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class DefaultRootComponent(
    componentContext: ComponentContext
): RootComponent, ComponentContext by componentContext, KoinComponent {

    private val favoritesStore: FavoritesStore by inject()
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
                favoritesStore = favoritesStore,
                characterClicked = { character ->
                    nav.pushNew(Config.Detail(character))
                }
            )
        )

        is Config.Detail -> RootComponent.Child.Detail(
            DefaultDetailComponent(
                componentContext = componentContext,
                character = config.character,
                onFinished = { nav.pop() },
                favoritesStore = favoritesStore
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