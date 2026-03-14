package org.example.rickandmorti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GreetingViewModel(initialGreeting: String) {
    private val _greetings = MutableStateFlow(listOf(initialGreeting))
    val greetings: StateFlow<List<String>> = _greetings

    fun addGreeting(text: String) {
        _greetings.update { current -> current + text }
    }
}