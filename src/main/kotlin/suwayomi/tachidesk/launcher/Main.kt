package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import com.github.weisj.darklaf.LafManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

@OptIn(DelicateCoroutinesApi::class) // FIXME: @Syer10 can this be written better?
fun main() {
    GlobalScope.launch(Dispatchers.Swing.immediate) {
        LafManager.install()

        jframe("Tachidesk-Server Launcher") {
            size = Dimension(800, 600)
            isResizable = false
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane = jpanel {
                layout = BorderLayout()
                jpanel {
                    jbutton("Launch") {
                        addActionListener {
                            println("Clicked")
                        }
                    }.also { add(it) }
                }.also { add(it, BorderLayout.SOUTH) }

            }
        }
    }
}