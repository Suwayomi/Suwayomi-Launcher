package suwayomi.tachidesk.launcher.settings

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

class LauncherSettings {
    private val settings = PreferencesSettings(
        Preferences.userRoot().node("suwayomi/launcher")
    )

    fun debugLogs(): LauncherPreference<Boolean> {
        return LauncherPreference("debug", false, settings, BooleanAdapter)
    }
}
