package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.swing.Swing
import java.awt.Component
import java.awt.Container
import java.awt.FlowLayout
import java.awt.GraphicsConfiguration
import java.awt.LayoutManager
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.text.Format
import javax.swing.AbstractButton
import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFormattedTextField
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JToggleButton
import javax.swing.SpinnerModel
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.text.Document

@DslMarker
annotation class SwingDsl

/** Define a [JFrame] */
@SwingDsl
inline fun jframe(
    title: String? = null,
    graphicsConfiguration: GraphicsConfiguration? = null,
    isVisible: Boolean = true,
    builder: JFrame.() -> Unit
): JFrame {
    return JFrame(title, graphicsConfiguration).apply {
        builder()

        this.isVisible = isVisible
    }
}

/** Define a [JPanel] */
@SwingDsl
inline fun jpanel(
    layoutManager: LayoutManager = FlowLayout(),
    isDoubleBuffered: Boolean = true,
    builder: JPanel.() -> Unit
): JPanel {
    return JPanel(layoutManager, isDoubleBuffered).apply {
        builder()
    }
}

/** Define a [JButton] */
@SwingDsl
inline fun jbutton(text: String? = null, icon: Icon? = null, builder: JButton.() -> Unit): JButton {
    return JButton(text, icon).apply {
        builder()
    }
}

/** Define a [JToggleButton] */
@SwingDsl
inline fun jToggleButton(text: String? = null, icon: Icon? = null, selected: Boolean = false, builder: JToggleButton.() -> Unit): JToggleButton {
    return JToggleButton(text, icon, selected).apply {
        builder()
    }
}

/** Define a [JCheckBox] */
@SwingDsl
inline fun jCheckBox(text: String? = null, icon: Icon? = null, selected: Boolean = false, builder: JCheckBox.() -> Unit): JCheckBox {
    return JCheckBox(text, icon, selected).apply {
        builder()
    }
}

/** Define a [JTextArea] */
@SwingDsl
inline fun jTextArea(text: String? = null, rows: Int = 0, columns: Int = 0, document: Document? = null, builder: JTextArea.() -> Unit): JTextArea {
    return JTextArea(document, text, rows, columns).apply {
        builder()
    }
}

/** Define a [JTextField] */
@SwingDsl
inline fun jTextField(text: String? = null, columns: Int = 0, document: Document? = null, builder: JTextField.() -> Unit): JTextField {
    return JTextField(document, text, columns).apply {
        builder()
    }
}

/** Define a [JFormattedTextField] */
@SwingDsl
inline fun jFormattedTextField(format: Format, value: Any? = null, builder: JFormattedTextField.() -> Unit): JFormattedTextField {
    return JFormattedTextField(format).apply {
        setValue(value)
        builder()
    }
}

/** Define a [JPasswordField] */
@SwingDsl
inline fun jPasswordField(text: String? = null, columns: Int = 0, document: Document? = null, builder: JPasswordField.() -> Unit): JPasswordField {
    return JPasswordField(document, text, columns).apply {
        builder()
    }
}

/** Define a [JComboBox] */
@SwingDsl
inline fun <E> jComboBox(
    items: Array<E>? = null,
    model: ComboBoxModel<E>? = null,
    builder: JComboBox<E>.() -> Unit
): JComboBox<E> {
    require(items != null || model != null) { "Both items and model were null" }
    return JComboBox(model ?: DefaultComboBoxModel(items)).apply {
        builder()
    }
}

/** Define a [JSpinner] */
@SwingDsl
inline fun jSpinner(model: SpinnerModel = SpinnerNumberModel(), builder: JSpinner.() -> Unit): JSpinner {
    return JSpinner(model).apply {
        builder()
    }
}

@SwingDsl
fun Component.addTo(container: Container, constraints: Any? = null) {
    container.add(this, constraints)
}

/** Adds [Component] to parent [Container] */
context(Container)
@SwingDsl
fun <T : Component> T.bind(constraints: Any? = null): T {
    add(this@bind, constraints)
    return this@bind
}

sealed class KeyListenerEvent {
    abstract val event: KeyEvent?
    data class Pressed(override val event: KeyEvent?) : KeyListenerEvent()
    data class Typed(override val event: KeyEvent?) : KeyListenerEvent()
    data class Released(override val event: KeyEvent?) : KeyListenerEvent()
}

/** Default [KeyListenerEvent] for [Component] */
@SwingDsl
fun Component.keyListener(): Flow<KeyListenerEvent> = callbackFlow {
    val keyListener = object : KeyListener {
        override fun keyPressed(e: KeyEvent?) {
            trySend(KeyListenerEvent.Pressed(e))
        }
        override fun keyTyped(e: KeyEvent?) {
            trySend(KeyListenerEvent.Typed(e))
        }
        override fun keyReleased(e: KeyEvent?) {
            trySend(KeyListenerEvent.Released(e))
        }
    }
    addKeyListener(keyListener)
    awaitClose { removeKeyListener(keyListener) }
}.flowOn(Dispatchers.Swing)

/** Default [ActionEvent] for [AbstractButton] */
@SwingDsl
fun AbstractButton.actions(): Flow<ActionEvent> = callbackFlow {
    val actionListener = ActionListener {
        trySend(it)
    }
    addActionListener(actionListener)
    awaitClose { removeActionListener(actionListener) }
}.flowOn(Dispatchers.Swing)

/** Default [ActionEvent] for [JTextField] */
@SwingDsl
fun JTextField.actions(): Flow<ActionEvent> = callbackFlow {
    val actionListener = ActionListener {
        trySend(it)
    }
    addActionListener(actionListener)
    awaitClose { removeActionListener(actionListener) }
}.flowOn(Dispatchers.Swing)

/** Default [ActionEvent] for [JComboBox] */
@SwingDsl
fun <E> JComboBox<E>.actions(): Flow<ActionEvent> = callbackFlow {
    val actionListener = ActionListener {
        trySend(it)
    }
    addActionListener(actionListener)
    awaitClose { removeActionListener(actionListener) }
}.flowOn(Dispatchers.Swing)

/** Default [ActionEvent] for [JSpinner] */
@SwingDsl
fun JSpinner.changes(): Flow<ChangeEvent> = callbackFlow {
    val actionListener = ChangeListener {
        trySend(it)
    }
    addChangeListener(actionListener)
    awaitClose { removeChangeListener(actionListener) }
}.flowOn(Dispatchers.Swing)
