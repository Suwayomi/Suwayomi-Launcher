package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.harawata.appdirs.AppDirsFactory
import suwayomi.tachidesk.launcher.config.ConfigManager
import suwayomi.tachidesk.launcher.config.ServerConfig
import suwayomi.tachidesk.launcher.settings.LauncherSettings
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.system.exitProcess

class LauncherViewModel {
    private val scope = MainScope()

    private val homeDir: Path = Path(this::class.java.protectionDomain.codeSource.location.toURI().path).parent
    private val tachideskServer = homeDir / "bin" / "Tachidesk-Server.jar"

    init {
        require(tachideskServer.exists()) {
            "Could not find Tachidesk-Server.jar at '${tachideskServer.absolutePathString()}'"
        }
    }

    private val settings = LauncherSettings()
    val rootDir = settings.rootDir().asStateFlow(scope)
    private val config = rootDir.drop(1)
        .map(::getServerConfig)
        .stateIn(scope, SharingStarted.Eagerly, getServerConfig(settings.rootDir().get()))

    // Server ip and port bindings
    val ip = config.asStateFlow { it.ip }
    val port = config.asStateFlow { it.port }

    // Socks5 proxy
    val socksProxyEnabled = config.asStateFlow { it.socksProxyEnabled }
    val socksProxyHost = config.asStateFlow { it.socksProxyHost }
    val socksProxyPort = config.asStateFlow { it.socksProxyPort }

    // WebUI
    val webUIEnabled = config.asStateFlow { it.webUIEnabled }
    val webUIFlavor = config.asStateFlow { it.webUIFlavor }
    val initialOpenInBrowserEnabled = config.asStateFlow { it.initialOpenInBrowserEnabled }
    val webUIInterface = config.asStateFlow { it.webUIInterface }
    val electronPath = config.asStateFlow { it.electronPath }
    val webUIChannel = config.asStateFlow { it.webUIChannel }
    val webUIUpdateCheckInterval = config.asStateFlow { it.webUIUpdateCheckInterval }

    // Downloader
    val downloadAsCbz = config.asStateFlow { it.downloadAsCbz }
    val downloadsPath = config.asStateFlow { it.downloadsPath }
    val autoDownloadNewChapters = config.asStateFlow { it.autoDownloadNewChapters }

    // Updater
    val maxParallelUpdateRequests = config.asStateFlow { it.maxParallelUpdateRequests }
    val excludeUnreadChapters = config.asStateFlow { it.excludeUnreadChapters }
    val excludeNotStarted = config.asStateFlow { it.excludeNotStarted }
    val excludeCompleted = config.asStateFlow { it.excludeCompleted }
    val globalUpdateInterval = config.asStateFlow { it.globalUpdateInterval }

    // Authentication
    val basicAuthEnabled = config.asStateFlow { it.basicAuthEnabled }
    val basicAuthUsername = config.asStateFlow { it.basicAuthUsername }
    val basicAuthPassword = config.asStateFlow { it.basicAuthPassword }

    // Misc
    val debug = config.asStateFlow { it.debugLogsEnabled }
    val systemTray = config.asStateFlow { it.systemTrayEnabled }

    // Backup
    val backupPath = config.asStateFlow { it.backupPath }
    val backupTime = config.asStateFlow { it.backupTime }
    val backupInterval = config.asStateFlow { it.backupInterval }
    val backupTTL = config.asStateFlow { it.backupTTL }

    val theme = settings.theme().asStateFlow(scope)

    fun launch() {
        val os = System.getProperty("os.name").lowercase()
        val javaPath = if (os.startsWith("mac os x")) {
            homeDir / "jre/Contents/Home/bin/java"
        } else if (os.startsWith("windows")) {
            homeDir / "jre/bin/javaw"
        } else {
            // Probably linux.
            val javaPath = homeDir / "jre/bin/java"
            if (javaPath.exists()) {
                javaPath
            } else {
                Path("/usr/bin/java")
            }
        }
        val java = if (!javaPath.exists()) {
            println("Java executable was not found! Defaulting to 'java'")
            "java"
        } else {
            javaPath.absolutePathString()
        }

        val jarFile = tachideskServer.absolutePathString()
        val properties = settings.getProperties().toMutableList()
        if (webUIInterface.value.equals("electron", true) && electronPath.value.isBlank()) {
            val electronPath = if (os.startsWith("mac os x")) {
                homeDir / "electron/Electron.app/Contents/MacOS/Electron"
            } else if (os.startsWith("windows")) {
                homeDir / "electron/electron.exe"
            } else {
                // Probably linux.
                val electronPath = homeDir / "electron/electron"
                if (electronPath.exists()) {
                    electronPath
                } else {
                    Path("/usr/bin/electron")
                }
            }
            if (electronPath.exists()) {
                properties += "-Dsuwayomi.tachidesk.config.server.electronPath=${electronPath.absolutePathString()}"
            } else {
                println("Electron executable was not found! In order to run this launcher, you may need Electron installed.")
            }
        }

        ProcessBuilder(java, *properties.toTypedArray(), "-jar", jarFile).start()
        exitProcess(0)
    }

    private fun getServerConfig(rootDir: String?): ServerConfig {
        val resolvedRootDir = rootDir
            ?: AppDirsFactory.getInstance().getUserDataDir("Tachidesk", null, null)

        val configManager = ConfigManager(tachideskServer, resolvedRootDir)

        configManager.updateUserConfig()

        return ServerConfig(scope, configManager)
    }

    private fun <T> StateFlow<ServerConfig>.asStateFlow(flow: (ServerConfig) -> MutableStateFlow<T>): MutableStateFlow<T> {
        val stateFlow = MutableStateFlow(flow(value).value)
        scope.launch {
            val latestFlow = map { flow(it) }.stateIn(this, SharingStarted.Eagerly, flow(value))
            stateFlow.collect {
                latestFlow.value.value = it
            }
        }
        return stateFlow
    }
}
