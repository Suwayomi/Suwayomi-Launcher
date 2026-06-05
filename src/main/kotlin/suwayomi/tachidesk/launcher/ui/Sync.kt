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
import suwayomi.tachidesk.launcher.jCheckBox
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jPasswordField
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.settings.LauncherSettings.AuthMode
import kotlin.time.Duration

@Suppress("ktlint:standard:function-naming")
fun Sync(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jCheckBox("SyncYomi", selected = vm.syncYomiEnabled.value) {
        toolTipText = "default: false"
        actions()
            .onEach {
                vm.syncYomiEnabled.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("SyncYomi Host") {
        isEditable = false
    }.bind()
    jTextField(vm.syncYomiHost.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: \"\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.syncYomiHost.value = text?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX().wrap())

    jTextArea("SyncYomi API Key") {
        isEditable = false
    }.bind()
    jPasswordField(vm.syncYomiApiKey.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: \"\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.syncYomiApiKey.value = password?.concatToString()?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10 // todo why?
    }.bind(CC().grow().spanX())

    jCheckBox("Sync Manga", selected = vm.syncDataManga.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.syncDataManga.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Sync Chapters", selected = vm.syncDataChapters.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.syncDataChapters.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Sync Tracking", selected = vm.syncDataTracking.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.syncDataTracking.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Sync History", selected = vm.syncDataHistory.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.syncDataHistory.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Sync Categories", selected = vm.syncDataCategories.value) {
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.syncDataCategories.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())

    jTextArea("Sync Interval") {
        isEditable = false
    }.bind()
    jTextField(vm.syncInterval.value.toString()) {
        toolTipText = "default: 0s ; range: [-∞, +∞]"
        isEnabled = vm.syncYomiEnabled.value
        vm.syncYomiEnabled
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank() && runCatching { Duration.parse(it) }.isSuccess) {
                    vm.syncInterval.value = Duration.parse(it)
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())
}
