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
import com.russhwolf.settings.get
import com.russhwolf.settings.set

interface Adapter<T> {
    operator fun get(
        settings: ObservableSettings,
        key: String,
        default: T,
    ): T

    operator fun set(
        settings: ObservableSettings,
        key: String,
        value: T,
    )

    fun addListener(
        settings: ObservableSettings,
        key: String,
        default: T,
        callback: (T) -> Unit,
    ): SettingsListener

    fun asPropertyValue(value: T): String = value?.toString().orEmpty()
}

object BooleanAdapter : Adapter<Boolean> {
    override fun get(
        settings: ObservableSettings,
        key: String,
        default: Boolean,
    ) = settings[key, default]

    override fun set(
        settings: ObservableSettings,
        key: String,
        value: Boolean,
    ) {
        settings[key] = value
    }

    override fun addListener(
        settings: ObservableSettings,
        key: String,
        default: Boolean,
        callback: (Boolean) -> Unit,
    ) = settings.addBooleanListener(key, default, callback)
}

object IntAdapter : Adapter<Int> {
    override fun get(
        settings: ObservableSettings,
        key: String,
        default: Int,
    ) = settings[key, default]

    override fun set(
        settings: ObservableSettings,
        key: String,
        value: Int,
    ) {
        settings[key] = value
    }

    override fun addListener(
        settings: ObservableSettings,
        key: String,
        default: Int,
        callback: (Int) -> Unit,
    ) = settings.addIntListener(key, default, callback)
}

object IntOrNullAdapter : Adapter<Int?> {
    override fun get(
        settings: ObservableSettings,
        key: String,
        default: Int?,
    ): Int? = settings.getIntOrNull(key) ?: default

    override fun set(
        settings: ObservableSettings,
        key: String,
        value: Int?,
    ) {
        settings[key] = value
    }

    override fun addListener(
        settings: ObservableSettings,
        key: String,
        default: Int?,
        callback: (Int?) -> Unit,
    ) = settings.addIntOrNullListener(key, callback)
}

object StringAdapter : Adapter<String> {
    override fun get(
        settings: ObservableSettings,
        key: String,
        default: String,
    ) = settings[key, default]

    override fun set(
        settings: ObservableSettings,
        key: String,
        value: String,
    ) {
        settings[key] = value
    }

    override fun addListener(
        settings: ObservableSettings,
        key: String,
        default: String,
        callback: (String) -> Unit,
    ) = settings.addStringListener(key, default, callback)
}

object StringOrNullAdapter : Adapter<String?> {
    override fun get(
        settings: ObservableSettings,
        key: String,
        default: String?,
    ) = settings.getStringOrNull(key) ?: default

    override fun set(
        settings: ObservableSettings,
        key: String,
        value: String?,
    ) {
        settings[key] = value
    }

    override fun addListener(
        settings: ObservableSettings,
        key: String,
        default: String?,
        callback: (String?) -> Unit,
    ) = settings.addStringOrNullListener(key, callback)
}

class SerializableAdapter<E>(
    private val serialize: (E) -> String,
    private val deserialize: (String) -> E,
) : Adapter<E> {
    override fun get(
        settings: ObservableSettings,
        key: String,
        default: E,
    ) = settings.getStringOrNull(key)?.let { deserialize(it) } ?: default

    override fun set(
        settings: ObservableSettings,
        key: String,
        value: E,
    ) {
        settings[key] = serialize(value)
    }

    override fun addListener(
        settings: ObservableSettings,
        key: String,
        default: E,
        callback: (E) -> Unit,
    ) = settings.addStringOrNullListener(key) {
        val value =
            if (it != null) {
                deserialize(it)
            } else {
                default
            }
        callback(value)
    }

    override fun asPropertyValue(value: E): String = serialize(value)
}
