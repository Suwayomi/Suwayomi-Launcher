package suwayomi.tachidesk.launcher.config

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import com.typesafe.config.parser.ConfigDocument
import com.typesafe.config.parser.ConfigDocumentFactory
import io.github.config4k.toConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import suwayomi.tachidesk.launcher.settings.LauncherSettings.AuthMode
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ConfigManager(
    private val tachideskServer: Path,
    rootDir: String,
) {
    private val userConfigFile = getServerConf(rootDir)
    val logger = KotlinLogging.logger {}

    init {
        updateUserConfig()
    }

    private fun getUserConfig(): Config =
        userConfigFile.let {
            ConfigFactory.parseFile(it.toFile())
        }

    var config: Config = getUserConfig()
        private set

    fun <T : Any> migrateConfig(
        configDocument: ConfigDocument,
        config: Config,
        configKey: String,
        toConfigKey: String,
        toType: (ConfigValue) -> T?,
    ): ConfigDocument {
        try {
            val configValue = config.getValue(configKey)
            val typedValue = toType(configValue)
            if (typedValue != null) {
                return configDocument.withValue(
                    toConfigKey,
                    typedValue.toConfig("internal").getValue("internal"),
                )
            }
        } catch (_: ConfigException) {
            // ignore, likely already migrated
        }

        return configDocument
    }

    fun migrate(
        configDocument: ConfigDocument,
        config: Config,
    ): ConfigDocument {
        var updatedConfig = configDocument
        updatedConfig =
            migrateConfig(
                updatedConfig,
                config,
                "server.basicAuthEnabled",
                "server.authMode",
                toType = {
                    if (it.unwrapped() as? Boolean == true) {
                        AuthMode.BASIC_AUTH.name
                    } else {
                        null
                    }
                },
            )
        updatedConfig =
            migrateConfig(
                updatedConfig,
                config,
                "server.basicAuthUsername",
                "server.authUsername",
                toType = { it.unwrapped() as? String },
            )
        updatedConfig =
            migrateConfig(
                updatedConfig,
                config,
                "server.basicAuthPassword",
                "server.authPassword",
                toType = { it.unwrapped() as? String },
            )
        return updatedConfig
    }

    /**
     * Makes sure the "UserConfig" is up-to-date.
     *
     *  - adds missing settings
     *  - removes outdated settings
     */
    private fun updateUserConfig() {
        val serverConfigFileContent = getDefaultConfig(tachideskServer)
        if (!userConfigFile.exists()) {
            userConfigFile.createParentDirectories().writeText(serverConfigFileContent)
        }

        val serverConfig = ConfigFactory.parseString(serverConfigFileContent)
        val userConfig = getUserConfig()
        // NOTE: if more than 1 dot is included, that's a nested setting, which we need to filter out here
        val refKeys =
            serverConfig.root().entries.flatMap {
                (it.value as? ConfigObject)?.entries?.map { e -> "${it.key}.${e.key}" }.orEmpty()
            }
        val hasMissingSettings = refKeys.any { !userConfig.hasPath(it) }
        val hasOutdatedSettings = userConfig.entrySet().any { !refKeys.contains(it.key) && it.key.count { c -> c == '.' } <= 1 }
        val isUserConfigOutdated = hasMissingSettings || hasOutdatedSettings
        if (!isUserConfigOutdated) {
            return
        }

        logger.debug {
            "user config is out of date, updating... (missingSettings= $hasMissingSettings, outdatedSettings= $hasOutdatedSettings"
        }

        val serverConfigDoc = ConfigDocumentFactory.parseString(serverConfigFileContent)
        userConfigFile.writeText(serverConfigDoc.render())

        var newUserConfigDoc: ConfigDocument = serverConfigDoc
        userConfig
            .entrySet()
            .filter {
                serverConfig.hasPath(
                    it.key,
                ) ||
                    it.key.count { c -> c == '.' } > 1
            }.forEach { newUserConfigDoc = newUserConfigDoc.withValue(it.key, it.value) }

        newUserConfigDoc =
            migrate(newUserConfigDoc, userConfig)

        userConfigFile.writeText(newUserConfigDoc.render())
        config = getUserConfig()
    }

    private fun updateUserConfigFile(
        path: String,
        value: ConfigValue,
    ) {
        val userConfigDoc = ConfigDocumentFactory.parseFile(userConfigFile.toFile())
        val updatedConfigDoc = userConfigDoc.withValue(path, value)
        val newFileContent = updatedConfigDoc.render()
        userConfigFile.writeText(newFileContent)
    }

    private val configMutex = Mutex()

    suspend fun updateValue(
        path: String,
        value: Any,
    ) {
        configMutex.withLock {
            val configValue = value.toConfig("internal").getValue("internal")

            updateUserConfigFile(path, configValue)

            config = getUserConfig()
        }
    }

    companion object {
        private fun getDefaultConfig(tachideskServer: Path): String =
            FileSystems.newFileSystem(tachideskServer, null as ClassLoader?).use {
                it.getPath("/server-reference.conf").readText()
            }

        fun getServerConf(rootDir: String) = Path(rootDir, "server.conf")

        fun resetConfig(
            tachideskServer: Path,
            rootDir: String,
        ) {
            val userConfigFile = getServerConf(rootDir)
            userConfigFile.createParentDirectories().writeText(getDefaultConfig(tachideskServer))
        }

        fun deleteConfig(rootDir: String) = getServerConf(rootDir).deleteIfExists()
    }
}
