package suwayomi.tachidesk.launcher

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

inline fun jframe(title: String, isVisible: Boolean = true, builder: JFrame.() -> Unit): JFrame {
    return JFrame(title).apply {
        builder()

        this.isVisible = isVisible
    }
}

inline fun jpanel(builder: JPanel.() -> Unit): JPanel {
    return JPanel().apply {
        builder()
    }
}
inline fun jbutton(title: String, builder: JButton.() -> Unit): JButton {
    return JButton(title).apply {
        builder()
    }
}