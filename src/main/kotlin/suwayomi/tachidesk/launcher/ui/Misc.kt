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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.KeyListenerEvent
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import javax.swing.SpinnerNumberModel

@Suppress("ktlint:standard:function-naming")
fun Misc(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jCheckBox("Debug logging", selected = vm.debug.value) {
        toolTipText =
            "Use this to toggle extra logging to the console window to help debug issues." // todo improve
        actions()
            .onEach {
                vm.debug.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jCheckBox("System Tray", selected = vm.systemTray.value) {
        toolTipText = "Use this to toggle Suwayomi showing in the system tray." // todo improve
        actions()
            .onEach {
                vm.systemTray.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Max log files") {
        isEditable = false
    }.bind()
    jSpinner(
        SpinnerNumberModel(
            vm.maxLogFiles.value.coerceAtLeast(10),
            0,
            Int.MAX_VALUE,
            1,
        ),
    ) {
        toolTipText = "The max number of days to keep files before they get deleted"
        changes()
            .onEach {
                vm.maxLogFiles.value = (value as Int)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    val regex = "([0-9]+)\\s*(|kb|mb|gb)s?".toRegex(RegexOption.IGNORE_CASE)

    jTextArea("Max Log File Size") {
        isEditable = false
    }.bind()
    jTextField(vm.maxLogFileSize.value) {
        toolTipText = "The max size of a log file - possible values: 1 (bytes), 1KB (kilobytes), 1MB (megabytes), 1GB (gigabytes)"
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank() && regex.matches(it.trim())) {
                    vm.maxLogFileSize.value = it.trim()
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())
    jTextArea("Max Log Folder Size") {
        isEditable = false
    }.bind()
    jTextField(vm.maxLogFolderSize.value) {
        toolTipText = "The max size of all saved log files - possible values: 1 (bytes), 1KB (kilobytes), 1MB (megabytes), 1GB (gigabytes)"
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank() && regex.matches(it.trim())) {
                    vm.maxLogFolderSize.value = it.trim()
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())
}
