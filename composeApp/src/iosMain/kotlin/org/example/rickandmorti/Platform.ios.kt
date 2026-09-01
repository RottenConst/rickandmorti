package org.example.rickandmorti

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.collections.emptyMap

actual fun createSettings(): Settings =
    NSUserDefaultsSettings.Factory().create()

actual fun openInBrowser(url: String) {
    val sharedApplication = UIApplication.sharedApplication
    val nsUrl = NSURL.URLWithString(url)!!
    sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
}