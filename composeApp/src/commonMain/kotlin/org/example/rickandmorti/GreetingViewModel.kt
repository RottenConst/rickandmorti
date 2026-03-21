package org.example.rickandmorti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GreetingViewModel(
    private val greetingRepository: GreetingRepository,
    initialGreeting: String
) {
    private val _greetings = MutableStateFlow(listOf(initialGreeting))
    val greetings: StateFlow<List<String>> = _greetings

    val characters get() = greetingRepository.characters

    fun addGreeting(text: String) {
        _greetings.update { current -> current + text }
    }

    suspend fun loadCharacters(){
        greetingRepository.loadCharacters()
    }
}