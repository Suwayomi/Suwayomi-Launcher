package suwayomi.tachidesk.launcher.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.settings.LauncherSettings
import javax.swing.JFileChooser
import javax.swing.UIManager

fun WebUI(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    jCheckBox("WebUI", selected = vm.webUIEnabled.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.webUIEnabled.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jTextArea("WebUI Flavor") {
        isEditable = false
    }.bind()
    jComboBox(LauncherSettings.WebUIFlavor.values()) {
        selectedItem = LauncherSettings.WebUIFlavor.values().find { it.name.equals(vm.webUIFlavor.value, true) }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.webUIFlavor.value = (selectedItem as LauncherSettings.WebUIFlavor).name
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jCheckBox("Open in browser", selected = vm.initialOpenInBrowserEnabled.value) {
        // todo toolTipText = ""
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        actions()
            .onEach {
                vm.initialOpenInBrowserEnabled.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("WebUI Interface") {
        isEditable = false
    }.bind()
    jComboBox(LauncherSettings.WebUIInterface.values()) {
        selectedItem = LauncherSettings.WebUIInterface.values().find { it.name.equals(vm.webUIInterface.value, true) }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.webUIInterface.value = (selectedItem as LauncherSettings.WebUIInterface).name.lowercase()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Electron path") {
        isEditable = false
    }.bind()
    val textField = jTextField(vm.electronPath.value.orEmpty()) {
        isEnabled = vm.webUIEnabled.value
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.electronPath.value = text?.trim().orEmpty()
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
                        vm.rootDir.value = path
                        textField.text = path
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
