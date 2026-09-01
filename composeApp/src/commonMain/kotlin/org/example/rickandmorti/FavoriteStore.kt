package org.example.rickandmorti

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.collections.emptySet

interface FavoritesStore {
    val favoritesCharactersFlow: Flow<Set<Int>>
    val favoritesEpisodesFlow: Flow<Set<Int>>
    val favoritesLocationsFlow: Flow<Set<Int>>

    fun addFavoriteCharacter(characterId: Int)
    fun removeFavoriteCharacter(characterId: Int)
    fun isFavoriteCharacter(characterId: Int): Boolean

    fun addFavoriteEpisode(episodeId: Int)
    fun removeFavoriteEpisode(episodeId: Int)
    fun isFavoriteEpisode(episodeID: Int): Boolean

    fun addFavoriteLocation(locationID:Int)
    fun removeFavoriteLocation(locationID: Int)
    fun isFavoriteLocation(locationID: Int): Boolean
}

class SettingsFavoritesStore(
    private val settings: Settings,
    private val json: Json = Json { ignoreUnknownKeys = true }
): FavoritesStore {

    private companion object {
        const val FAVORITES_CHARACTER_KEY = "favorites_list"
        const val FAVORITES_EPISODES_KEY = "favorite_episodes_list"
        const val FAVORITES_LOCATIONS_KEY = "favorite_locations_list"
    }

    private val _favoritesCharactersFlow = MutableStateFlow<Set<Int>>(emptySet())
    override val favoritesCharactersFlow: StateFlow<Set<Int>> = _favoritesCharactersFlow.asStateFlow()

    private val _favoritesEpisodesFlow = MutableStateFlow<Set<Int>>(emptySet())
    override val favoritesEpisodesFlow: StateFlow<Set<Int>> = _favoritesEpisodesFlow.asStateFlow()

    private val _favoritesLocationsFlow = MutableStateFlow<Set<Int>>(emptySet())
    override val favoritesLocationsFlow: StateFlow<Set<Int>> = _favoritesLocationsFlow.asStateFlow()

    init {
        _favoritesCharactersFlow.value = parseFavorites(FAVORITES_CHARACTER_KEY)
        _favoritesEpisodesFlow.value = parseFavorites(FAVORITES_EPISODES_KEY)
        _favoritesLocationsFlow.value = parseFavorites(FAVORITES_LOCATIONS_KEY)
    }

    // персонажи

    override fun addFavoriteCharacter(characterId: Int) {
        val current = _favoritesCharactersFlow.value
        val updated = current + characterId
        _favoritesCharactersFlow.value = updated
        settings[FAVORITES_CHARACTER_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated.toList())
    }

    override fun removeFavoriteCharacter(characterId: Int) {
        val current = _favoritesCharactersFlow.value
        val updated = current - characterId
        _favoritesCharactersFlow.value = updated
        settings[FAVORITES_CHARACTER_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated.toList())
    }

    override fun isFavoriteCharacter(characterId: Int): Boolean = _favoritesCharactersFlow.value.contains(characterId)

    // эпизоды

    override fun addFavoriteEpisode(episodeId: Int) {
        val current = _favoritesEpisodesFlow.value
        val updated = current + episodeId
        _favoritesEpisodesFlow.value = updated
        settings[FAVORITES_EPISODES_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated.toList())
    }

    override fun removeFavoriteEpisode(episodeId: Int) {
        val current = _favoritesEpisodesFlow.value
        val updated = current - episodeId
        _favoritesEpisodesFlow.value = updated
        settings[FAVORITES_EPISODES_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated.toList())
    }

    override fun isFavoriteEpisode(episodeID: Int): Boolean = _favoritesEpisodesFlow.value.contains(episodeID)

    // локации

    override fun addFavoriteLocation(locationID: Int) {
        val current = _favoritesLocationsFlow.value
        val updated = current + locationID
        _favoritesLocationsFlow.value = updated
        settings[FAVORITES_LOCATIONS_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated.toList())
    }

    override fun removeFavoriteLocation(locationID: Int) {
        val current = _favoritesLocationsFlow.value
        val updated = current - locationID
        _favoritesLocationsFlow.value = updated
        settings[FAVORITES_LOCATIONS_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated.toList())
    }

    override fun isFavoriteLocation(locationID: Int): Boolean = _favoritesLocationsFlow.value.contains(locationID)

    private fun parseFavorites(key: String): Set<Int> {
        val str = settings.getStringOrNull(key)
        return if (str.isNullOrEmpty()) {
            emptySet()
        } else {
            runCatching {
                json.decodeFromString(ListSerializer(Int.serializer()), str)
            }.getOrNull()?.toSet() ?: emptySet()
        }
    }
}