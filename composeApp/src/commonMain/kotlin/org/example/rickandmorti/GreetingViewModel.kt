package org.example.rickandmorti

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.example.rickandmorti.data.Character
import kotlin.native.concurrent.ThreadLocal
import kotlin.time.Clock

class GreetingViewModel(
    private val greetingRepository: GreetingRepository,
    private val coroutineScope: CoroutineScope,
    initialGreeting: String
) {
    private val _greetings = MutableStateFlow(listOf(initialGreeting))
    val greetings: StateFlow<List<String>> = _greetings

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    init {
        Logger.log("GreetingViewModel created")
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun addGreeting(text: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val formatter = LocalDateTime.Format { byUnicodePattern("HH:mm:ss") }
        val formattedDate = now.format(formatter)
        _greetings.update { current -> current + "$text ($formattedDate)" }
    }

    fun loadCharacters(){
        Logger.log("loadCharacters called")
        coroutineScope.launch {
            try {
                val result = greetingRepository.loadCharacters()
                Logger.log("Characters loaded: ${result.size} items")
                _characters.value = result
            } catch (e: Exception) {
                Logger.log("Error loading characters: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun onDestroy() {
        coroutineScope.cancel()
    }
}

//логи
@ThreadLocal
object Logger {
    fun log(message: String) = println("[LOG] $message")
}