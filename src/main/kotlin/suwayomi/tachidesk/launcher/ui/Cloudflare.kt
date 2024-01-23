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
import java.net.URL
import javax.swing.SpinnerNumberModel

fun Cloudflare(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center")
    )
) {
    jCheckBox("Use FlareSolverr", selected = vm.flareSolverrEnabled.value) {
        toolTipText = "Use FlareSolverr instance to bypass Cloudflare." // todo improve
        actions()
            .onEach {
                vm.flareSolverrEnabled.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jTextArea("FlareSolverr URL") {
        isEditable = false
    }.bind()
    jTextField(vm.flareSolverrUrl.value) {
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }
            .onEach {
                if (!it.isNullOrBlank() && runCatching { URL(it).toURI() }.isSuccess) {
                    vm.flareSolverrUrl.value = it
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())

    jTextArea("FlareSolverr timeout") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.flareSolverrTimeout.value.coerceAtLeast(10), 10, Int.MAX_VALUE, 1)) {
        toolTipText = "Time limit in seconds for FlareSolverr to run, will fail if it goes over"
        changes()
            .onEach {
                vm.flareSolverrTimeout.value = (value as Int)
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
