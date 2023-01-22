package suwayomi.tachidesk.launcher

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.asStateFlow
import suwayomi.tachidesk.launcher.settings.LauncherSettings

class LauncherViewModel {
    private val scope = MainScope()
    private val settings = LauncherSettings()

    private val _debug = settings.debugLogs().asStateFlow(scope)
    val debug = _debug.asStateFlow()

    fun setDebug(value: Boolean) {
        _debug.value = value
    }
}
