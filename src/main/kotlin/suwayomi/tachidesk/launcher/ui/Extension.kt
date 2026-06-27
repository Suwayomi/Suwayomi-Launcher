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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
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
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.selection
import javax.swing.JList
import javax.swing.JScrollPane

@Suppress("ktlint:standard:function-naming")
fun Extension(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Extension stores") {
        isEditable = false
    }.bind(CC().wrap())
    val extensionStores: JList<String> = JList(vm.extensionStores.value.toTypedArray())
    vm.extensionStores
        .drop(1)
        .onEach {
            extensionStores.setListData(it.toTypedArray())
        }.flowOn(Dispatchers.Main)
        .launchIn(scope)
    val textField = MutableStateFlow("")
    val jTextField =
        jTextField(textField.value) {
            toolTipText =
                "Add additional stores to Suwayomi, the format of a store is \"https://github.com/MY_ACCOUNT/MY_EXTENSION_STORE\""
            keyListener()
                .filterIsInstance<KeyListenerEvent.Released>()
                .onEach {
                    textField.value = text?.trim().orEmpty()
                }.flowOn(Dispatchers.Default)
                .launchIn(scope)
        }.bind(CC().grow().spanX())
    jbutton("Add") {
        actions()
            .onEach {
                val changed = vm.extensionStores.value + textField.value
                vm.extensionStores.value = changed.distinct()
                textField.value = ""
                jTextField.text = ""
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        textField
            .onEach {
                isEnabled = it.startsWith("http") && (it.endsWith("json") || it.endsWith("pb") || it.endsWith("pb.gz"))
            }.flowOn(Dispatchers.Main)
            .launchIn(scope)
    }.bind()
    jbutton("Remove") {
        actions()
            .onEach {
                val changed = vm.extensionStores.value - extensionStores.selectedValuesList.toSet()
                vm.extensionStores.value = changed.distinct()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        isEnabled = !extensionStores.isSelectionEmpty
        extensionStores
            .selection()
            .onEach {
                isEnabled = !extensionStores.isSelectionEmpty
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    JScrollPane(extensionStores).bind(CC().grow().spanX())
}
