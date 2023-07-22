package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.settings.ThemeSettings
import com.github.weisj.darklaf.theme.Theme
import com.github.weisj.darklaf.theme.event.ThemeChangeEvent
import com.github.weisj.darklaf.theme.event.ThemeChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.settings.LauncherSettings.WebUIFlavor
import suwayomi.tachidesk.launcher.settings.LauncherSettings.WebUIInterface
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.SpinnerNumberModel
import javax.swing.UIManager

suspend fun main() {
    val scope = MainScope()
    val vm = LauncherViewModel()

    withContext(Dispatchers.Swing.immediate) {
        setupTheme(vm)

        jframe("Tachidesk-Server Launcher") {
            size = Dimension(800, 600)
            isResizable = false
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane = jpanel(MigLayout(LC().fill())) {
                jpanel(
                    FlowLayout().apply {
                        alignment = FlowLayout.TRAILING
                    }
                ) {
                    jbutton("Theme") {
                        actions()
                            .onEach {
                                ThemeSettings.showSettingsDialog(this)
                            }
                            .flowOn(Dispatchers.Default)
                            .launchIn(scope)
                    }.bind()
                }.bind("north")
                jpanel(
                    MigLayout(
                        LC().wrap().fill().insetsAll("16"),
                        AC().count(1).align("leading").gap("20"),
                        AC().align("center").gap("20")
                    )
                ) {
                    Directories(vm, scope).bind()
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

fun setupTheme(vm: LauncherViewModel) {
    vm.theme.value?.let {
        try {
            val theme = Base64.getDecoder().decode(it).inputStream().use {
                ObjectInputStream(it).use {
                    it.readObject() as Theme
                }
            }
            LafManager.setTheme(theme)
        } catch (e: Exception) {
            LafManager.setTheme(LafManager.getPreferredThemeStyle())
        }
    } ?: LafManager.setTheme(LafManager.getPreferredThemeStyle())
    LafManager.install()
    LafManager.addThemeChangeListener(
        object : ThemeChangeListener {
            override fun themeChanged(e: ThemeChangeEvent) {}
            override fun themeInstalled(e: ThemeChangeEvent) {
                ByteArrayOutputStream().use { it ->
                    ObjectOutputStream(it).use {
                        it.writeObject(e.newTheme)
                    }
                    vm.theme.value = Base64.getEncoder().encodeToString(it.toByteArray())
                }
            }
        }
    )
}

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
    jSpinner(SpinnerNumberModel(vm.port.value, 0, Int.MAX_VALUE, 1)) {
        toolTipText = "Which port to use the server, 4567 is the default" // todo improve
        changes()
            .onEach {
                vm.port.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
}

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
    jSpinner(SpinnerNumberModel(vm.socksProxyPort.value.toIntOrNull() ?: 0, 0, Int.MAX_VALUE, 1)) {
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
    jComboBox(WebUIFlavor.values()) {
        selectedItem = WebUIFlavor.values().find { it.name.equals(vm.webUIFlavor.value, true) }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.webUIFlavor.value = (selectedItem as WebUIFlavor).name
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
    jComboBox(WebUIInterface.values()) {
        selectedItem = WebUIInterface.values().find { it.name.equals(vm.webUIInterface.value, true) }
        vm.webUIEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.webUIInterface.value = (selectedItem as WebUIInterface).name.lowercase()
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

fun BasicAuth(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    jCheckBox("Basic Authentication", selected = vm.basicAuthEnabled.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.basicAuthEnabled.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

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
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.basicAuthUsername.value = text?.trim().orEmpty()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())

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
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.basicAuthPassword.value = password?.toString()?.trim().orEmpty()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX())
}

fun Misc(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    jCheckBox("Debug logging", selected = vm.debug.value) {
        toolTipText = "Use this to toggle extra logging to the console window to help debug issues." // todo improve
        actions()
            .onEach {
                vm.debug.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jCheckBox("System Tray", selected = vm.systemTray.value) {
        toolTipText = "Use this to toggle Tachidesk showing in the system tray." // todo improve
        actions()
            .onEach {
                vm.systemTray.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind()
}

fun Directories(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().fill()
    )
) {
    jTextArea("Root path") {
        isEditable = false
    }.bind()
    val rootDirField = jTextField(vm.rootDir.value.orEmpty()) {
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.rootDir.value = text?.takeUnless { it.isBlank() }?.trim()
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
                        rootDirField.text = path
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jTextArea("Downloads path") {
        isEditable = false
    }.bind()
    val downloadsPathField = jTextField(vm.downloadsPath.value) {
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.downloadsPath.value = text?.trim().orEmpty()
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
                        downloadsPathField.text = path
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
