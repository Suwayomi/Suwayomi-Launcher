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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jpanel
import javax.swing.SpinnerNumberModel

fun Updater(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center")
    )
) {
    jTextArea("Max Parallel Update Requests") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.maxParallelUpdateRequests.value.coerceIn(1, 20), 1, 20, 1)) {
        toolTipText = "Sets how many sources can be updated in parallel. Updates are grouped by source and all mangas of a source are updated synchronously" // todo improve
        changes()
            .onEach {
                vm.maxParallelUpdateRequests.value = value as Int
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())

    jCheckBox("Exclude unread chapters", selected = vm.excludeUnreadChapters.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.excludeUnreadChapters.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jCheckBox("Exclude not started", selected = vm.excludeNotStarted.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.excludeNotStarted.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jCheckBox("Exclude completed", selected = vm.excludeCompleted.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.excludeCompleted.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    val spinner = jSpinner(SpinnerNumberModel(vm.globalUpdateInterval.value.coerceIn(6.0, 168.0), 6.0, 168.0, 0.5)) {
        toolTipText = "Time in hours, the interval in which the global update will be automatically triggered" // todo improve
        changes()
            .onEach {
                vm.globalUpdateInterval.value = value as Double
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        if (vm.globalUpdateInterval.value == 0.0) {
            isEnabled = false
            value = 12.0
        }
    }

    jCheckBox("Global Update", selected = vm.globalUpdateInterval.value != 0.0) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.globalUpdateInterval.value = if (isSelected) {
                    spinner.value as Double
                } else {
                    0.0
                }
                spinner.isEnabled = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("Global Update Interval") {
        isEditable = false
    }.bind()
    spinner.bind(CC().grow().spanX())
}
