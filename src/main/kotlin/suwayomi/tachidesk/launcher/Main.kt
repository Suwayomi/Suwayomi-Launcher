package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.github.weisj.darklaf.LafManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.settings.LauncherSettings.WebUIInterface
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SpinnerNumberModel

suspend fun main() {
    val scope = MainScope()
    val vm = LauncherViewModel()

    withContext(Dispatchers.Swing.immediate) {
        LafManager.installTheme(LafManager.getPreferredThemeStyle())

        jframe("Tachidesk-Server Launcher") {
            size = Dimension(800, 600)
            isResizable = false
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane = jpanel(MigLayout(LC().fill())) {
                jpanel(
                    GridLayout(0, 2)
                ) {
                    ServerIpAndPortBindings(vm, scope).bind()
                    Socks5(vm, scope).bind()
                    WebUI(vm, scope).bind()
                    BasicAuth(vm, scope).bind()
                    Misc(vm, scope).bind()
                }.bind("center")
                jpanel {
                    jbutton("Launch") {
                        actions()
                            .onEach {
                                vm.launch()
                            }
                            .flowOn(Dispatchers.Default)
                            .launchIn(scope)
                    }.bind()
                }.bind("south")
            }
        }
    }
}

fun ServerIpAndPortBindings(vm: LauncherViewModel, scope: CoroutineScope): JPanel {
    return jpanel(
        FlowLayout().apply {
            hgap = 0
            vgap = 0
        }
    ) {
        jpanel(GridLayout(0, 1)) {
            /*
            TODO
             - Warning when changing this value
             - Format checking to display an error when its an invalid ip
             */
            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("IP") {
                    isEditable = false
                }.bind()
                jTextField(vm.ip.value) {
                    toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value"
                    actions()
                        .onEach {
                            vm.ip.value = text
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind()
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("Port") {
                    isEditable = false
                }.bind()
                jSpinner(SpinnerNumberModel(vm.port.value, 0, Int.MAX_VALUE, 1)) {
                    toolTipText = "Which port to use the server, 4567 is the default"
                    changes()
                        .onEach {
                            vm.port.value = value as Int
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                }.bind()
            }.bind()
        }.bind()
    }
}

fun Socks5(vm: LauncherViewModel, scope: CoroutineScope): JPanel {
    return jpanel(
        FlowLayout().apply {
            hgap = 0
            vgap = 0
        }
    ) {
        jpanel(GridLayout(0, 1)) {
            jCheckBox("Socks5 Proxy", selected = vm.socksProxyEnabled.value) {
                // todo toolTipText = "Use this to toggle extra logging to the console window to help debug issues."
                actions()
                    .onEach {
                        vm.socksProxyEnabled.value = isSelected
                    }
                    .flowOn(Dispatchers.Default)
                    .launchIn(scope)
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
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
                    // todo toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value"
                    actions()
                        .onEach {
                            vm.socksProxyHost.value = text
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind()
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("Socks5 Port") {
                    isEditable = false
                }.bind()
                jSpinner(SpinnerNumberModel(vm.socksProxyPort.value ?: 0, 0, Int.MAX_VALUE, 1)) {
                    // todo toolTipText = "Which port to use the server, 4567 is the default"
                    isEnabled = vm.socksProxyEnabled.value
                    vm.socksProxyEnabled
                        .onEach {
                            isEnabled = it
                        }
                        .launchIn(scope)
                    changes()
                        .onEach {
                            vm.socksProxyPort.value = (value as Int).takeUnless { it == 0 }
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                }.bind()
            }.bind()
        }.bind()
    }
}

fun WebUI(vm: LauncherViewModel, scope: CoroutineScope): JPanel {
    return jpanel(
        FlowLayout().apply {
            hgap = 0
            vgap = 0
        }
    ) {
        jpanel(GridLayout(0, 1)) {
            jCheckBox("WebUI", selected = vm.webUIEnabled.value) {
                // todo toolTipText = "Use this to toggle extra logging to the console window to help debug issues."
                actions()
                    .onEach {
                        vm.webUIEnabled.value = isSelected
                    }
                    .flowOn(Dispatchers.Default)
                    .launchIn(scope)
            }.bind()
            jCheckBox("Open in browser", selected = vm.initialOpenInBrowserEnabled.value) {
                // todo toolTipText = "Use this to toggle extra logging to the console window to help debug issues."
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
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("WebUI Interface") {
                    isEditable = false
                }.bind()
                jComboBox(WebUIInterface.values()) {
                    selectedItem = vm.webUIInterface.value
                    vm.webUIEnabled
                        .onEach {
                            isEnabled = it
                        }
                        .launchIn(scope)
                    actions()
                        .onEach {
                            vm.webUIInterface.value = selectedItem as WebUIInterface
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                }.bind()
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("Electron path") {
                    isEditable = false
                }.bind()
                jTextField(vm.electronPath.value.orEmpty()) {
                    isEnabled = vm.webUIEnabled.value
                    vm.webUIEnabled
                        .onEach {
                            isEnabled = it
                        }
                        .launchIn(scope)
                    // todo toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .onEach {
                            vm.electronPath.value = text?.takeUnless { it.isBlank() }?.trim()
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind()
            }.bind()
        }.bind()
    }
}

fun BasicAuth(vm: LauncherViewModel, scope: CoroutineScope): JPanel {
    return jpanel(
        FlowLayout().apply {
            hgap = 0
            vgap = 0
        }
    ) {
        jpanel(GridLayout(0, 1)) {
            jCheckBox("Basic Authentication", selected = vm.basicAuthEnabled.value) {
                // todo toolTipText = "Use this to toggle extra logging to the console window to help debug issues."
                actions()
                    .onEach {
                        vm.basicAuthEnabled.value = isSelected
                    }
                    .flowOn(Dispatchers.Default)
                    .launchIn(scope)
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("Username") {
                    isEditable = false
                }.bind()
                jTextField(vm.basicAuthUsername.value) {
                    isEnabled = vm.basicAuthEnabled.value
                    vm.basicAuthEnabled
                        .onEach {
                            isEnabled = it
                        }
                        .launchIn(scope)
                    // todo toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .onEach {
                            vm.basicAuthUsername.value = text?.takeUnless { it.isBlank() }?.trim()
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind()
            }.bind()

            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("Password") {
                    isEditable = false
                }.bind()
                jPasswordField(vm.basicAuthPassword.value) {
                    isEnabled = vm.basicAuthEnabled.value
                    vm.basicAuthEnabled
                        .onEach {
                            isEnabled = it
                        }
                        .launchIn(scope)
                    // todo toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .onEach {
                            vm.basicAuthPassword.value = password.toString().takeUnless { it.isBlank() }?.trim()
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind()
            }.bind()
        }.bind()
    }
}

fun Misc(vm: LauncherViewModel, scope: CoroutineScope): JPanel {
    return jpanel(
        FlowLayout().apply {
            hgap = 0
            vgap = 0
        }
    ) {
        jpanel(GridLayout(0, 1)) {
            jCheckBox("Debug logging", selected = vm.debug.value) {
                toolTipText = "Use this to toggle extra logging to the console window to help debug issues."
                actions()
                    .onEach {
                        vm.debug.value = isSelected
                    }
                    .flowOn(Dispatchers.Default)
                    .launchIn(scope)
            }.bind()

            jCheckBox("System Tray", selected = vm.systemTray.value) {
                toolTipText = "Use this to toggle Tachidesk showing in the system tray."
                actions()
                    .onEach {
                        vm.systemTray.value = isSelected
                    }
                    .flowOn(Dispatchers.Default)
                    .launchIn(scope)
            }.bind()
            jpanel(
                FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                    hgap = 0
                    vgap = 0
                }
            ) {
                jTextArea("Downloads path") {
                    isEditable = false
                }.bind()
                jTextField(vm.downloadsPath.value.orEmpty()) {
                    // todo toolTipText = "Where to expose the server, 0.0.0.0 is the default and suggested value"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .onEach {
                            vm.downloadsPath.value = text?.takeUnless { it.isBlank() }?.trim()
                        }
                        .flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind()
            }.bind()
        }.bind()
    }
}
