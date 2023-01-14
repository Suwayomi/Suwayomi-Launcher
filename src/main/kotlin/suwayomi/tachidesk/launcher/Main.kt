/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package suwayomi.tachidesk.launcher

import com.github.weisj.darklaf.LafManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

fun main() {
    GlobalScope.launch(Dispatchers.Swing.immediate) {
        LafManager.install()

        JFrame("Tachidesk-Server Launcher").apply {
            size = Dimension(600, 400)
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane = JPanel().apply {
                layout = BorderLayout()
                //preferredSize = Dimension(600, 400)
                JPanel().apply {
                    JButton("Launch").apply {
                        addActionListener {
                            println("Clicked")
                        }
                    }.also { add(it) }
                }.also { add(it, BorderLayout.SOUTH) }

            }
            isVisible = true
        }
    }
}