package org.example.rickandmorti

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import kotlin.collections.emptyMap

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun createSettings(): ObservableSettings =
    NSUserDefaultsSettings.Factory().create()

actual fun openInBrowser(url: String) {
    val sharedApplication = UIApplication.sharedApplication
    val nsUrl = NSURL.URLWithString(url)!!
    sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
}