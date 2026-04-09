package org.example.rickandmorti.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.data.Character

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val activeTab: Value<Tab>

    fun onTabSelected(tab: Tab)
    enum class Tab {
        LIST, FAVORITES;

        val title: String
            get() = when (this) {
                LIST -> "All"
                FAVORITES -> "Favorites"
            }
    }

    sealed interface Child {
        class List(val component: ListComponent) : Child
        class Detail(val component: DetailComponent) : Child
        class Favorites(val component: ListComponent) : Child
    }
}

interface DetailComponent {
    val model: Value<Character>
    val favorites: Value<Set<Int>>
    fun toggleFavorite(character: Character)
    fun onBackPressed()
}

interface ListComponent {
    val model: Value<List<Character>>
    val favorites: Flow<Set<Int>>
    val isFavoritesOnly: Boolean

    fun onCharacterClicked(character: Character)
    fun loadNextPage()
    fun toggleFavorite(character: Character)
}