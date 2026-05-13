package org.example.rickandmorti.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class BaseViewModel(
    protected val viewModelScope: CoroutineScope = MainScope()
) {
    fun onDestroy() {
        viewModelScope.cancel()
    }
}