package org.example.rickandmorti.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.example.rickandmorti.FavoritesStore
import org.example.rickandmorti.domain.model.Character
import kotlin.collections.emptySet

class DefaultDetailComponent(
    componentContext: ComponentContext,
    character: Character,
    private val onFinished: () -> Unit,
    private val favoritesStore: FavoritesStore
): DetailComponent, ComponentContext by componentContext {

    override val model: Value<Character> = MutableValue(character)
    private val _favorites = MutableValue<Set<Int>>(emptySet())
    override val favorites: Value<Set<Int>> = _favorites

    private val scope = CoroutineScope(SupervisorJob())

    init {
        favoritesStore.favoritesFlow
            .onEach { set ->
                _favorites.value = set
            }
            .launchIn(scope)

        // Правильный способ: используем doOnDestroy
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    override fun toggleFavorite(character: Character) {
        if (favoritesStore.isFavorite(character.id)) {
            favoritesStore.removeFavorite(character.id)
        } else {
            favoritesStore.addFavorite(character.id)
        }
    }

    override fun onBackPressed() = onFinished()
}