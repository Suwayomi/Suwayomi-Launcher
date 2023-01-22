package suwayomi.tachidesk.launcher.settings

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.set

interface LauncherAdapter<T> {
    operator fun get(settings: ObservableSettings, key: String, default: T): T

    operator fun set(settings: ObservableSettings, key: String, value: T)

    fun addListener(settings: ObservableSettings, key: String, default: T, callback: (T) -> Unit): SettingsListener
}

object BooleanAdapter : LauncherAdapter<Boolean> {
    override fun get(settings: ObservableSettings, key: String, default: Boolean): Boolean {
        return settings.getBoolean(key, default)
    }

    override fun set(settings: ObservableSettings, key: String, value: Boolean) {
        settings[key] = value
    }

    override fun addListener(settings: ObservableSettings, key: String, default: Boolean, callback: (Boolean) -> Unit): SettingsListener {
        return settings.addBooleanListener(key, default, callback)
    }
}
