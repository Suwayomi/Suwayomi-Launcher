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
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jpanel
import javax.swing.SpinnerNumberModel

@Suppress("ktlint:standard:function-naming")
fun Requests(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Max Parallel Source Requests") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.maxSourcesInParallel.value.coerceIn(6, 20), 6, 20, 1)) {
        toolTipText =
            "default: 6 ; range: [1, 20] ; How many different sources can do requests " +
            "(library update, downloads) in parallel. Library update/downloads are " +
            "grouped by source and all manga of a source are updated/downloaded synchronously"
        changes()
            .onEach {
                vm.maxSourcesInParallel.value = value as Int
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
}
