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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.FocusListenerEvent
import suwayomi.tachidesk.launcher.KeyListenerEvent
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.focusListener
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import java.awt.event.KeyEvent
import javax.swing.JFileChooser
import javax.swing.UIManager
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isWritable

@Suppress("ktlint:standard:function-naming")
fun LocalSource(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Local Source path") {
        isEditable = false
    }.bind()
    val localSourcePathField =
        jTextField(vm.localSourcePath.value) {
            toolTipText = "default: \"\""
            focusListener()
                .filterIsInstance<FocusListenerEvent.Lost>()
                .combine(
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .filter { it.event?.keyCode == KeyEvent.VK_ENTER },
                ) { _, _ -> }
                .map {
                    text?.trim()
                }.onEach {
                    if (it.isNullOrBlank() ||
                        runCatching {
                            Path(it).createDirectories().isWritable()
                        }.getOrElse { false }
                    ) {
                        vm.localSourcePath.value = it.orEmpty()
                    }
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
                        vm.localSourcePath.value = path
                        localSourcePathField.text = path
                    }
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
