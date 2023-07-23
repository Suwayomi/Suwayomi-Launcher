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
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import javax.swing.JFileChooser
import javax.swing.UIManager

fun RootDir(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center")
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
}
