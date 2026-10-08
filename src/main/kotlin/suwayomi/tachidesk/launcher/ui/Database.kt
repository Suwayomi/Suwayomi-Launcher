package suwayomi.tachidesk.launcher.ui

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
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jPasswordField
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.settings.LauncherSettings.DatabaseType
import javax.swing.SpinnerNumberModel

@Suppress("ktlint:standard:function-naming")
fun Database(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Database Type") {
        isEditable = false
    }.bind()
    jComboBox(DatabaseType.entries.toTypedArray()) {
        selectedItem = vm.databaseType.value
        toolTipText = "default: BINARY"
        actions()
            .onEach {
                vm.databaseType.value = (selectedItem as DatabaseType)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Database URL") {
        isEditable = false
    }.bind()
    jTextField(vm.databaseUrl.value) {
        isEnabled = vm.databaseType.value != DatabaseType.H2
        vm.databaseType
            .onEach {
                isEnabled = it != DatabaseType.H2
            }.launchIn(scope)
        toolTipText = "default: \"postgresql://localhost:5432/suwayomi\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank()) {
                    vm.databaseUrl.value = it
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15
    }.bind(CC().grow().spanX().wrap())

    jTextArea("Username") {
        isEditable = false
    }.bind()
    jTextField(vm.databaseUsername.value) {
        isEnabled = vm.databaseType.value != DatabaseType.H2
        vm.databaseType
            .onEach {
                isEnabled = it != DatabaseType.H2
            }.launchIn(scope)
        toolTipText = "default: \"\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.databaseUsername.value = text?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15 // todo why?
    }.bind(CC().grow().spanX().wrap())
    jTextArea("Password") {
        isEditable = false
    }.bind()
    jPasswordField(vm.databasePassword.value) {
        isEnabled = vm.databaseType.value != DatabaseType.H2
        vm.databaseType
            .onEach {
                isEnabled = it != DatabaseType.H2
            }.launchIn(scope)
        toolTipText = "default: \"\""
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .onEach {
                vm.databasePassword.value = password?.concatToString()?.trim().orEmpty()
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 15 // todo why?
    }.bind(CC().grow().spanX())

    jTextArea("Database Schema") {
        isEditable = false
    }.bind()
    jTextField(vm.databaseSchema.value) {
        toolTipText = "default: \"suwayomi\" ; The name of the database schema that is going to be used"
        isEnabled = vm.databaseType.value != DatabaseType.H2
        vm.databaseType
            .onEach {
                isEnabled = it != DatabaseType.H2
            }.launchIn(scope)
        keyListener()
            .filterIsInstance<KeyListenerEvent.Released>()
            .map {
                text?.trim()
            }.onEach {
                if (!it.isNullOrBlank()) {
                    vm.databaseSchema.value = it
                }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        columns = 10
    }.bind(CC().grow().spanX().wrap())

    jCheckBox("Use Hikari Connection Pool", selected = vm.useHikariConnectionPool.value) {
        toolTipText = "default: true ; Use Hikari Connection Pool to connect to the database."
        actions()
            .onEach {
                vm.useHikariConnectionPool.value = isSelected
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())

    jTextArea("Hikari Max Pool Size") {
        isEditable = false
    }.bind()
    jSpinner(SpinnerNumberModel(vm.hikariMaxPoolSize.value.coerceIn(0, 9999), 0, 9999, 1)) {
        toolTipText =
            "default: 6 ; range: [1, +∞] ; The maximum number of actual connections to the database ; See https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing"
        isEnabled = vm.useHikariConnectionPool.value
        vm.useHikariConnectionPool
            .onEach {
                isEnabled = it
            }.launchIn(scope)
        changes()
            .onEach {
                vm.hikariMaxPoolSize.value = value as Int
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX())
}
