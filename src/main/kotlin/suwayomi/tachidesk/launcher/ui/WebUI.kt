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

@Suppress("ktlint:standard:function-naming")
fun WebUI(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jCheckBox("WebUI", selected = vm.webUIEnabled.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.webUIEnabled.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jTextArea("WebUI Flavor") {
        isEditable = false
    }.bind()
    jComboBox(LauncherSettings.WebUIFlavor.entries.toTypedArray()) {
        selectedItem =
            LauncherSettings.WebUIFlavor.entries.find { it == vm.webUIFlavor.value }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: WEBUI"
        actions()
            .onEach {
                vm.webUIFlavor.value = (selectedItem as LauncherSettings.WebUIFlavor)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jCheckBox("Open in browser", selected = vm.initialOpenInBrowserEnabled.value) {
        toolTipText = "default: true ; Open client on startup"
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        actions()
            .onEach {
                vm.initialOpenInBrowserEnabled.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("WebUI Interface") {
        isEditable = false
    }.bind()
    jComboBox(LauncherSettings.WebUIInterface.entries.toTypedArray()) {
        selectedItem =
            LauncherSettings.WebUIInterface.entries.find {
                it == vm.webUIInterface.value
            }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: BROWSER"
        actions()
            .onEach {
                vm.webUIInterface.value =
                    (selectedItem as LauncherSettings.WebUIInterface)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Electron path") {
        isEditable = false
    }.bind()
    val textField =
        jTextField(vm.electronPath.value) {
            isEnabled = vm.webUIEnabled.value
            vm.webUIEnabled
                .onEach {
                    isEnabled = it
                }.launchIn(scope)
            toolTipText = "default: \"\""
            keyListener()
                .filterIsInstance<KeyListenerEvent.Released>()
                .onEach {
                    vm.electronPath.value = text?.trim().orEmpty()
                }.flowOn(Dispatchers.Default)
                .launchIn(scope)
            columns = 10
        }.bind()
    jbutton(icon = UIManager.getIcon("FileView.directoryIcon")) {
        toolTipText = "default: \"\""
        actions()
            .onEach {
                val chooser =
                    JFileChooser().apply {
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
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jTextArea("WebUI Channel") {
        isEditable = false
    }.bind()
    jComboBox(LauncherSettings.WebUIChannel.entries.toTypedArray()) {
        selectedItem =
            LauncherSettings.WebUIChannel.entries.find {
                it == vm.webUIChannel.value
            }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText =
            "default: STABLE ; \"BUNDLED\" (the version bundled with the server release), \"STABLE\" or \"PREVIEW\" - the WebUI version that should be used"
        actions()
            .onEach {
                vm.webUIChannel.value =
                    (selectedItem as LauncherSettings.WebUIChannel)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    val spinner =
        jSpinner(
            SpinnerNumberModel(
                vm.webUIUpdateCheckInterval.value.coerceIn(1.0, 23.0),
                1.0,
                23.0,
                0.5,
            ),
        ) {
            toolTipText =
                "default: 23.0 ; range: [0.0, 23.0] ; 0.0 == disabled ; Time in hours, how often the server should check for WebUI updates"
            changes()
                .onEach {
                    vm.webUIUpdateCheckInterval.value = value as Double
                }.flowOn(Dispatchers.Default)
                .launchIn(scope)
            if (vm.webUIUpdateCheckInterval.value == 0.0) {
                isEnabled = false
                value = 12.0
            }
        }

    jCheckBox("WebUI Updates", selected = vm.webUIUpdateCheckInterval.value != 0.0) {
        toolTipText = "default: 23.0 ; range: [0.0, 23.0] ; 0.0 == disabled ; Time in hours"
        actions()
            .onEach {
                vm.webUIUpdateCheckInterval.value =
                    if (isSelected) {
                        spinner.value as Double
                    } else {
                        0.0
                    }
                spinner.isEnabled = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("WebUI Update Interval") {
        isEditable = false
    }.bind()
    spinner.bind(CC().grow().spanX())

    jTextArea("WebUI SubPath") {
        isEditable = false
    }.bind()
    jTextField(vm.webUISubpath.value) {
        val regex = "^(/[a-zA-Z0-9._-]+)*$".toRegex()
        toolTipText =
            "default: \"\" ; Serve WebUI under a subpath (e.g., /manga). Leave empty for root path. Must start with / if specified."
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                if (text?.matches(regex) == true) {
                    vm.webUISubpath.value = text?.trim().orEmpty()
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())
}
