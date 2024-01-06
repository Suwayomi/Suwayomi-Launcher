package suwayomi.tachidesk.launcher.config

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.typesafe.config.ConfigValue

interface ConfigAdapter<T> {
    fun toType(configValue: ConfigValue): T
}

object StringConfigAdapter : ConfigAdapter<String> {
    override fun toType(configValue: ConfigValue): String {
        return configValue.unwrapped() as String
    }
}

object IntConfigAdapter : ConfigAdapter<Int> {
    override fun toType(configValue: ConfigValue): Int {
        return (configValue.unwrapped() as Number).toInt()
    }
}

object BooleanConfigAdapter : ConfigAdapter<Boolean> {
    override fun toType(configValue: ConfigValue): Boolean {
        return configValue.unwrapped() as Boolean
    }
}

object DoubleConfigAdapter : ConfigAdapter<Double> {
    override fun toType(configValue: ConfigValue): Double {
        return (configValue.unwrapped() as Number).toDouble()
    }
}

object StringListConfigAdapter : ConfigAdapter<List<String>> {
    override fun toType(configValue: ConfigValue): List<String> {
        @Suppress("UNCHECKED_CAST")
        return configValue.unwrapped() as List<String>
    }
}
