package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import kotlinx.serialization.Serializable
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.model.Location
import org.example.rickandmorti.presentation.CharacterViewModel
import org.example.rickandmorti.presentation.EpisodesViewModel
import org.example.rickandmorti.presentation.LocationViewModel
import org.example.rickandmorti.util.distinctUntilChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultRootComponent(
    componentContext: ComponentContext
): RootComponent, ComponentContext by componentContext, KoinComponent {

    private val favoritesStore: FavoritesStore by inject()
    private val characterViewModel: CharacterViewModel by inject()
    private val episodesViewModel: EpisodesViewModel by inject()
    private val locationViewModel: LocationViewModel by inject()
    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.CharacterList,
        handleBackButton = true,
        childFactory = ::createChild
    )

    override val activeTab: Value<RootComponent.Tab> = stack.map { childStack ->
        when (childStack.active.configuration) {
            is Config.CharacterList, is Config.CharacterFavorites -> RootComponent.Tab.CHARACTERS
            is Config.EpisodesList -> RootComponent.Tab.EPISODES
            is Config.LocationList -> RootComponent.Tab.LOCATIONS
            else -> RootComponent.Tab.CHARACTERS
        }
    }.distinctUntilChanged()

    override val canGoBack: Value<Boolean> = stack.map { it.items.size > 1 }.distinctUntilChanged()

    override fun onTabSelected(tab: RootComponent.Tab) {
        val config = when (tab) {
            RootComponent.Tab.CHARACTERS -> Config.CharacterList
            RootComponent.Tab.EPISODES -> Config.EpisodesList
            RootComponent.Tab.LOCATIONS -> Config.LocationList
        }
        navigation.replaceAll(config)
    }

    override fun goBack() {
        navigation.pop()
    }

    private fun createCharactersChild(
        componentContext: ComponentContext,
        characterClicked: (Character) -> Unit,
        initialFavoriteOnly: Boolean = false
    ): RootComponent.Child.Characters = RootComponent.Child.Characters(
        DefaultCharacterListComponent(
            componentContext = componentContext,
            favoritesStore = favoritesStore,
            viewModel = characterViewModel,
            initialIsFavoritesOnly = initialFavoriteOnly,
            characterClicked = characterClicked
        )
    )

    private fun createCharacterDetailChild(
        componentContext: ComponentContext,
        character: Character
    ): RootComponent.Child.DetailCharacter = RootComponent.Child.DetailCharacter(
        DefaultDetailCharacterComponent(
            componentContext = componentContext,
            character = character,
            onFinished = { navigation.pop() },
            favoritesStore = favoritesStore,
            episodeClicked = { episode -> navigation.pushToFront(Config.DetailEpisode(episode)) },
            viewModel = characterViewModel,
            onLocationToScreenClicked = { location -> navigation.pushToFront(Config.DetailLocation(location)) }
        )
    )

    private fun createEpisodesChild(
        componentContext: ComponentContext,
        episodeClicked: (Episode) -> Unit
    ): RootComponent.Child.Episodes = RootComponent.Child.Episodes(
        DefaultEpisodeListComponent(
            componentContext = componentContext,
            favoritesStore = favoritesStore,
            viewModel = episodesViewModel,
            episodeClicked = episodeClicked
        )
    )

    private fun createDetailEpisodeChild(
        componentContext: ComponentContext,
        episode: Episode
    ): RootComponent.Child.DetailEpisode = RootComponent.Child.DetailEpisode(
        DefaultDetailEpisodeComponent(
            componentContext = componentContext,
            episode = episode,
            favoritesStore = favoritesStore,
            onFinished = { navigation.pop() },
            viewModel = episodesViewModel,
            characterClicked = { episode -> navigation.pushToFront(Config.Detail(episode))}
        )
    )

    private fun createLocationsChild(
        componentContext: ComponentContext,
        lotionClicked: (Location) -> Unit
    ): RootComponent.Child.Locations = RootComponent.Child.Locations(
        DefaultLocationListComponent(
            componentContext = componentContext,
            locationClicked = lotionClicked,
            favoritesStore = favoritesStore,
            viewModel = locationViewModel
        )
    )

    private fun createDetailLocationChild(
        componentContext: ComponentContext,
        location: Location
    ): RootComponent.Child.DetailLocation = RootComponent.Child.DetailLocation(
        DefaultDetailLocationComponent(
            componentContext = componentContext,
            location = location,
            favoritesStore = favoritesStore,
            onFinished = { navigation.pop() },
            viewModel = locationViewModel,
            characterClicked = { character -> navigation.pushToFront(Config.Detail(character)) }
        )
    )

    @OptIn(DelicateDecomposeApi::class)
    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child = when (config) {
        is Config.CharacterList -> createCharactersChild(
            componentContext = componentContext,
            characterClicked = { character -> navigation.push(Config.Detail(character)) },
        )

        is Config.CharacterFavorites -> createCharactersChild(
            componentContext = componentContext,
            characterClicked = { character -> navigation.push(Config.Detail(character)) },
            initialFavoriteOnly = true
        )

        is Config.Detail -> createCharacterDetailChild(
            componentContext = componentContext,
            character = config.character
        )

        is Config.EpisodesList -> createEpisodesChild(
            componentContext = componentContext,
            episodeClicked = { episode -> navigation.push(Config.DetailEpisode(episode)) }
        )

        is Config.DetailEpisode -> createDetailEpisodeChild(
            componentContext = componentContext,
            episode = config.episode
        )

        is Config.LocationList -> createLocationsChild(
            componentContext = componentContext,
            lotionClicked = { location -> navigation.push(Config.DetailLocation(location))}
        )

        is Config.DetailLocation -> createDetailLocationChild(
            componentContext = componentContext,
            location = config.location
        )
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object CharacterList : Config

        @Serializable
        data object CharacterFavorites: Config

        @Serializable
        data object EpisodesList: Config

        @Serializable
        data object LocationList: Config

        @Serializable
        data class Detail(val character: Character) : Config

        @Serializable
        data class DetailEpisode(val episode: Episode): Config

        @Serializable
        data class DetailLocation(val location: Location): Config
    }
}