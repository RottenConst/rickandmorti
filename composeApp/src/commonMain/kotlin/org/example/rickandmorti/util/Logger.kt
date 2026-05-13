package org.example.rickandmorti.util

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object Logger {
    fun log(message: String) = println("[LOG] $message")
}