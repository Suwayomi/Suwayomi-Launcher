package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.github.weisj.darklaf.LafManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import javax.swing.JFrame
import kotlin.system.exitProcess

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
                jpanel {
                    jCheckBox("Debug logging", selected = vm.debug.value) {
                        actions()
                            .onEach {
                                vm.setDebug(isSelected)
                            }
                            .flowOn(Dispatchers.Default)
                            .launchIn(scope)
                    }.bind()
                }.bind("north")
                jpanel {
                    jbutton("Launch") {
                        actions()
                            .onEach {
                                println("Clicked")
                                exitProcess(0)
                            }
                            .flowOn(Dispatchers.Default)
                            .launchIn(scope)
                    }.bind()
                }.bind("south")
            }
        }
    }
}
