package org.example.rickandmorti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GreetingViewModel(
    private val greetingRepository: GreetingRepository,
    initialGreeting: String
) {
    private val _greetings = MutableStateFlow(listOf(initialGreeting))
    val greetings: StateFlow<List<String>> = _greetings

    val characters get() = greetingRepository.characters

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun addGreeting(text: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val formatter = LocalDateTime.Format { byUnicodePattern("HH:mm:ss") }
        val formattedDate = now.format(formatter)
        _greetings.update { current -> current + "$text ($formattedDate)" }
    }

    suspend fun loadCharacters(){
        greetingRepository.loadCharacters()
    }
}