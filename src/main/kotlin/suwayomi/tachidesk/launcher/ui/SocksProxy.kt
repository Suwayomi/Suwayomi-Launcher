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
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jPasswordField
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import javax.swing.SpinnerNumberModel

@Suppress("ktlint:standard:function-naming")
fun Socks5(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jCheckBox("Socks Proxy", selected = vm.socksProxyEnabled.value) {
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.socksProxyEnabled.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jSpinner(SpinnerNumberModel(vm.socksProxyVersion.value.coerceAtLeast(4), 4, 5, 1)) {
        changes()
            .onEach {
                vm.socksProxyVersion.value = (value as Int)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    /*
    TODO
     - Validate host maybe?
     */
    jTextArea("Socks Host") {
        isEditable = false
    }.bind()
    jTextField(vm.socksProxyHost.value) {
        isEnabled = vm.socksProxyEnabled.value
        vm.socksProxyEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        // todo toolTipText = ""
        actions()
            .onEach {
                vm.socksProxyHost.value = text
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Socks Port") {
        isEditable = false
    }.bind()
    jSpinner(
        SpinnerNumberModel(
            vm.socksProxyPort.value
                .toIntOrNull()
                ?.coerceAtLeast(0) ?: 0,
            0,
            Int.MAX_VALUE,
            1,
        ),
    ) {
        // todo toolTipText = ""
        isEnabled = vm.socksProxyEnabled.value
        vm.socksProxyEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        changes()
            .onEach {
                vm.socksProxyPort.value =
                    (value as Int).takeUnless { it == 0 }?.toString().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Username") {
        isEditable = false
    }.bind()
    jTextField(vm.socksProxyUsername.value) {
        isEnabled = vm.socksProxyEnabled.value
        vm.socksProxyEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.socksProxyUsername.value = text?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Password") {
        isEditable = false
    }.bind()
    jPasswordField(vm.socksProxyPassword.value) {
        isEnabled = vm.socksProxyEnabled.value
        vm.socksProxyEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        // todo toolTipText = ""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.socksProxyPassword.value = password?.concatToString()?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX())
}
