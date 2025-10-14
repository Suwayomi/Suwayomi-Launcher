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
import suwayomi.tachidesk.launcher.jCheckBox
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

@Suppress("ktlint:standard:function-naming")
fun Backup(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Backups path") {
        isEditable = false
    }.bind()
    val backupPathField =
        jTextField(vm.backupPath.value) {
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
                        vm.backupPath.value = it.orEmpty()
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
                        vm.backupPath.value = path
                        backupPathField.text = path
                    }
                }
            }.flowOn(Dispatchers.Default)
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
        toolTipText =
            "default: \"00:00\" ; Daily backup time (HH:MM) ; range: [00:00, 23:59]"
        actions()
            .filter {
                text.count { it == ':' } == 1 &&
                    text
                        .substringBefore(':')
                        .let { it.isNotEmpty() && it.all { it.isDigit() } && it.toInt() in 0..23 } &&
                    text
                        .substringAfter(':')
                        .let { it.isNotEmpty() && it.all { it.isDigit() } && it.toInt() in 0..59 }
            }.onEach {
                vm.backupTime.value = text
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Backup Interval") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.backupInterval.value.coerceIn(0, 14), 0, 14, 1)) {
        toolTipText =
            "default: 1 ; range: [0, +∞] ; 0 == disabled ; Time in days"
        changes()
            .onEach {
                vm.backupInterval.value = value as Int
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())

    jTextArea("Backup TTL") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.backupTTL.value.coerceIn(0, 30), 0, 30, 1)) {
        toolTipText =
            "default: 14 ; range: [0, +∞] ; 0 == disabled ; Backup retention in days"
        changes()
            .onEach {
                vm.backupTTL.value = value as Int
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())

    jCheckBox("Auto-Backup Manga", selected = vm.autoBackupIncludeManga.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeManga.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jCheckBox("Auto-Backup Categories", selected = vm.autoBackupIncludeCategories.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeCategories.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jCheckBox("Auto-Backup Chapters", selected = vm.autoBackupIncludeChapters.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeChapters.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jCheckBox("Auto-Backup Tracking", selected = vm.autoBackupIncludeTracking.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeTracking.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jCheckBox("Auto-Backup History", selected = vm.autoBackupIncludeHistory.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeHistory.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jCheckBox("Auto-Backup Manga", selected = vm.autoBackupIncludeClientData.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeClientData.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jCheckBox("Auto-Backup Manga", selected = vm.autoBackupIncludeServerSettings.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.autoBackupIncludeServerSettings.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
}
