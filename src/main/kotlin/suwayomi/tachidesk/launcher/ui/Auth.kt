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
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jPasswordField
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.settings.LauncherSettings.AuthMode
import kotlin.time.Duration

@Suppress("ktlint:standard:function-naming")
fun Auth(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Auth mode") {
        isEditable = false
    }.bind()
    jComboBox(AuthMode.entries.toTypedArray()) {
        selectedItem = vm.authMode.value
        toolTipText = "default: NONE"
        actions()
            .onEach {
                vm.authMode.value = (selectedItem as AuthMode)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Username") {
        isEditable = false
    }.bind()
    jTextField(vm.authUsername.value) {
        isEnabled = vm.authMode.value != AuthMode.NONE
        vm.authMode
            .onEach {
                isEnabled = it != AuthMode.NONE
            }.launchIn(scope)
        toolTipText = "default: \"\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.authUsername.value = text?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Password") {
        isEditable = false
    }.bind()
    jPasswordField(vm.authPassword.value) {
        isEnabled = vm.authMode.value != AuthMode.NONE
        vm.authMode
            .onEach {
                isEnabled = it != AuthMode.NONE
            }.launchIn(scope)
        toolTipText = "default: \"\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.authPassword.value = password?.concatToString()?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX())

    jTextArea("JWT Audience") {
        isEditable = false
    }.bind()
    jTextField(vm.jwtAudience.value) {
        isEnabled = vm.authMode.value == AuthMode.UI_LOGIN
        vm.authMode
            .onEach {
                isEnabled = it == AuthMode.UI_LOGIN
            }.launchIn(scope)
        toolTipText = "default: \"suwayomi-server-api\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.authUsername.value = text?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())

    jTextArea("JWT Token Expiry") {
        isEditable = false
    }.bind()
    jTextField(vm.jwtTokenExpiry.value.toString()) {
        toolTipText = "default: 5m ; range: [0s, +∞]"
        isEnabled = vm.authMode.value == AuthMode.UI_LOGIN
        vm.authMode
            .onEach {
                isEnabled = it == AuthMode.UI_LOGIN
            }.launchIn(scope)
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank() && runCatching { Duration.parse(it) }.isSuccess) {
                    vm.jwtTokenExpiry.value = Duration.parse(it)
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())

    jTextArea("JWT Refresh Expiry") {
        isEditable = false
    }.bind()
    jTextField(vm.jwtRefreshExpiry.value.toString()) {
        toolTipText = "default: 60d ; range: [0s, +∞]"
        isEnabled = vm.authMode.value == AuthMode.UI_LOGIN
        vm.authMode
            .onEach {
                isEnabled = it == AuthMode.UI_LOGIN
            }.launchIn(scope)
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank() && runCatching { Duration.parse(it) }.isSuccess) {
                    vm.jwtRefreshExpiry.value = Duration.parse(it)
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())
}
