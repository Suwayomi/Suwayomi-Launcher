package suwayomi.tachidesk.launcher.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.KeyListenerEvent
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import javax.swing.JFileChooser
import javax.swing.SpinnerNumberModel
import javax.swing.UIManager

fun Backup(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    jTextArea("Backups path") {
        isEditable = false
    }.bind()
    val downloadsPathField = jTextField(vm.backupPath.value) {
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.backupPath.value = text?.trim().orEmpty()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind()
    jbutton(icon = UIManager.getIcon("FileView.directoryIcon")) {
        // todo toolTipText = ""
        actions()
            .onEach {
                val chooser = JFileChooser().apply {
                    val details = actionMap.get("viewTypeDetails")
                    details?.actionPerformed(null)
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                }
                when (chooser.showOpenDialog(this)) {
                    JFileChooser.APPROVE_OPTION -> {
                        val path = chooser.selectedFile.absolutePath
                        vm.backupPath.value = path
                        downloadsPathField.text = path
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    /*
    TODO
     - Warning when changing this value
     - Format checking to display an error when its an invalid ip
     */
    jTextArea("IP") {
        isEditable = false
    }.bind()
    jTextField(vm.backupTime.value) {
        toolTipText = "range: hour: 0-23, minute: 0-59 - default: \"00:00\" - time of day at which the automated backup should be triggered" // todo improve
        actions()
            .filter {
                text.count { it == ':' } == 1 &&
                    text.substringBefore(':').all { it.isDigit() } &&
                    text.substringAfter(':').all { it.isDigit() }
            }
            .onEach {
                vm.backupTime.value = text
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Backup Interval") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.backupInterval.value.coerceIn(0, 14), 0, 14, 1)) {
        toolTipText = "time in days - 0 to disable it - Interval in which the server will automatically create a backup." // todo improve
        changes()
            .onEach {
                vm.port.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())

    jTextArea("Backup TTL") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.backupInterval.value.coerceIn(0, 30), 0, 30, 1)) {
        toolTipText = "time in days - 0 to disable it - How long backup files will be kept before they will get deleted." // todo improve
        changes()
            .onEach {
                vm.port.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
}
