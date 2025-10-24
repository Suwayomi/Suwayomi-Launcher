package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import ca.gosyer.appdirs.AppDirs
import io.github.config4k.registerCustomType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import suwayomi.tachidesk.launcher.config.ConfigManager
import suwayomi.tachidesk.launcher.config.DurationType
import suwayomi.tachidesk.launcher.config.MutableStateFlowType
import suwayomi.tachidesk.launcher.config.ServerConfig
import suwayomi.tachidesk.launcher.settings.LauncherPreference
import suwayomi.tachidesk.launcher.settings.LauncherSettings
import suwayomi.tachidesk.launcher.settings.LauncherSettings.CbzMediaType
import suwayomi.tachidesk.launcher.settings.LauncherSettings.DatabaseType
import suwayomi.tachidesk.launcher.settings.LauncherSettings.KoreaderSyncChecksumMethod
import suwayomi.tachidesk.launcher.settings.LauncherSettings.KoreaderSyncConflictStrategy
import suwayomi.tachidesk.launcher.settings.LauncherSettings.SortOrder
import suwayomi.tachidesk.launcher.util.checkIfPortInUse
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JOptionPane
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.system.exitProcess

class LauncherViewModel {
    private val scope = MainScope()

    init {
        require(tachideskServer.exists()) {
            "Could not find Suwayomi-Server.jar at '${tachideskServer.absolutePathString()}'"
        }
        registerCustomType(MutableStateFlowType())
        registerCustomType(DurationType())
    }

    private val settings = LauncherSettings()
    val rootDir = settings.rootDir().asStateFlow(scope)
    private val config =
        rootDir
            .drop(1)
            .map(::getServerConfig)
            .stateIn(scope, SharingStarted.Eagerly, getServerConfig(settings.rootDir().get()))

    // Server ip and port bindings
    val ip = config.asStateFlow { it.ip }
    val port = config.asStateFlow { it.port }

    // Socks5 proxy
    val socksProxyEnabled = config.asStateFlow { it.socksProxyEnabled }
    val socksProxyVersion = config.asStateFlow { it.socksProxyVersion }
    val socksProxyHost = config.asStateFlow { it.socksProxyHost }
    val socksProxyPort = config.asStateFlow { it.socksProxyPort }
    val socksProxyUsername = config.asStateFlow { it.socksProxyUsername }
    val socksProxyPassword = config.asStateFlow { it.socksProxyPassword }

    // WebUI
    val webUIEnabled = config.asStateFlow { it.webUIEnabled }
    val webUIFlavor = config.asStateFlow { it.webUIFlavor }
    val initialOpenInBrowserEnabled = config.asStateFlow { it.initialOpenInBrowserEnabled }
    val webUIInterface = config.asStateFlow { it.webUIInterface }
    val electronPath = config.asStateFlow { it.electronPath }
    val webUIChannel = config.asStateFlow { it.webUIChannel }
    val webUIUpdateCheckInterval = config.asStateFlow { it.webUIUpdateCheckInterval }
    val webUISubpath = config.asStateFlow { it.webUISubpath }

    // Downloader
    val downloadAsCbz = config.asStateFlow { it.downloadAsCbz }
    val downloadsPath = config.asStateFlow { it.downloadsPath }
    val autoDownloadNewChapters = config.asStateFlow { it.autoDownloadNewChapters }
    val excludeEntryWithUnreadChapters = config.asStateFlow { it.excludeEntryWithUnreadChapters }
    val autoDownloadNewChaptersLimit = config.asStateFlow { it.autoDownloadNewChaptersLimit }
    val autoDownloadIgnoreReUploads = config.asStateFlow { it.autoDownloadIgnoreReUploads }
    val downloadConversions = config.asStateFlow { it.downloadConversions }

    // Extension
    val extensionRepos = config.asStateFlow { it.extensionRepos }

    // Requests
    val maxSourcesInParallel = config.asStateFlow { it.maxSourcesInParallel }

    // Updater
    val excludeUnreadChapters = config.asStateFlow { it.excludeUnreadChapters }
    val excludeNotStarted = config.asStateFlow { it.excludeNotStarted }
    val excludeCompleted = config.asStateFlow { it.excludeCompleted }
    val globalUpdateInterval = config.asStateFlow { it.globalUpdateInterval }
    val updateMangas = config.asStateFlow { it.updateMangas }

    // Authentication
    val authMode = config.asStateFlow { it.authMode }
    val authUsername = config.asStateFlow { it.authUsername }
    val authPassword = config.asStateFlow { it.authPassword }
    val jwtAudience = config.asStateFlow { it.jwtAudience }
    val jwtTokenExpiry = config.asStateFlow { it.jwtTokenExpiry }
    val jwtRefreshExpiry = config.asStateFlow { it.jwtRefreshExpiry }

    // Misc
    val debug = config.asStateFlow { it.debugLogsEnabled }
    val systemTray = config.asStateFlow { it.systemTrayEnabled }
    val maxLogFiles = config.asStateFlow { it.maxLogFiles }
    val maxLogFileSize = config.asStateFlow { it.maxLogFileSize }
    val maxLogFolderSize = config.asStateFlow { it.maxLogFolderSize }

    // Backup
    val backupPath = config.asStateFlow { it.backupPath }
    val backupTime = config.asStateFlow { it.backupTime }
    val backupInterval = config.asStateFlow { it.backupInterval }
    val backupTTL = config.asStateFlow { it.backupTTL }
    val autoBackupIncludeManga = config.asStateFlow { it.autoBackupIncludeManga }
    val autoBackupIncludeCategories = config.asStateFlow { it.autoBackupIncludeCategories }
    val autoBackupIncludeChapters = config.asStateFlow { it.autoBackupIncludeChapters }
    val autoBackupIncludeTracking = config.asStateFlow { it.autoBackupIncludeTracking }
    val autoBackupIncludeHistory = config.asStateFlow { it.autoBackupIncludeHistory }
    val autoBackupIncludeClientData = config.asStateFlow { it.autoBackupIncludeClientData }
    val autoBackupIncludeServerSettings = config.asStateFlow { it.autoBackupIncludeServerSettings }

    // Local Source
    val localSourcePath = config.asStateFlow { it.localSourcePath }

    // Cloudflare bypass
    val flareSolverrEnabled = config.asStateFlow { it.flareSolverrEnabled }
    val flareSolverrUrl = config.asStateFlow { it.flareSolverrUrl }
    val flareSolverrTimeout = config.asStateFlow { it.flareSolverrTimeout }
    val flareSolverrSessionName = config.asStateFlow { it.flareSolverrSessionName }
    val flareSolverrSessionTtl = config.asStateFlow { it.flareSolverrSessionTtl }
    val flareSolverrAsResponseFallback = config.asStateFlow { it.flareSolverrAsResponseFallback }

    // opds settings
    val opdsUseBinaryFileSizes: MutableStateFlow<Boolean> = config.asStateFlow { it.opdsUseBinaryFileSizes }
    val opdsItemsPerPage: MutableStateFlow<Int> = config.asStateFlow { it.opdsItemsPerPage }
    val opdsEnablePageReadProgress: MutableStateFlow<Boolean> = config.asStateFlow { it.opdsEnablePageReadProgress }
    val opdsMarkAsReadOnDownload: MutableStateFlow<Boolean> = config.asStateFlow { it.opdsMarkAsReadOnDownload }
    val opdsShowOnlyUnreadChapters: MutableStateFlow<Boolean> = config.asStateFlow { it.opdsShowOnlyUnreadChapters }
    val opdsShowOnlyDownloadedChapters: MutableStateFlow<Boolean> = config.asStateFlow { it.opdsShowOnlyDownloadedChapters }
    val opdsChapterSortOrder: MutableStateFlow<SortOrder> = config.asStateFlow { it.opdsChapterSortOrder }
    val opdsCbzMimetype: MutableStateFlow<CbzMediaType> = config.asStateFlow { it.opdsCbzMimetype }

    // koreader sync
    val koreaderSyncServerUrl: MutableStateFlow<String> = config.asStateFlow { it.koreaderSyncServerUrl }
    val koreaderSyncUsername: MutableStateFlow<String> = config.asStateFlow { it.koreaderSyncUsername }
    val koreaderSyncUserkey: MutableStateFlow<String> = config.asStateFlow { it.koreaderSyncUserkey }
    val koreaderSyncDeviceId: MutableStateFlow<String> = config.asStateFlow { it.koreaderSyncDeviceId }
    val koreaderSyncChecksumMethod: MutableStateFlow<KoreaderSyncChecksumMethod> = config.asStateFlow { it.koreaderSyncChecksumMethod }
    val koreaderSyncPercentageTolerance: MutableStateFlow<Double> = config.asStateFlow { it.koreaderSyncPercentageTolerance }
    val koreaderSyncStrategyForward: MutableStateFlow<KoreaderSyncConflictStrategy> = config.asStateFlow { it.koreaderSyncStrategyForward }
    val koreaderSyncStrategyBackward: MutableStateFlow<KoreaderSyncConflictStrategy> =
        config.asStateFlow { it.koreaderSyncStrategyBackward }

    val databaseType: MutableStateFlow<DatabaseType> = config.asStateFlow { it.databaseType }
    val databaseUrl: MutableStateFlow<String> = config.asStateFlow { it.databaseUrl }
    val databaseUsername: MutableStateFlow<String> = config.asStateFlow { it.databaseUsername }
    val databasePassword: MutableStateFlow<String> = config.asStateFlow { it.databasePassword }

    val theme = settings.theme().asStateFlow(scope)

    fun launch(forceElectron: Boolean = false) {
        scope.launch(Dispatchers.Main.immediate) launchMain@{
            if (checkIfPortInUse(ip.value, port.value)) {
                val option =
                    JOptionPane.showOptionDialog(
                        null,
                        "The server is already running in the background. " +
                            "If you try to start it again, any changes you made won't be saved. " +
                            "Please close the current server before you click Continue. " +
                            "Or, you can continue without saving your changes.",
                        "Server found",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        arrayOf(
                            "Cancel",
                            "Continue",
                        ),
                        1,
                    )
                when (option) {
                    0 -> {
                        return@launchMain
                    }
                    1 -> Unit
                }
            }

            val os = System.getProperty("os.name").lowercase()
            val javaPath =
                if (os.startsWith("mac os x")) {
                    homeDir / "jre/bin/java"
                } else if (os.startsWith("windows")) {
                    if (debug.value) {
                        homeDir / "jre/bin/java.exe"
                    } else {
                        homeDir / "jre/bin/javaw.exe"
                    }
                } else {
                    // Probably linux.
                    val javaPath = homeDir / "jre/bin/java"
                    if (javaPath.exists()) {
                        javaPath
                    } else {
                        Path("/usr/bin/java")
                    }
                }
            val java =
                if (!javaPath.exists()) {
                    logger.info { "Java executable was not found! Defaulting to 'java'" }
                    "java"
                } else {
                    javaPath.absolutePathString()
                }

            logger.info { "Java path: $java" }

            val jarFile = tachideskServer.absolutePathString()
            val properties = settings.getProperties().toMutableList()
            if (
                (forceElectron || webUIInterface.value == LauncherSettings.WebUIInterface.Electron) &&
                (electronPath.value.isBlank() || Path(electronPath.value).notExists())
            ) {
                val electronPath =
                    if (os.startsWith("mac os x")) {
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
                logger.info { "Electron path: ${electronPath.absolutePathString()}" }
                if (electronPath.exists()) {
                    this@LauncherViewModel.electronPath.value = electronPath.absolutePathString()
                } else {
                    logger.info { "Electron executable was not found! Disabling Electron" }
                    this@LauncherViewModel.webUIInterface.value =
                        LauncherSettings.WebUIInterface.Browser
                }
            }

            if (forceElectron) {
                properties.add(LauncherPreference.ARG_PREFIX + "webUIInterface=electron")
            }

            logger.debug { "Properties:\n" + properties.joinToString(separator = "\n") }
            delay(100)
            ProcessBuilder(java, *properties.toTypedArray(), "--add-exports=java.desktop/sun.awt=ALL-UNNAMED", "-jar", jarFile).start()
            exitProcess(0)
        }
    }

    private fun getServerConfig(rootDir: String?): ServerConfig {
        val resolvedRootDir =
            rootDir
                ?: AppDirs { appName = "Tachidesk" }.getUserDataDir()

        val configManager = ConfigManager(tachideskServer, resolvedRootDir)

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

    companion object {
        private val homeDir: Path by lazy {
            Paths
                .get(
                    this::class.java.protectionDomain.codeSource.location
                        .toURI(),
                ).parent
        }
        private val tachideskServer by lazy {
            homeDir / "bin" / "Suwayomi-Server.jar"
        }

        private fun getRootDir(rootDir: String?): String = rootDir ?: AppDirs { appName = "Tachidesk" }.getUserDataDir()

        fun reset() {
            val settings = LauncherSettings()

            try {
                ConfigManager.resetConfig(tachideskServer, getRootDir(settings.rootDir().get()))
            } catch (_: Exception) {
                val rootDir = settings.rootDir().get()
                if (rootDir != null) {
                    settings.rootDir().set(null)
                }
            }
        }

        private val logger = KotlinLogging.logger {}
    }
}
