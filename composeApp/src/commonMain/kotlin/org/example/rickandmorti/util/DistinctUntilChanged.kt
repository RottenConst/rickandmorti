package org.example.rickandmorti.util

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.Value

fun <T : Any> Value<T>.distinctUntilChanged(): Value<T> = object : Value<T>() {
    private var current: T? = null
    override var value: T = this@distinctUntilChanged.value
        private set

    override fun subscribe(observer: (T) -> Unit): Cancellation {
        val originalCancellation = this@distinctUntilChanged.subscribe { newValue ->
            if (current == null || current != newValue) {
                current = newValue
                value = newValue
                observer(newValue)
            }
        }

        return object : Cancellation {
            override fun cancel() {
                originalCancellation.cancel()
            }
        }
    }
}