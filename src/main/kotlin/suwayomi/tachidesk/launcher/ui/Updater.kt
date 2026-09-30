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

@Suppress("ktlint:standard:function-naming")
fun Updater(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    val spinner =
        jSpinner(
            SpinnerNumberModel(
                vm.globalUpdateInterval.value.coerceIn(6.0, 168.0),
                6.0,
                168.0,
                0.5,
            ),
        ) {
            toolTipText =
                "default: 12.0 ; range: [6.0, +∞] ; 0.0 == disabled ; Time in hours" // todo improve
            changes()
                .onEach {
                    vm.globalUpdateInterval.value = value as Double
                }.flowOn(Dispatchers.Default)
                .launchIn(scope)
            if (vm.globalUpdateInterval.value == 0.0) {
                isEnabled = false
                value = 12.0
            }
        }

    jCheckBox("Global Update", selected = vm.globalUpdateInterval.value != 0.0) {
        toolTipText = "default: 12.0 ; range: [6.0, +∞] ; 0.0 == disabled ; Time in hours"
        actions()
            .onEach {
                vm.globalUpdateInterval.value =
                    if (isSelected) {
                        spinner.value as Double
                    } else {
                        0.0
                    }
                spinner.isEnabled = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("Global Update Interval") {
        isEditable = false
    }.bind()
    spinner.bind(CC().grow().spanX())
}
