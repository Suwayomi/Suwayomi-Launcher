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
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.focusListener
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import java.awt.event.KeyEvent
import javax.swing.JFileChooser
import javax.swing.SpinnerNumberModel
import javax.swing.UIManager
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isWritable

fun Backup(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Backups path") {
        isEditable = false
    }.bind()
    val backupPathField = jTextField(vm.backupPath.value) {
        // todo toolTipText = ""
        focusListener()
            .filterIsInstance<FocusListenerEvent.Lost>()
            .combine(
                keyListener()
                    .filterIsInstance<KeyListenerEvent.Released>()
                    .filter { it.event?.keyCode == KeyEvent.VK_ENTER },
            ) { _, _ -> }
            .map {
                text?.trim()
            }
            .onEach {
                if (it.isNullOrBlank() || runCatching { Path(it).createDirectories().isWritable() }.getOrElse { false }) {
                    vm.backupPath.value = it.orEmpty()
                }
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
                        vm.backupPath.value = path
                        backupPathField.text = path
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    /*
    TODO
     - Warning when changing this value
     - Format checking to display an error when its an invalid ip
     */
    jTextArea("Backup Time") {
        isEditable = false
    }.bind()
    jTextField(vm.backupTime.value) {
        toolTipText = "range: hour: 0-23, minute: 0-59 - default: \"00:00\" - time of day at which the automated backup should be triggered" // todo improve
        actions()
            .filter {
                text.count { it == ':' } == 1 &&
                    text.substringBefore(':').let { it.isNotEmpty() && it.all { it.isDigit() } && it.toInt() in 0..23 } &&
                    text.substringAfter(':').let { it.isNotEmpty() && it.all { it.isDigit() } && it.toInt() in 0..59 }
            }
            .onEach {
                vm.backupTime.value = text
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Backup Interval") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.backupInterval.value.coerceIn(0, 14), 0, 14, 1)) {
        toolTipText = "time in days - 0 to disable it - Interval in which the server will automatically create a backup." // todo improve
        changes()
            .onEach {
                vm.backupInterval.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())

    jTextArea("Backup TTL") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.backupTTL.value.coerceIn(0, 30), 0, 30, 1)) {
        toolTipText = "time in days - 0 to disable it - How long backup files will be kept before they will get deleted." // todo improve
        changes()
            .onEach {
                vm.backupTTL.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
}
