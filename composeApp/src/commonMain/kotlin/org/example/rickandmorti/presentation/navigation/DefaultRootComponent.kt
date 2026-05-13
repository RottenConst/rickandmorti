package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import kotlinx.serialization.Serializable
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.util.distinctUntilChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class DefaultRootComponent(
    componentContext: ComponentContext
): RootComponent, ComponentContext by componentContext, KoinComponent {

    private val favoritesStore: FavoritesStore by inject()
    private val navigation = StackNavigation<Config>()
    private val _activeTab = MutableValue(RootComponent.Tab.LIST)
    override val activeTab: Value<RootComponent.Tab> = _activeTab
    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.List,
        handleBackButton = true,
        childFactory = ::createChild
    )
    override val canGoBack: Value<Boolean> = stack.map { it.items.size > 1 }.distinctUntilChanged()

    override fun onTabSelected(tab: RootComponent.Tab) {
        _activeTab.value = tab // Сохраняем активную вкладку
        when (tab) {
            RootComponent.Tab.LIST -> navigation.replaceAll(Config.List)
            RootComponent.Tab.FAVORITES -> navigation.replaceAll(Config.Favorites)
        }
    }

    override fun goBack() {
        navigation.pop()
    }

    @OptIn(DelicateDecomposeApi::class)
    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child = when (config) {
        Config.List -> RootComponent.Child.List(
            DefaultListComponent(
                componentContext = componentContext,
                favoritesStore = favoritesStore,
                isFavoritesOnly = false,
                characterClicked = { character ->
                    navigation.push(Config.Detail(character))
                }
            )
        )

        Config.Favorites -> RootComponent.Child.Favorites(
            DefaultListComponent(
                componentContext = componentContext,
                favoritesStore = favoritesStore,
                isFavoritesOnly = true,
                characterClicked = { character ->
                    navigation.push(Config.Detail(character))
                }
            )
        )

        is Config.Detail -> RootComponent.Child.Detail(
            DefaultDetailComponent(
                componentContext = componentContext,
                character = config.character,
                onFinished = { navigation.pop() },
                favoritesStore = favoritesStore
            )
        )
    }


    @Serializable
    private sealed interface Config { // 6
        @Serializable
        data object List : Config

        @Serializable
        data object Favorites: Config

        @Serializable
        data class Detail(val character: Character) : Config
    }
}