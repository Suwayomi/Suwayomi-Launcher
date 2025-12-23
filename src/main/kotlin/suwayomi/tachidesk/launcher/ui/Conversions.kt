package suwayomi.tachidesk.launcher.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import suwayomi.tachidesk.launcher.KeyListenerEvent
import suwayomi.tachidesk.launcher.LauncherViewModel
import suwayomi.tachidesk.launcher.actions
import suwayomi.tachidesk.launcher.bind
import suwayomi.tachidesk.launcher.changes
import suwayomi.tachidesk.launcher.config.ServerConfig
import suwayomi.tachidesk.launcher.jSpinner
import suwayomi.tachidesk.launcher.jTabbedPane
import suwayomi.tachidesk.launcher.jTextArea
import suwayomi.tachidesk.launcher.jTextField
import suwayomi.tachidesk.launcher.jbutton
import suwayomi.tachidesk.launcher.jpanel
import suwayomi.tachidesk.launcher.keyListener
import suwayomi.tachidesk.launcher.selection
import java.net.URI
import javax.swing.JList
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.SpinnerNumberModel
import javax.swing.event.ListSelectionEvent
import kotlin.time.Duration

@Suppress("ktlint:standard:function-naming")
fun Conversions(
    vm: LauncherViewModel,
    scope: CoroutineScope,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    jTextArea("Conversions:") {
        isEditable = false
    }.bind(CC().wrap())

    jTabbedPane {
        addTab("Download", conversionsLayout(scope, vm.downloadConversions))
        addTab("Serve", conversionsLayout(scope, vm.serveConversions))
    }.bind(CC().grow().spanX().wrap())
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun conversionsLayout(
    scope: CoroutineScope,
    conversions: MutableStateFlow<Map<String, ServerConfig.DownloadConversion>>,
) = jpanel(
    MigLayout(
        LC().alignX("center").alignY("center"),
    ),
) {
    val downloadConversions: JList<String> =
        JList(
            conversions.value.keys
                .toTypedArray(),
        )
    downloadConversions.selectionMode = ListSelectionModel.SINGLE_SELECTION
    conversions
        .map { it.keys }
        .distinctUntilChanged()
        .drop(1)
        .onEach {
            downloadConversions.setListData(it.toTypedArray())
        }.flowOn(Dispatchers.Main)
        .launchIn(scope)
    val textField = MutableStateFlow("")
    val jTextField =
        jTextField(textField.value) {
            toolTipText =
                "Add image conversions to Suwayomi using a image MimeType, or use 'default' for other types of images"
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
                val changed =
                    conversions.value + (
                        textField.value to
                            ServerConfig.DownloadConversion(
                                "none",
                            )
                    )
                conversions.value = changed
                textField.value = ""
                jTextField.text = ""
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        textField
            .onEach {
                isEnabled = it == "default" ||
                    (it.startsWith("image/") && it.substringAfter("image/").isNotBlank())
            }.flowOn(Dispatchers.Main)
            .launchIn(scope)
    }.bind()
    jbutton("Remove") {
        actions()
            .onEach {
                val changed = conversions.value - downloadConversions.selectedValuesList.toSet()
                conversions.value = changed
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
        isEnabled = !downloadConversions.isSelectionEmpty
        downloadConversions
            .selection()
            .onEach {
                isEnabled = !downloadConversions.isSelectionEmpty
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }.bind(CC().wrap())
    jpanel {
        // downloadConversions.bind()
        JScrollPane(downloadConversions).bind()

        jpanel(
            MigLayout(
                LC().alignX("center").alignY("center"),
            ),
        ) {
            jTextArea("Target") {
                isEditable = false
            }.bind()
            val targetMime =
                jTextField("") {
                    toolTipText = "The target's URL or the format's MimeType"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .map {
                            text?.trim()
                        }.onEach { target ->
                            if (!target.isNullOrBlank() && (
                                    (
                                        target.startsWith("image/") &&
                                            target.substringAfter("image/").isNotBlank()
                                    ) || runCatching { URI(target).toURL() }.isSuccess
                                )
                            ) {
                                val mime = downloadConversions.selectedValue
                                if (mime != null) {
                                    conversions.update {
                                        val currentValue = conversions.value[mime]
                                        if (currentValue != null) {
                                            it.plus(
                                                mime to
                                                    currentValue.copy(
                                                        target = target,
                                                    ),
                                            )
                                        } else {
                                            it
                                        }
                                    }
                                }
                            }
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind(CC().grow().spanX().wrap())
            jTextArea("Compression") {
                isEditable = false
            }.bind()
            val spinner =
                jSpinner(
                    SpinnerNumberModel(
                        0.0f,
                        0f,
                        1f,
                        0.01f,
                    ),
                ) {
                    toolTipText =
                        "0 to use default - How much the image should be compressed"
                    changes()
                        .onEach {
                            val mime = downloadConversions.selectedValue
                            if (mime != null) {
                                conversions.update { conversions ->
                                    val currentValue = conversions[mime]
                                    if (currentValue != null) {
                                        conversions.plus(
                                            mime to
                                                currentValue.copy(
                                                    compressionLevel = (value as Float).toString().toDouble().takeIf { it > 0.01 },
                                                ),
                                        )
                                    } else {
                                        conversions
                                    }
                                }
                            }
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                }.bind(CC().grow().spanX().wrap())

            jTextArea("Call timeout") {
                isEditable = false
            }.bind()
            val callTimeout =
                jTextField {
                    toolTipText = "default: 2m ; range: [0s, +∞]"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .map {
                            text?.trim()
                        }.onEach {
                            val mime = downloadConversions.selectedValue
                            if (mime != null && (it.isNullOrBlank() || runCatching { Duration.parse(it) }.isSuccess)) {
                                conversions.update { conversions ->
                                    val currentValue = conversions[mime]
                                    if (currentValue != null) {
                                        conversions.plus(
                                            mime to
                                                currentValue.copy(
                                                    callTimeout =
                                                        it
                                                            ?.takeUnless { it.isBlank() }
                                                            ?.let { Duration.parse(it) },
                                                ),
                                        )
                                    } else {
                                        conversions
                                    }
                                }
                            }
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind(CC().grow().spanX().wrap())
            jTextArea("Connect timeout") {
                isEditable = false
            }.bind()
            val connectTimeout =
                jTextField {
                    toolTipText = "default: 30s ; range: [0s, +∞]"
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .map {
                            text?.trim()
                        }.onEach {
                            val mime = downloadConversions.selectedValue
                            if (mime != null && (it.isNullOrBlank() || runCatching { Duration.parse(it) }.isSuccess)) {
                                conversions.update { conversions ->
                                    val currentValue = conversions[mime]
                                    if (currentValue != null) {
                                        conversions.plus(
                                            mime to
                                                currentValue.copy(
                                                    connectTimeout =
                                                        it
                                                            ?.takeUnless { it.isBlank() }
                                                            ?.let { Duration.parse(it) },
                                                ),
                                        )
                                    } else {
                                        conversions
                                    }
                                }
                            }
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind(CC().grow().spanX().wrap())

            // Headers section
            jTextArea("Headers") {
                isEditable = false
            }.bind(CC().wrap())
            val headersList: JList<String> =
                JList(
                    arrayOf<String>(),
                )
            headersList.selectionMode = ListSelectionModel.SINGLE_SELECTION
            val headersState = MutableStateFlow<List<Pair<String, String>>>(emptyList())
            headersState
                .map { it.map { h -> "${h.first}: ${h.second}" } }
                .distinctUntilChanged()
                .onEach {
                    headersList.setListData(it.toTypedArray())
                }.flowOn(Dispatchers.Main)
                .launchIn(scope)

            val headerNameField = MutableStateFlow("")
            val headerValueField = MutableStateFlow("")
            val headerNameTextField =
                jTextField(headerNameField.value) {
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .onEach {
                            headerNameField.value = text?.trim().orEmpty()
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind(CC().grow().spanX().split(2))
            val headerValueTextField =
                jTextField(headerValueField.value) {
                    keyListener()
                        .filterIsInstance<KeyListenerEvent.Released>()
                        .onEach {
                            headerValueField.value = text?.trim().orEmpty()
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    columns = 10
                }.bind(CC().grow().spanX().split(2))

            val addHeaderButton =
                jbutton("Add Header") {
                    actions()
                        .onEach {
                            val mime = downloadConversions.selectedValue
                            if (mime != null) {
                                val currentConversion = conversions.value[mime]
                                if (currentConversion != null) {
                                    val headers = currentConversion.headers.orEmpty().toMutableMap()
                                    headers[headerNameField.value] = headerValueField.value
                                    conversions.update {
                                        it.plus(
                                            mime to
                                                currentConversion.copy(
                                                    headers = headers,
                                                ),
                                        )
                                    }
                                    headerNameField.value = ""
                                    headerValueField.value = ""
                                    headerNameTextField.text = ""
                                    headerValueTextField.text = ""
                                }
                            }
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                }.bind(CC())

            val removeHeaderButton =
                jbutton("Remove Header") {
                    actions()
                        .onEach {
                            val mime = downloadConversions.selectedValue
                            if (mime != null) {
                                val currentConversion = conversions.value[mime]
                                if (currentConversion != null && currentConversion.headers != null) {
                                    val headers = currentConversion.headers.toMutableMap()
                                    val selectedIndex = headersList.selectedIndex
                                    if (selectedIndex >= 0) {
                                        headers.remove(headersState.value[selectedIndex].first)
                                        conversions.update {
                                            it.plus(
                                                mime to
                                                    currentConversion.copy(
                                                        headers = headers.ifEmpty { null },
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }
                        }.flowOn(Dispatchers.Default)
                        .launchIn(scope)
                    isEnabled = !headersList.isSelectionEmpty
                }.bind(CC().wrap())

            JScrollPane(headersList).bind(CC().grow().spanX().wrap())

            downloadConversions
                .selection()
                .onStart {
                    emit(ListSelectionEvent(Any(), 0, 0, false))
                }.mapLatest {
                    coroutineScope {
                        launch {
                            val value = downloadConversions.selectedValue
                            if (value != null) {
                                conversions.collectLatest {
                                    val conversion = it[value]
                                    if (conversion != null) {
                                        targetMime.text = conversion.target
                                        spinner.value = conversion.compressionLevel?.toString()?.toFloat() ?: 0.0f
                                        callTimeout.text = conversion.callTimeout?.toString() ?: ""
                                        connectTimeout.text = conversion.connectTimeout?.toString() ?: ""
                                        headersState.value = conversion.headers?.toList().orEmpty()
                                    } else {
                                        targetMime.text = ""
                                        spinner.value = 0.0f
                                        callTimeout.text = ""
                                        connectTimeout.text = ""
                                        headersState.value = emptyList()
                                    }
                                }
                            } else {
                                targetMime.text = ""
                                spinner.value = 0.0f
                                callTimeout.text = ""
                                connectTimeout.text = ""
                                headersState.value = emptyList()
                            }

                            spinner.isVisible = true
                        }
                        targetMime
                            .keyListener()
                            .filterIsInstance<KeyListenerEvent.Released>()
                            .onStart { emit(KeyListenerEvent.Released(null)) }
                            .map {
                                targetMime.text?.trim()
                            }.mapLatest { target ->
                                targetMime.isEnabled = downloadConversions.selectedValue != null
                                val isMime =
                                    target != null && target.startsWith("image/") &&
                                        target.substringAfter("image/").isNotBlank()
                                val isUrl = target != null && runCatching { URI(target).toURL() }.isSuccess
                                spinner.isEnabled = isMime
                                callTimeout.isEnabled = isUrl
                                connectTimeout.isEnabled = isUrl
                                headerNameTextField.isEnabled = isUrl
                                headerValueTextField.isEnabled = isUrl
                                coroutineScope {
                                    combine(
                                        headerNameField,
                                        headerValueField,
                                    ) {
                                        it.all { it.isNotBlank() }
                                    }.distinctUntilChanged()
                                        .onEach {
                                            addHeaderButton.isEnabled = it && isUrl
                                        }.launchIn(this)
                                    headersList
                                        .selection()
                                        .onEach {
                                            removeHeaderButton.isEnabled = !headersList.isSelectionEmpty && isUrl
                                        }.flowOn(Dispatchers.Default)
                                        .launchIn(this)
                                }
                            }.launchIn(this)
                    }
                }.launchIn(scope)
        }.bind()
    }.bind(CC().grow().spanX())
}
