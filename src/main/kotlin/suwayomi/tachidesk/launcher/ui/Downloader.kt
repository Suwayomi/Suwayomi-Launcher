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

fun Downloader(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jCheckBox("Download as CBZ", selected = vm.downloadAsCbz.value) {
        toolTipText = "Download chapters as CBZ files." // todo improve
        actions()
            .onEach {
                vm.downloadAsCbz.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jTextArea("Downloads path") {
        isEditable = false
    }.bind()
    val downloadsPathField = jTextField(vm.downloadsPath.value) {
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
                    vm.downloadsPath.value = it.orEmpty()
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
                        vm.downloadsPath.value = path
                        downloadsPathField.text = path
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jCheckBox("Download new chapters", selected = vm.autoDownloadNewChapters.value) {
        toolTipText = "If new chapters that have been found, should Suwayomi download them automatically." // todo improve
        actions()
            .onEach {
                vm.autoDownloadNewChapters.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jCheckBox("Exclude unread entries", selected = vm.excludeEntryWithUnreadChapters.value) {
        toolTipText = "Ignore automatic chapter downloads of entries with unread chapters." // todo improve
        actions()
            .onEach {
                vm.excludeEntryWithUnreadChapters.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jCheckBox("Exclude re-uploaded entries", selected = vm.autoDownloadIgnoreReUploads.value) {
        toolTipText = "Ignore automatic chapter downloads of entries that are already uploaded." // todo improve
        actions()
            .onEach {
                vm.autoDownloadIgnoreReUploads.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jTextArea("Download new chapters limit") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.autoDownloadNewChaptersLimit.value.coerceAtLeast(0), 0, Int.MAX_VALUE, 1)) {
        toolTipText = "0 to disable it - How many unread downloaded chapters should be available - If the limit is reached, new chapters won't be downloaded automatically"
        changes()
            .onEach {
                vm.autoDownloadNewChaptersLimit.value = (value as Int)
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
