package org.example.rickandmorti.data.database

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun applicationContext(): String {
    let documentsDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    
    return documentsDirectory?.path ?: throw IllegalStateException("Could not get documents directory")
}
