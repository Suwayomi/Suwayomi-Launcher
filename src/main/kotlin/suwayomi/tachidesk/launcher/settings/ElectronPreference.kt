package suwayomi.tachidesk.launcher.settings

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.russhwolf.settings.ObservableSettings
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

class ElectronPreference(
    private val webUIInterface: LauncherPreference<LauncherSettings.WebUIInterface>,
    launcherKey: String,
    key: String,
    default: String?,
    settings: ObservableSettings,
    adapter: Adapter<String?>
) : LauncherPreference<String?>(
    launcherKey,
    key,
    default,
    settings,
    adapter
) {

    override fun getProperty(): String? {
        return when (webUIInterface.get()) {
            LauncherSettings.WebUIInterface.Browser -> super.getProperty()
            LauncherSettings.WebUIInterface.Electron -> when (get()) {
                null -> {
                    val os = System.getProperty("os.name").lowercase()
                    val path = if (os.startsWith("mac os x")) {
                        Path("electron/Electron.app/Contents/MacOS/Electron").absolutePathString()
                    } else if (os.startsWith("windows")) {
                        Path("electron/electron.exe").absolutePathString()
                    } else {
                        // Probably linux.
                        Path("./electron/electron").absolutePathString()
                    }
                    propertyPrefix + path
                }
                else -> super.getProperty()
            }
        }
    }
}
