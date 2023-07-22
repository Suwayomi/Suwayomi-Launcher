package suwayomi.tachidesk.launcher.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import javax.swing.SpinnerNumberModel

fun Socks5(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    jCheckBox("Socks5 Proxy", selected = vm.socksProxyEnabled.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.socksProxyEnabled.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    /*
    TODO
     - Validate host maybe?
     */
    jTextArea("Socks5 Host") {
        isEditable = false
    }.bind()
    jTextField(vm.socksProxyHost.value) {
        isEnabled = vm.socksProxyEnabled.value
        vm.socksProxyEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.socksProxyHost.value = text
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Socks5 Port") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.socksProxyPort.value.toIntOrNull()?.coerceAtLeast(0) ?: 0, 0, Int.MAX_VALUE, 1)) {
        // todo toolTipText = ""
        isEnabled = vm.socksProxyEnabled.value
        vm.socksProxyEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        changes()
            .onEach {
                vm.socksProxyPort.value = (value as Int).takeUnless { it == 0 }?.toString().orEmpty()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
