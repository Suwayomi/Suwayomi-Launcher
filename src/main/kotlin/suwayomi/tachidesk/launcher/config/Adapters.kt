package suwayomi.tachidesk.launcher.config

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
        return configValue.unwrapped() as Int
    }
}

object BooleanConfigAdapter : ConfigAdapter<Boolean> {
    override fun toType(configValue: ConfigValue): Boolean {
        return configValue.unwrapped() as Boolean
    }
}

object DoubleConfigAdapter : ConfigAdapter<Double> {
    override fun toType(configValue: ConfigValue): Double {
        return configValue.unwrapped() as Double
    }
}
