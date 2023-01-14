/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package suwayomi.tachidesk.launcher

import com.github.weisj.darklaf.LafManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import javax.swing.JFrame

@OptIn(DelicateCoroutinesApi::class) // FIXME: @Syer10 can this be written better?
fun main() {
    GlobalScope.launch(Dispatchers.Swing.immediate) {
        LafManager.installTheme(LafManager.getPreferredThemeStyle())

        jframe("Tachidesk-Server Launcher") {
            size = Dimension(800, 600)
            isResizable = false
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            contentPane = jpanel {
                layout = MigLayout(LC().fill())
                jpanel {
                    jbutton("Launch") {
                        addActionListener {
                            println("Clicked")
                        }
                    }.also { add(it) }
                }.also { add(it, "south") }

            }
        }
    }
}