package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.domain.model.Character
import org.example.rickandmorti.domain.model.Episode
import org.example.rickandmorti.domain.model.Location

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val activeTab: Value<Tab>
    val canGoBack: Value<Boolean>
    fun goBack()

    fun onTabSelected(tab: Tab)

    enum class Tab {
        CHARACTERS, EPISODES, LOCATIONS;

        val title: String
            get() = when (this) {
                CHARACTERS -> "All"
                EPISODES -> "Episodes"
                LOCATIONS -> "Locations"
            }
    }

    sealed interface Child {
        class Characters(val component: DefaultCharacterListComponent) : Child
        class DetailCharacter(val component: DetailCharacterComponent) : Child
        class Favorites(val component: CharacterListComponent) : Child
        class Episodes(val component: DefaultEpisodeListComponent) : Child
        class DetailEpisode(val component: DetailEpisodeComponent) : Child
        class Locations(val component: LocationListComponent): Child
        class DetailLocation(val component: DetailLocationComponent): Child
    }
}

interface DetailCharacterComponent {
    val character: Value<Character>
    val episodes: Value<List<Episode>>
    val isEpisodeLoading: Value<Boolean>
    val favorites: Value<Set<Int>>
    fun onEpisodeClicked(episode: Episode)
    fun toggleFavorite(character: Character)
    fun onBackPressed()
}

interface DetailEpisodeComponent {
    val episode: Value<Episode>
    val characters: Value<List<Character>>
    val isCharactersLoading: Value<Boolean>
    fun onCharacterClicked(character: Character)
    fun openEpisodeWatchPage()
    fun onBackPressed()
}

interface DetailLocationComponent {
    val location: Value<Location>
    val characters: Value<List<Character>>
    val isCharactersLoading: Value<Boolean>
    fun onCharacterClicked(character: Character)
    fun onBackPressed()
}

interface CharacterListComponent {
    val characters: Value<List<Character>>
    val favorites: Flow<Set<Int>>
    val isFavoritesOnly: Value<Boolean>
    val hasMorePages: Value<Boolean>
    fun loadNextPage(name: String?)
    fun loadSearch(name: String?)
    fun onCharacterClicked(character: Character)
    fun loadNextPage()
    fun toggleFavorite(character: Character)
}

interface EpisodeListComponent {
    val episodes: Value<List<Episode>>
    val hasMorePages: Value<Boolean>
    fun loadSearchEpisode(name: String?)
    fun onEpisodeClicked(episode: Episode)
    fun loadNextPage()
    fun loadNextPage(name: String?)
}

interface LocationListComponent {
    val location: Value<List<Location>>
    val hasMorePages: Value<Boolean>
    fun loadSearchLocation(name: String?)
    fun onLocationClick(location: Location)
    fun loadNextPage()
    fun loadNextPage(name: String?)
}