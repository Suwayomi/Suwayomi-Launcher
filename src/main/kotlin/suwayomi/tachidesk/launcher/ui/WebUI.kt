package suwayomi.tachidesk.launcher.ui

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.settings.LauncherSettings
import javax.swing.JFileChooser
import javax.swing.SpinnerNumberModel
import javax.swing.UIManager

fun WebUI(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
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
    jComboBox(LauncherSettings.WebUIFlavor.entries.toTypedArray()) {
        selectedItem = LauncherSettings.WebUIFlavor.entries.find { it.name.equals(vm.webUIFlavor.value, true) }
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
    jComboBox(LauncherSettings.WebUIInterface.entries.toTypedArray()) {
        selectedItem = LauncherSettings.WebUIInterface.entries.find { it.name.equals(vm.webUIInterface.value, true) }
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
    val textField = jTextField(vm.electronPath.value) {
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
    jTextArea("WebUI Channel") {
        isEditable = false
    }.bind()
    jComboBox(LauncherSettings.WebUIChannel.entries.toTypedArray()) {
        selectedItem = LauncherSettings.WebUIChannel.entries.find { it.name.equals(vm.webUIChannel.value, true) }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        toolTipText = "\"bundled\" (the version bundled with the server release), \"stable\" or \"preview\" - the WebUI version that should be used"
        actions()
            .onEach {
                vm.webUIChannel.value = (selectedItem as LauncherSettings.WebUIChannel).name.lowercase()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    val spinner = jSpinner(SpinnerNumberModel(vm.webUIUpdateCheckInterval.value.coerceIn(1.0, 23.0), 1.0, 23.0, 0.5)) {
        toolTipText = "Time in hours, how often the server should check for WebUI updates" // todo improve
        changes()
            .onEach {
                vm.webUIUpdateCheckInterval.value = value as Double
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        if (vm.webUIUpdateCheckInterval.value == 0.0) {
            isEnabled = false
            value = 12.0
        }
    }

    jCheckBox("WebUI Updates", selected = vm.webUIUpdateCheckInterval.value != 0.0) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.webUIUpdateCheckInterval.value = if (isSelected) {
                    spinner.value as Double
                } else {
                    0.0
                }
                spinner.isEnabled = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("WebUI Update Interval") {
        isEditable = false
    }.bind()
    spinner.bind(CC().grow().spanX())
}
