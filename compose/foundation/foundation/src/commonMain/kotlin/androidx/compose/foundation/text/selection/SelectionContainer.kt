/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.foundation.text.selection

import androidx.compose.foundation.internal.toClipEntry
import androidx.compose.foundation.text.ContextMenuArea
import androidx.compose.foundation.text.detectDownAndDragGesturesWithObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * Enables text selection for its direct or indirect children.
 *
 * Use of a lazy layout, such as [LazyRow][androidx.compose.foundation.lazy.LazyRow] or
 * [LazyColumn][androidx.compose.foundation.lazy.LazyColumn], within a [SelectionContainer] has
 * undefined behavior on text items that aren't composed. For example, texts that aren't composed
 * will not be included in copy operations and select all will not expand the selection to include
 * them.
 *
 * @sample androidx.compose.foundation.samples.SelectionSample
 */
@Composable
fun SelectionContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var selection by remember { mutableStateOf<Selection?>(null) }
    SelectionContainer(
        modifier = modifier,
        selection = selection,
        onSelectionChange = { selection = it },
        children = content
    )
}

/**
 * Disables text selection for its direct or indirect children. To use this, simply add this to wrap
 * one or more text composables.
 *
 * @sample androidx.compose.foundation.samples.DisableSelectionSample
 */
@Composable
fun DisableSelection(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSelectionRegistrar provides null, content = content)
}

/**
 * Selection Composable.
 *
 * The selection composable wraps composables and let them to be selectable. It paints the selection
 * area with start and end handles.
 */
@Suppress("ComposableLambdaParameterNaming")
@Composable
internal fun SelectionContainer(
    /** A [Modifier] for SelectionContainer. */
    modifier: Modifier = Modifier,
    /** Current Selection status. */
    selection: Selection?,
    /** A function containing customized behaviour when selection changes. */
    onSelectionChange: (Selection?) -> Unit,
    children: @Composable () -> Unit
) {
    val registrarImpl =
        rememberSaveable(saver = SelectionRegistrarImpl.Saver) { SelectionRegistrarImpl() }

    val manager = remember { SelectionManager(registrarImpl) }

    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    manager.hapticFeedBack = LocalHapticFeedback.current
    manager.onCopyHandler =
        remember(coroutineScope, clipboard) {
            { textToCopy ->
                coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    clipboard.setClipEntry(textToCopy.toClipEntry())
                }
            }
        }
    manager.textToolbar = LocalTextToolbar.current
    manager.onSelectionChange = onSelectionChange
    manager.selection = selection

    /*
     * Need a layout for selection gestures that span multiple text children.
     *
     * b/372053402: SimpleLayout must be the top layout in this composable because
     *     the modifier argument must be applied to the top layout in case it contains
     *     something like `Modifier.weight`.
     */
    SimpleLayout(modifier = modifier.then(manager.modifier)) {
        ContextMenuArea(manager) {
            CompositionLocalProvider(LocalSelectionRegistrar provides registrarImpl) {
                children()
                if (
                    manager.isInTouchMode &&
                        manager.hasFocus &&
                        !manager.isTriviallyCollapsedSelection()
                ) {
                    manager.selection?.let {
                        listOf(true, false).fastForEach { isStartHandle ->
                            val observer =
                                remember(isStartHandle) {
                                    manager.handleDragObserver(isStartHandle)
                                }

                            val positionProvider: () -> Offset =
                                remember(isStartHandle) {
                                    if (isStartHandle) {
                                        { manager.startHandlePosition ?: Offset.Unspecified }
                                    } else {
                                        { manager.endHandlePosition ?: Offset.Unspecified }
                                    }
                                }

                            val direction =
                                if (isStartHandle) {
                                    it.start.direction
                                } else {
                                    it.end.direction
                                }

                            val lineHeight =
                                if (isStartHandle) {
                                    manager.startHandleLineHeight
                                } else {
                                    manager.endHandleLineHeight
                                }
                            SelectionHandle(
                                offsetProvider = positionProvider,
                                isStartHandle = isStartHandle,
                                direction = direction,
                                handlesCrossed = it.handlesCrossed,
                                lineHeight = lineHeight,
                                modifier =
                                    Modifier.pointerInput(observer) {
                                        detectDownAndDragGesturesWithObserver(observer)
                                    },
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(manager) {
        onDispose {
            manager.onRelease()
            manager.hasFocus = false
        }
    }
}
