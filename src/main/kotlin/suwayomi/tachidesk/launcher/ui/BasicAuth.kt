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
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jPasswordField
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener

fun BasicAuth(vm: LauncherViewModel, scope: CoroutineScope) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center")
    )
) {
    jCheckBox("Basic Authentication", selected = vm.basicAuthEnabled.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.basicAuthEnabled.value = isSelected
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("Username") {
        isEditable = false
    }.bind()
    jTextField(vm.basicAuthUsername.value) {
        isEnabled = vm.basicAuthEnabled.value
        vm.basicAuthEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.basicAuthUsername.value = text?.trim().orEmpty()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Password") {
        isEditable = false
    }.bind()
    jPasswordField(vm.basicAuthPassword.value) {
        isEnabled = vm.basicAuthEnabled.value
        vm.basicAuthEnabled
            .onEach {
                isEnabled = it
            }
            .launchIn(scope)
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.basicAuthPassword.value = password?.toString()?.trim().orEmpty()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX())
}
