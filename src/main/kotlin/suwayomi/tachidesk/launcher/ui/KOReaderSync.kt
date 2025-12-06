package suwayomi.tachidesk.launcher.ui

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
import suwayomi.tachidesk.launcher.jComboBox
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.settings.LauncherSettings.KoreaderSyncChecksumMethod
import suwayomi.tachidesk.launcher.settings.LauncherSettings.KoreaderSyncConflictStrategy
import java.text.DecimalFormat
import javax.swing.JLabel
import javax.swing.JSlider
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

private const val DECIMAL_FORMAT = "0.###############" // max 15 decimals

@Suppress("ktlint:standard:function-naming")
fun KoReaderSync(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Checksum Method") {
        isEditable = false
    }.bind()
    jComboBox(KoreaderSyncChecksumMethod.entries.toTypedArray()) {
        selectedItem = vm.koreaderSyncChecksumMethod.value
        toolTipText = "default: BINARY"
        actions()
            .onEach {
                vm.koreaderSyncChecksumMethod.value = (selectedItem as KoreaderSyncChecksumMethod)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jTextArea("Sync Strategy Forward") {
        isEditable = false
    }.bind()
    jComboBox(KoreaderSyncConflictStrategy.entries.toTypedArray()) {
        selectedItem = vm.koreaderSyncStrategyForward.value
        toolTipText = "default: PROMPT"
        actions()
            .onEach {
                vm.koreaderSyncStrategyForward.value = (selectedItem as KoreaderSyncConflictStrategy)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jTextArea("Sync Strategy Backward") {
        isEditable = false
    }.bind()
    jComboBox(KoreaderSyncConflictStrategy.entries.toTypedArray()) {
        selectedItem = vm.koreaderSyncStrategyBackward.value
        toolTipText = "default: DISABLED"
        actions()
            .onEach {
                vm.koreaderSyncStrategyBackward.value = (selectedItem as KoreaderSyncConflictStrategy)
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().grow().spanX().wrap())
    jTextArea("Tolerance") {
        isEditable = false
    }.bind(CC().grow().spanX().wrap())
    val label = JLabel()
    val tolerance =
        try {
            // N = -log10(tolerance)
            (-log10(vm.koreaderSyncPercentageTolerance.value))
                .roundToInt()
                .coerceIn(0, 15)
        } catch (_: NumberFormatException) {
            14
        }
    label.bind(CC().grow().spanX().wrap())
    run {
        val df = DecimalFormat(DECIMAL_FORMAT)
        label.text = "N = $tolerance → tolerance = ${df.format(10.0.pow(-tolerance))}"
    }
    JSlider(1, 15, tolerance)
        .apply {
            toolTipText = "1.0E-15 # default: 1.0E-15 ; range: [1.0E-15, 1.0] ; Absolute tolerance for progress comparison"
            setMajorTickSpacing(1)
            setPaintTicks(true)
            setPaintLabels(true)

            addChangeListener {
                val tolerance = 10.0.pow(-value)
                val df = DecimalFormat(DECIMAL_FORMAT)
                label.text = "N = $value → tolerance = ${df.format(tolerance)}"
                vm.koreaderSyncPercentageTolerance.value = tolerance
            }
        }.bind(CC().grow().spanX().wrap())
}
