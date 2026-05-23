package org.example.rickandmorti

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import androidx.core.net.toUri

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

actual fun openInBrowser(url: String) {
    val context = Platform2.context
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}