package org.example.rickandmorti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
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
    private val settings: ObservableSettings,
    private val json: Json = Json { ignoreUnknownKeys = true }
): FavoritesStore {

    private companion object {
        const val FAVORITES_CHARACTER_KEY = "favorites_list"
        const val FAVORITES_EPISODES_KEY = "favorite_episodes_list"
        const val FAVORITES_LOCATIONS_KEY = "favorite_locations_list"
    }

    @OptIn(ExperimentalSettingsApi::class, ExperimentalCoroutinesApi::class,
        DelicateCoroutinesApi::class
    )
    override val favoritesCharactersFlow: Flow<Set<Int>> = settings.getStringFlow(FAVORITES_CHARACTER_KEY, defaultValue = "")
        .mapLatest { str ->
            if (str.isEmpty()) emptySet()
            else runCatching {
                json.decodeFromString(ListSerializer(Int.serializer()), str)
            }.getOrNull()?.toSet() ?: emptySet()

        }.shareIn(
            scope = GlobalScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class,
        ExperimentalSettingsApi::class
    )
    override val favoritesEpisodesFlow: Flow<Set<Int>> = settings.getStringFlow(FAVORITES_EPISODES_KEY, defaultValue = "")
        .mapLatest { str ->
            if (str.isEmpty()) emptySet()
            else runCatching {
                json.decodeFromString(ListSerializer(Int.serializer()), str)
            }.getOrNull()?.toSet() ?: emptySet()
        }.shareIn(
            scope = GlobalScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class,
        ExperimentalSettingsApi::class
    )
    override val favoritesLocationsFlow: Flow<Set<Int>> = settings.getStringFlow(FAVORITES_LOCATIONS_KEY, defaultValue = "")
        .mapLatest { str ->
            if (str.isEmpty()) emptySet()
            else runCatching {
                json.decodeFromString(ListSerializer(Int.serializer()), str)
            }.getOrNull()?.toSet() ?: emptySet()
        }.shareIn(GlobalScope, SharingStarted.WhileSubscribed(5_000), 1)

    // персонажи

    override fun addFavoriteCharacter(characterId: Int) {
        val current = getFavorites(FAVORITES_CHARACTER_KEY)
        val updated = (current + characterId).toList()
        settings[FAVORITES_CHARACTER_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun removeFavoriteCharacter(characterId: Int) {
        val current = getFavorites(FAVORITES_CHARACTER_KEY)
        val updated = (current - characterId).toList()
        settings[FAVORITES_CHARACTER_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun isFavoriteCharacter(characterId: Int): Boolean = getFavorites(FAVORITES_CHARACTER_KEY).contains(characterId)

    // эпизоды

    override fun addFavoriteEpisode(episodeId: Int) {
        val current = getFavorites(FAVORITES_EPISODES_KEY)
        val updated = (current + episodeId).toList()
        settings[FAVORITES_EPISODES_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun removeFavoriteEpisode(episodeId: Int) {
        val current = getFavorites(FAVORITES_EPISODES_KEY)
        val updated = (current - episodeId).toList()
        settings[FAVORITES_EPISODES_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun isFavoriteEpisode(episodeID: Int): Boolean = getFavorites(FAVORITES_EPISODES_KEY).contains(episodeID)

    // локации

    override fun addFavoriteLocation(locationID: Int) {
        val current = getFavorites(FAVORITES_LOCATIONS_KEY)
        val updated = (current + locationID).toList()
        settings[FAVORITES_LOCATIONS_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun removeFavoriteLocation(locationID: Int) {
        val current = getFavorites(FAVORITES_LOCATIONS_KEY)
        val updated = (current - locationID).toList()
        settings[FAVORITES_LOCATIONS_KEY] = json.encodeToString(ListSerializer(Int.serializer()),updated)
    }

    override fun isFavoriteLocation(locationID: Int): Boolean = getFavorites(FAVORITES_LOCATIONS_KEY).contains(locationID)

    private fun getFavorites(key: String): Set<Int> {
        val str: String? = settings.getStringOrNull(key)
        return if (str.isNullOrEmpty()) {
            emptySet()
        } else {
            runCatching {
                json.decodeFromString(ListSerializer(Int.serializer()), str)
            }.getOrNull()?.toSet() ?: emptySet()
        }
    }
}