package suwayomi.tachidesk.launcher.config

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory
import com.typesafe.config.parser.ConfigDocument
import com.typesafe.config.parser.ConfigDocumentFactory
import java.io.File
import java.nio.file.FileSystems
import kotlin.io.path.Path
import kotlin.io.path.readText

class ConfigManager(rootDir: String) {
    private val userConfigFile = File(rootDir, "server.conf")

    private fun getUserConfig(): Config {
        return userConfigFile.let {
            ConfigFactory.parseFile(it)
        }
    }

    var config: Config = getUserConfig()
        private set

    /**
     * Makes sure the "UserConfig" is up-to-date.
     *
     *  - adds missing settings
     *  - removes outdated settings
     */
    fun updateUserConfig() {
        val serverConfigFileContent = FileSystems.newFileSystem(Path("bin/Tachidesk-Server.jar"), null as ClassLoader?).use {
            it.getPath("/server-reference.conf").readText()
        }

        val serverConfig = ConfigFactory.parseString(serverConfigFileContent)
        val userConfig = getUserConfig()

        val hasMissingSettings = serverConfig.entrySet().any { !userConfig.hasPath(it.key) }
        val hasOutdatedSettings = userConfig.entrySet().any { !serverConfig.hasPath(it.key) }
        val isUserConfigOutdated = hasMissingSettings || hasOutdatedSettings
        if (!isUserConfigOutdated) {
            return
        }

        // logger.debug { "user config is out of date, updating... (missingSettings= $hasMissingSettings, outdatedSettings= $hasOutdatedSettings" }

        val serverConfigDoc = ConfigDocumentFactory.parseString(serverConfigFileContent)
        userConfigFile.writeText(serverConfigDoc.render())

        var newUserConfigDoc: ConfigDocument = serverConfigDoc
        userConfig.entrySet().filter { serverConfig.hasPath(it.key) }.forEach { newUserConfigDoc = newUserConfigDoc.withValue(it.key, it.value) }

        userConfigFile.writeText(newUserConfigDoc.render())
    }

    private fun updateUserConfigFile(path: String, value: ConfigValue) {
        val userConfigDoc = ConfigDocumentFactory.parseFile(userConfigFile)
        val updatedConfigDoc = userConfigDoc.withValue(path, value)
        val newFileContent = updatedConfigDoc.render()
        userConfigFile.writeText(newFileContent)
    }

    fun updateValue(path: String, value: Any) {
        val configValue = ConfigValueFactory.fromAnyRef(value)

        updateUserConfigFile(path, configValue)

        config = getUserConfig()
    }
}
