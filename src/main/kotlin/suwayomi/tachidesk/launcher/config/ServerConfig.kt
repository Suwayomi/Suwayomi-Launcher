package suwayomi.tachidesk.launcher.config

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import io.github.config4k.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import suwayomi.tachidesk.launcher.settings.LauncherSettings.AuthMode
import kotlin.reflect.KProperty

class ServerConfig(
    val scope: CoroutineScope,
    val configManager: ConfigManager,
) {
    open inner class OverrideConfigValue {
        var flow: MutableStateFlow<Any>? = null

        inline operator fun <reified T : MutableStateFlow<R>, reified R> getValue(
            thisRef: ServerConfig,
            property: KProperty<*>,
        ): T {
            if (flow != null) {
                return flow as T
            }

            val stateFlow =
                configManager.config
                    .getConfig("server")
                    .getValue<ServerConfig, T>(thisRef, property)
            @Suppress("UNCHECKED_CAST")
            flow = stateFlow as MutableStateFlow<Any>

            stateFlow
                .drop(1)
                .distinctUntilChanged()
                .onEach { configManager.updateValue("server.${property.name}", it as Any) }
                .launchIn(scope)

            return stateFlow
        }
    }

    val ip: MutableStateFlow<String> by OverrideConfigValue()
    val port: MutableStateFlow<Int> by OverrideConfigValue()

    // proxy
    val socksProxyEnabled: MutableStateFlow<Boolean> by OverrideConfigValue()
    val socksProxyVersion: MutableStateFlow<Int> by OverrideConfigValue()
    val socksProxyHost: MutableStateFlow<String> by OverrideConfigValue()
    val socksProxyPort: MutableStateFlow<String> by OverrideConfigValue()
    val socksProxyUsername: MutableStateFlow<String> by OverrideConfigValue()
    val socksProxyPassword: MutableStateFlow<String> by OverrideConfigValue()

    // webUI
    val webUIEnabled: MutableStateFlow<Boolean> by OverrideConfigValue()
    val webUIFlavor: MutableStateFlow<String> by OverrideConfigValue()
    val initialOpenInBrowserEnabled: MutableStateFlow<Boolean> by OverrideConfigValue()
    val webUIInterface: MutableStateFlow<String> by OverrideConfigValue()
    val electronPath: MutableStateFlow<String> by OverrideConfigValue()
    val webUIChannel: MutableStateFlow<String> by OverrideConfigValue()
    val webUIUpdateCheckInterval: MutableStateFlow<Double> by OverrideConfigValue()

    // downloader
    val downloadAsCbz: MutableStateFlow<Boolean> by OverrideConfigValue()
    val downloadsPath: MutableStateFlow<String> by OverrideConfigValue()
    val autoDownloadNewChapters: MutableStateFlow<Boolean> by OverrideConfigValue()
    val excludeEntryWithUnreadChapters: MutableStateFlow<Boolean> by OverrideConfigValue()
    val autoDownloadNewChaptersLimit: MutableStateFlow<Int> by OverrideConfigValue()
    val autoDownloadIgnoreReUploads: MutableStateFlow<Boolean> by OverrideConfigValue()
    val downloadConversions: MutableStateFlow<Map<String, DownloadConversion>> by OverrideConfigValue()

    data class DownloadConversion(
        val target: String,
        val compressionLevel: Float? = null,
    )

    // extension
    val extensionRepos: MutableStateFlow<List<String>> by OverrideConfigValue()

    // requests
    val maxSourcesInParallel: MutableStateFlow<Int> by OverrideConfigValue()

    // updater
    val excludeUnreadChapters: MutableStateFlow<Boolean> by OverrideConfigValue()
    val excludeNotStarted: MutableStateFlow<Boolean> by OverrideConfigValue()
    val excludeCompleted: MutableStateFlow<Boolean> by OverrideConfigValue()
    val globalUpdateInterval: MutableStateFlow<Double> by OverrideConfigValue()
    val updateMangas: MutableStateFlow<Boolean> by OverrideConfigValue()

    // Authentication
    val authMode: MutableStateFlow<AuthMode> by OverrideConfigValue()
    val authUsername: MutableStateFlow<String> by OverrideConfigValue()
    val authPassword: MutableStateFlow<String> by OverrideConfigValue()

    // misc
    val debugLogsEnabled: MutableStateFlow<Boolean> by OverrideConfigValue()
    val systemTrayEnabled: MutableStateFlow<Boolean> by OverrideConfigValue()

    // backup
    val backupPath: MutableStateFlow<String> by OverrideConfigValue()
    val backupTime: MutableStateFlow<String> by OverrideConfigValue()
    val backupInterval: MutableStateFlow<Int> by OverrideConfigValue()
    val backupTTL: MutableStateFlow<Int> by OverrideConfigValue()

    // local source
    val localSourcePath: MutableStateFlow<String> by OverrideConfigValue()

    // cloudflare bypass
    val flareSolverrEnabled: MutableStateFlow<Boolean> by OverrideConfigValue()
    val flareSolverrUrl: MutableStateFlow<String> by OverrideConfigValue()
    val flareSolverrTimeout: MutableStateFlow<Int> by OverrideConfigValue()
    val flareSolverrSessionName: MutableStateFlow<String> by OverrideConfigValue()
    val flareSolverrSessionTtl: MutableStateFlow<Int> by OverrideConfigValue()
    val flareSolverrAsResponseFallback: MutableStateFlow<Boolean> by OverrideConfigValue()

    // opds settings
    val opdsUseBinaryFileSizes: MutableStateFlow<Boolean> by OverrideConfigValue()
    val opdsItemsPerPage: MutableStateFlow<Int> by OverrideConfigValue()
    val opdsEnablePageReadProgress: MutableStateFlow<Boolean> by OverrideConfigValue()
    val opdsMarkAsReadOnDownload: MutableStateFlow<Boolean> by OverrideConfigValue()
    val opdsShowOnlyUnreadChapters: MutableStateFlow<Boolean> by OverrideConfigValue()
    val opdsShowOnlyDownloadedChapters: MutableStateFlow<Boolean> by OverrideConfigValue()
    val opdsChapterSortOrder: MutableStateFlow<String> by OverrideConfigValue()
}
