package org.example.rickandmorti

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun currentMillis(): Long = NSDate().timeIntervalSince1970.toLong() * 1000