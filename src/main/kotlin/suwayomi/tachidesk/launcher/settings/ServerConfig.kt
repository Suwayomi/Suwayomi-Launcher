package suwayomi.tachidesk.launcher.settings

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.harawata.appdirs.AppDirsFactory
import kotlin.io.path.Path
import kotlin.io.path.exists

@Serializable
data class ServerConfig(
    val server: Server
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun get(rootDir: String?): Server {
            val resolvedRootDir = rootDir
                ?: AppDirsFactory.getInstance().getUserDataDir("Tachidesk", null, null)

            val path = Path(resolvedRootDir).resolve("server.conf")
            return if (path.exists()) {
                val config = ConfigFactory.parseFile(path.toFile())
                Hocon.decodeFromConfig<ServerConfig>(config).server
            } else {
                Server()
            }
        }
    }
}

@Serializable
data class Server(
    // Server ip and port bindings
    val ip: String = "0.0.0.0",
    val port: Int = 4567,
    // Socks5 proxy
    val socksProxyEnabled: Boolean = false,
    val socksProxyHost: String = "",
    val socksProxyPort: String = "",
    // webUI
    val webUIEnabled: Boolean = true,
    val initialOpenInBrowserEnabled: Boolean = true,
    val webUIInterface: String = "browser", // "browser" or "electron",
    val electronPath: String = "",
    // Authentication
    val basicAuthEnabled: Boolean = false,
    val basicAuthUsername: String = "",
    val basicAuthPassword: String = "",
    // Misc
    val debugLogsEnabled: Boolean = false,
    val systemTrayEnabled: Boolean = true,
    val downloadsPath: String = ""
)
