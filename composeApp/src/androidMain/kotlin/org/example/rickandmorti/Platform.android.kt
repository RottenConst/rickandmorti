package org.example.rickandmorti

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun createSettings(): ObservableSettings {
    val context: Context = Platform2.context
    return SharedPreferencesSettings(context.getSharedPreferences("app_settings", Context.MODE_PRIVATE))
}

@SuppressLint("StaticFieldLeak")
internal object Platform2 {
    lateinit var context: Context
        private set

    fun initialize(application: Application) {
        context = application.applicationContext
    }
}