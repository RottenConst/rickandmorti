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
    val favoritesFlow: Flow<Set<Int>>
    fun addFavorite(characterId: Int)
    fun removeFavorite(characterId: Int)
    fun isFavorite(characterId: Int): Boolean
}

class SettingsFavoritesStore(
    private val settings: ObservableSettings,
    private val json: Json = Json { ignoreUnknownKeys = true }
): FavoritesStore {

    private companion object {
        const val FAVORITES_KEY = "favorites_list"
    }

    @OptIn(ExperimentalSettingsApi::class, ExperimentalCoroutinesApi::class,
        DelicateCoroutinesApi::class
    )
    override val favoritesFlow: Flow<Set<Int>> = settings.getStringFlow(FAVORITES_KEY, defaultValue = "")
        .mapLatest { str ->
            if (str.isEmpty()) {
                emptySet()
            } else {
                runCatching {
                    json.decodeFromString<List<Int>>(ListSerializer(Int.serializer()), str)
                }.getOrNull()?.toSet() ?: emptySet()
            }
        }
        .shareIn(
            scope = GlobalScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed(5_000)
        )
    override fun addFavorite(characterId: Int) {
        val current = getFavorites()
        val updated = (current + characterId).toList()
        settings[FAVORITES_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun removeFavorite(characterId: Int) {
        val current = getFavorites()
        val updated = (current - characterId).toList()
        settings[FAVORITES_KEY] = json.encodeToString(ListSerializer(Int.serializer()), updated)
    }

    override fun isFavorite(characterId: Int): Boolean = getFavorites().contains(characterId)

    private fun getFavorites(): Set<Int> {
        val str: String? = settings.getStringOrNull(FAVORITES_KEY)
        return if (str == null || str.isEmpty()) {
            emptySet()
        } else {
            runCatching {
                json.decodeFromString<List<Int>>(ListSerializer(Int.serializer()), str)
            }.getOrNull()?.toSet() ?: emptySet()
        }
    }
}