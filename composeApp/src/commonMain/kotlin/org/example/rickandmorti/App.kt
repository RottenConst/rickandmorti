package org.example.rickandmorti

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

@Composable
@Preview
fun App() {
    MaterialTheme {

        val koin = getKoin()

        val viewModel: GreetingViewModel = remember {
            koin.get<GreetingViewModel> {
                parametersOf("Hello from Parameter! 🚀")
            }
        }

        var greetings by remember { mutableStateOf(viewModel.greetings.value) }

        LaunchedEffect(Unit) {
            viewModel.greetings.collect { newGreetings ->
                greetings = newGreetings
            }
        }

        Column(
            modifier = Modifier
                .padding(all = 10.dp)
                .safeContentPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            greetings.forEach { greeting ->
                Text(greeting)
                HorizontalDivider()
            }

            Button(onClick = {
                viewModel.addGreeting("New greeting at ${currentMillis()}")
            }) {
                Text("add greeting")
            }
        }
    }
}