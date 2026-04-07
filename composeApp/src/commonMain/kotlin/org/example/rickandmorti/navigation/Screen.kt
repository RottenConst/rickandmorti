package org.example.rickandmorti.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import org.example.rickandmorti.data.Character

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {
        class List(val component: ListComponent) : Child
        class Detail(val component: DetailComponent) : Child
    }
}

interface DetailComponent {
    val model: Value<Character>
    fun onBackPressed()
}

interface ListComponent {
    val model: Value<List<Character>>
    val favorites: Flow<Set<Int>>
    fun onCharacterClicked(character: Character)
    fun loadNextPage()
    fun toggleFavorite(character: Character)
}