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
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.settings.LauncherSettings
import suwayomi.tachidesk.launcher.settings.LauncherSettings.CbzMediaType
import suwayomi.tachidesk.launcher.settings.LauncherSettings.SortOrder
import javax.swing.SpinnerNumberModel

@Suppress("ktlint:standard:function-naming")
fun Opds(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jCheckBox("Use binary file sizes", selected = vm.opdsUseBinaryFileSizes.value) {
        toolTipText = "default: false ; Display file size in binary (KiB, MiB, GiB) instead of decimal (KB, MB, GB)"
        actions()
            .onEach {
                vm.opdsUseBinaryFileSizes.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jTextArea("Items per page") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.opdsItemsPerPage.value.coerceAtLeast(0), 0, 5000, 1)) {
        toolTipText = "default: 100 ; range: [10, 5000]"
        changes()
            .onEach {
                vm.opdsItemsPerPage.value = value as Int
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
    jCheckBox("Page read progress", selected = vm.opdsEnablePageReadProgress.value) {
        toolTipText = "default: true"
        actions()
            .onEach {
                vm.opdsEnablePageReadProgress.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Mark downloads as read", selected = vm.opdsMarkAsReadOnDownload.value) {
        toolTipText = "default: false"
        actions()
            .onEach {
                vm.opdsMarkAsReadOnDownload.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Show only unread", selected = vm.opdsShowOnlyUnreadChapters.value) {
        toolTipText = "default: false"
        actions()
            .onEach {
                vm.opdsShowOnlyUnreadChapters.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jCheckBox("Show only downloaded", selected = vm.opdsShowOnlyDownloadedChapters.value) {
        toolTipText = "default: false"
        actions()
            .onEach {
                vm.opdsShowOnlyDownloadedChapters.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().spanX())
    jTextArea("Chapter sort order") {
        isEditable = false
    }.bind()
    jComboBox(SortOrder.entries.toTypedArray()) {
        selectedItem =
            SortOrder.entries.find { it == vm.opdsChapterSortOrder.value }
        toolTipText = "default: DESC"
        actions()
            .onEach {
                vm.opdsChapterSortOrder.value = (selectedItem as SortOrder)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Cbz mime type") {
        isEditable = false
    }.bind()
    jComboBox(CbzMediaType.entries.toTypedArray()) {
        selectedItem =
            CbzMediaType.entries.find { it == vm.opdsCbzMimetype.value }
        toolTipText =
            "default: MODERN; Controls the MimeType that Suwayomi sends in OPDS entries for CBZ archives. Also affects global CBZ download. Modern follows recent IANA standard (2017), while LEGACY (deprecated mimetype for .cbz) and COMPATIBLE (deprecated mimetype for all comic archives) might be more compatible with older clients."
        actions()
            .onEach {
                vm.opdsCbzMimetype.value = (selectedItem as CbzMediaType)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
}
