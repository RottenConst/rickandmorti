package org.example.rickandmorti.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.example.rickandmorti.GreetingViewModel
import org.example.rickandmorti.Logger
import org.example.rickandmorti.data.Character
import org.example.rickandmorti.screens.items.ItemCharacter
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class DefaultListComponent(
    componentContext: ComponentContext,
    private val characterClicked: (Character) -> Unit
): ListComponent, ComponentContext by componentContext, KoinComponent {

    private val _model = MutableValue<List<Character>>(emptyList())
    override val model: Value<List<Character>> = _model

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val viewModel: GreetingViewModel = get<GreetingViewModel> {
        parametersOf("Hello! KMP")
    }

    init {
        Logger.log("ListComponent: init started")
        lifecycle.subscribe(
            onCreate = {
                Logger.log("ListComponent: onCreate")
                scope.launch {
                    viewModel.loadCharacters()
                }
                // Подписка на поток данных
                scope.launch {
                    viewModel.characters.collect { characters ->
                        Logger.log("Received ${characters.size} characters from Flow")
                        _model.update { characters }
                    }
                }
            },
            onDestroy = {
                Logger.log("ListComponent: onDestroy")
                viewModel.onDestroy()
                scope.cancel()
            }
        )
    }

    override fun onCharacterClicked(character: Character) = characterClicked(character)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    component: ListComponent,
) {
    val state by component.model.subscribeAsState()

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(title = { Text("List") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
        ) {
            if (state.isEmpty()) {
                item {
                    Text(
                        text = "No characters loaded. Check logs.",
                        modifier = Modifier.fillParentMaxSize().wrapContentSize(),
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }
            } else {
                items(state) { character ->
                    ItemCharacter(
                        character = character,
                        onClick = { component.onCharacterClicked(character) }
                    )
                }
            }
        }
    }
}