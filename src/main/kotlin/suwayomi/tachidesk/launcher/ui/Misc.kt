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
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jpanel

fun Misc(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center")
    )
) {
    jCheckBox("Debug logging", selected = vm.debug.value) {
        toolTipText = "Use this to toggle extra logging to the console window to help debug issues." // todo improve
        actions()
            .onEach {
                vm.debug.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jCheckBox("GraphQL Debug logging", selected = vm.gqlDebug.value) {
        toolTipText = "Use this to toggle extra logging to the console window to help debug graphql issues." // todo improve
        actions()
            .onEach {
                vm.gqlDebug.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jCheckBox("System Tray", selected = vm.systemTray.value) {
        toolTipText = "Use this to toggle Tachidesk showing in the system tray." // todo improve
        actions()
            .onEach {
                vm.systemTray.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind()
}
