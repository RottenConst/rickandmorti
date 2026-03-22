package org.example.rickandmorti.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
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
    fun onCharacterClicked(character: Character)
}