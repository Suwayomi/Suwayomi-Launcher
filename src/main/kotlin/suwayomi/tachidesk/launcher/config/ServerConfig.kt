package suwayomi.tachidesk.launcher.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ServerConfig(
    private val scope: CoroutineScope,
    private val configManager: ConfigManager
) {

    inner class OverrideConfigValue<T : Any>(private val configAdapter: ConfigAdapter<T>) : ReadOnlyProperty<ServerConfig, MutableStateFlow<T>> {
        private var flow: MutableStateFlow<T>? = null

        override fun getValue(thisRef: ServerConfig, property: KProperty<*>): MutableStateFlow<T> {
            val path = "server.${property.name}"
            if (flow != null) {
                return flow!!
            }

            val value = configManager.config.getValue(path)
            val stateFlow = MutableStateFlow(configAdapter.toType(value))
            flow = stateFlow

            stateFlow
                .drop(1)
                .onEach {
                    configManager.updateValue(path, it)
                }
                .launchIn(scope)

            return stateFlow
        }
    }

    val ip: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val port: MutableStateFlow<Int> by OverrideConfigValue(IntConfigAdapter)

    // proxy
    val socksProxyEnabled: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val socksProxyHost: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val socksProxyPort: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)

    // webUI
    val webUIEnabled: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val webUIFlavor: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val initialOpenInBrowserEnabled: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val webUIInterface: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val electronPath: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val webUIChannel: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val webUIUpdateCheckInterval: MutableStateFlow<Double> by OverrideConfigValue(DoubleConfigAdapter)

    // downloader
    val downloadAsCbz: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val downloadsPath: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val autoDownloadNewChapters: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)

    // updater
    val maxParallelUpdateRequests: MutableStateFlow<Int> by OverrideConfigValue(IntConfigAdapter)
    val excludeUnreadChapters: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val excludeNotStarted: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val excludeCompleted: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val globalUpdateInterval: MutableStateFlow<Double> by OverrideConfigValue(DoubleConfigAdapter)

    // Authentication
    val basicAuthEnabled: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val basicAuthUsername: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val basicAuthPassword: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)

    // misc
    val debugLogsEnabled: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)
    val systemTrayEnabled: MutableStateFlow<Boolean> by OverrideConfigValue(BooleanConfigAdapter)

    // backup
    val backupPath: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val backupTime: MutableStateFlow<String> by OverrideConfigValue(StringConfigAdapter)
    val backupInterval: MutableStateFlow<Int> by OverrideConfigValue(IntConfigAdapter)
    val backupTTL: MutableStateFlow<Int> by OverrideConfigValue(IntConfigAdapter)
}
