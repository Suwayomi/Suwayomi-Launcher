package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import kotlinx.coroutines.MainScope
import suwayomi.tachidesk.launcher.settings.LauncherSettings
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

class LauncherViewModel {
    private val scope = MainScope()
    private val settings = LauncherSettings()

    // Server ip and port bindings
    val ip = settings.ip().asStateFlow(scope)
    val port = settings.port().asStateFlow(scope)

    // Socks5 proxy
    val socksProxyEnabled = settings.socksProxyEnabled().asStateFlow(scope)
    val socksProxyHost = settings.socksProxyHost().asStateFlow(scope)
    val socksProxyPort = settings.socksProxyPort().asStateFlow(scope)

    // WebUI
    val webUIEnabled = settings.webUIEnabled().asStateFlow(scope)
    val webUIFlavor = settings.webUIFlavor().asStateFlow(scope)
    val initialOpenInBrowserEnabled = settings.initialOpenInBrowserEnabled().asStateFlow(scope)
    val webUIInterface = settings.webUIInterface().asStateFlow(scope)
    val electronPath = settings.electronPath().asStateFlow(scope)

    // Authentication
    val basicAuthEnabled = settings.basicAuthEnabled().asStateFlow(scope)
    val basicAuthUsername = settings.basicAuthUsername().asStateFlow(scope)
    val basicAuthPassword = settings.basicAuthPassword().asStateFlow(scope)

    // Misc
    val debug = settings.debugLogs().asStateFlow(scope)
    val systemTray = settings.systemTray().asStateFlow(scope)

    val rootDir = settings.rootDir().asStateFlow(scope)
    val downloadsPath = settings.downloadsPath().asStateFlow(scope)

    val theme = settings.theme().asStateFlow(scope)

    fun launch() {
        // todo validate
        val javaPath = Path("jre/bin/javaw").absolutePathString()
        val jarFile = Path("bin/Tachidesk-Server.jar").absolutePathString()
        val properties = settings.getProperties().toTypedArray()

        ProcessBuilder(javaPath, *properties, "-jar", jarFile).start()
        exitProcess(0)
    }
}
