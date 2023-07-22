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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import net.miginfocom.layout.AC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.ui.BasicAuth
import suwayomi.tachidesk.launcher.ui.Directories
import suwayomi.tachidesk.launcher.ui.Misc
import suwayomi.tachidesk.launcher.ui.ServerIpAndPortBindings
import suwayomi.tachidesk.launcher.ui.Socks5
import suwayomi.tachidesk.launcher.ui.WebUI
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64
import javax.swing.JFrame

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
