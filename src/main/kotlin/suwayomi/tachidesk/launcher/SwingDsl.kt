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
import javax.swing.AbstractButton
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

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

@SwingDsl
fun Component.addTo(container: Container, constraints: Any? = null) {
    container.add(this, constraints)
}

/** Adds [Component] to parent [Container] */
context(Container)
@SwingDsl
fun Component.bind(constraints: Any? = null) {
    add(this@bind, constraints)
}

/** Default [ActionEvent] for [AbstractButton] */
@SwingDsl
fun AbstractButton.actions(): Flow<ActionEvent> = callbackFlow {
    val actionListener = ActionListener {
        trySend(it)
    }
    addActionListener(actionListener)
    awaitClose { removeActionListener(actionListener) }
}.flowOn(Dispatchers.Swing)
