package org.example.rickandmorti.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.example.rickandmorti.data.Character

class DefaultDetailComponent(
    componentContext: ComponentContext,
    character: Character,
    private val onFinished: () -> Unit
): DetailComponent, ComponentContext by componentContext {

    override val model: Value<Character> = MutableValue(character)

    override fun onBackPressed() = onFinished()
}