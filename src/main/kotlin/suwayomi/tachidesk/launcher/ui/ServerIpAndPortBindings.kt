package suwayomi.tachidesk.launcher.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
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
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import javax.swing.SpinnerNumberModel

fun ServerIpAndPortBindings(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    /*
    TODO
     - Warning when changing this value
     - Format checking to display an error when its an invalid ip
     */
    jTextArea("IP") {
        isEditable = false
    }.bind()
    jTextField(vm.ip.value) {
        toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value" // todo improve
        actions()
            .filter {
                text.count { it == '.' } == 4 &&
                    text.split('.').all {
                        val int = it.toIntOrNull()
                        int != null && int in 0..255
                    }
            }
            .onEach {
                vm.ip.value = text
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Port") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.port.value.coerceAtLeast(0), 0, Int.MAX_VALUE, 1)) {
        toolTipText = "Which port to use the server, 4567 is the default" // todo improve
        changes()
            .onEach {
                vm.port.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
}
