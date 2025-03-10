/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.compose.ui.viewinterop

import android.graphics.Rect
import android.view.FocusFinder
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.FOCUS_DOWN
import android.view.ViewTreeObserver
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Exit
import androidx.compose.ui.focus.FocusEnterExitScope
import androidx.compose.ui.focus.FocusOwner
import androidx.compose.ui.focus.FocusProperties
import androidx.compose.ui.focus.FocusPropertiesModifierNode
import androidx.compose.ui.focus.FocusTargetNode
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.performRequestFocus
import androidx.compose.ui.focus.requestInteropFocus
import androidx.compose.ui.focus.toAndroidFocusDirection
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.Nodes
import androidx.compose.ui.node.requireLayoutNode
import androidx.compose.ui.node.requireOwner
import androidx.compose.ui.node.requireView
import androidx.compose.ui.node.visitLocalDescendants
import androidx.compose.ui.platform.InspectorInfo

internal fun Modifier.focusInteropModifier(): Modifier =
    this
        // Focus Group to intercept focus enter/exit.
        .then(FocusGroupPropertiesElement)
        .focusTarget()
        // Focus Target to make the embedded view focusable.
        .then(FocusTargetPropertiesElement)
        .focusTarget()

private class FocusTargetPropertiesNode : Modifier.Node(), FocusPropertiesModifierNode {
    override fun applyFocusProperties(focusProperties: FocusProperties) {
        focusProperties.canFocus = node.isAttached && getEmbeddedView().hasFocusable()
    }
}

private class FocusGroupPropertiesNode :
    Modifier.Node(), FocusPropertiesModifierNode, ViewTreeObserver.OnGlobalFocusChangeListener {
    var focusedChild: View? = null
    // ViewTreeObserver used during onAttach() -- used to make sure to remove the listener
    // from the global ViewTreeObserver even if the ComposeView has already been removed from
    // the hierarchy before onDetach() is called.
    var attachedViewTreeObserver: ViewTreeObserver? = null

    val onEnter: FocusEnterExitScope.() -> Unit = {
        // If this requestFocus is triggered by the embedded view getting focus,
        // then we don't perform this onEnter logic.
        val embeddedView = getEmbeddedView()
        if (!embeddedView.isFocused && !embeddedView.hasFocus()) {
            val focusOwner = requireOwner().focusOwner
            val hostView = requireView()

            val targetViewFocused =
                embeddedView.requestInteropFocus(
                    direction = requestedFocusDirection.toAndroidFocusDirection(),
                    rect = getCurrentlyFocusedRect(focusOwner, hostView, embeddedView)
                )
            if (!targetViewFocused) {
                cancelFocusChange()
            }
        }
    }

    val onExit: FocusEnterExitScope.() -> Unit = {
        val embeddedView = getEmbeddedView()
        @OptIn(ExperimentalComposeUiApi::class)
        if (ComposeUiFlags.isViewFocusFixEnabled) {
            if (embeddedView.hasFocus() || embeddedView.isFocused) {
                embeddedView.clearFocus()
            }
        } else if (embeddedView.hasFocus()) {
            val focusOwner = requireOwner().focusOwner
            val hostView = requireView()

            // If the embedded view is not a view group, then we can safely exit this focus group.
            if (embeddedView !is ViewGroup) {
                check(hostView.requestFocus()) { "host view did not take focus" }
            } else {
                val focusedRect = getCurrentlyFocusedRect(focusOwner, hostView, embeddedView)
                val androidFocusDirection =
                    requestedFocusDirection.toAndroidFocusDirection() ?: FOCUS_DOWN

                val nextView =
                    with(FocusFinder.getInstance()) {
                        if (focusedChild != null) {
                            findNextFocus(
                                hostView as ViewGroup,
                                focusedChild,
                                androidFocusDirection
                            )
                        } else {
                            findNextFocusFromRect(
                                hostView as ViewGroup,
                                focusedRect,
                                androidFocusDirection
                            )
                        }
                    }
                if (nextView != null && embeddedView.containsDescendant(nextView)) {
                    nextView.requestFocus(androidFocusDirection, focusedRect)
                    cancelFocusChange()
                } else {
                    check(hostView.requestFocus()) { "host view did not take focus" }
                }
            }
        }
    }

    override fun applyFocusProperties(focusProperties: FocusProperties) {
        focusProperties.canFocus = false
        focusProperties.onEnter = onEnter
        focusProperties.onExit = onExit
    }

    private fun getFocusTargetOfEmbeddedViewWrapper(): FocusTargetNode {
        var foundFocusTargetOfFocusGroup = false
        visitLocalDescendants(Nodes.FocusTarget) {
            if (foundFocusTargetOfFocusGroup) return it
            foundFocusTargetOfFocusGroup = true
        }
        error("Could not find focus target of embedded view wrapper")
    }

    override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
        if (requireLayoutNode().owner == null) return
        val embeddedView = getEmbeddedView()
        val focusOwner = requireOwner().focusOwner
        val hostView = requireOwner()
        val subViewLostFocus =
            oldFocus != null && oldFocus != hostView && embeddedView.containsDescendant(oldFocus)
        val subViewGotFocus =
            newFocus != null && newFocus != hostView && embeddedView.containsDescendant(newFocus)
        when {
            subViewLostFocus && subViewGotFocus -> {
                // Focus Moving within embedded view. Do nothing.
                focusedChild = newFocus
            }
            subViewGotFocus -> {
                // Focus moved to the embedded view.
                focusedChild = newFocus
                val focusTargetNode = getFocusTargetOfEmbeddedViewWrapper()
                if (!focusTargetNode.focusState.hasFocus)
                    focusOwner.focusTransactionManager.withNewTransaction {
                        focusTargetNode.performRequestFocus()
                    }
            }
            subViewLostFocus -> {
                focusedChild = null
                val focusTargetNode = getFocusTargetOfEmbeddedViewWrapper()
                if (focusTargetNode.focusState.isFocused) {
                    focusOwner.clearFocus(
                        force = false,
                        refreshFocusEvents = true,
                        clearOwnerFocus = false,
                        focusDirection = Exit
                    )
                }
            }
            else -> {
                // Focus Change not applicable to this node.
                focusedChild = null
            }
        }
    }

    override fun onAttach() {
        super.onAttach()
        val viewTreeObserver = requireView().viewTreeObserver
        attachedViewTreeObserver = viewTreeObserver
        viewTreeObserver.addOnGlobalFocusChangeListener(this)
    }

    override fun onDetach() {
        val viewTreeObserver = attachedViewTreeObserver
        if (viewTreeObserver != null && viewTreeObserver.isAlive) {
            viewTreeObserver.removeOnGlobalFocusChangeListener(this)
        }
        attachedViewTreeObserver = null
        requireView().viewTreeObserver.removeOnGlobalFocusChangeListener(this)
        focusedChild = null
        super.onDetach()
    }
}

private object FocusGroupPropertiesElement : ModifierNodeElement<FocusGroupPropertiesNode>() {
    override fun create(): FocusGroupPropertiesNode = FocusGroupPropertiesNode()

    override fun update(node: FocusGroupPropertiesNode) {}

    override fun InspectorInfo.inspectableProperties() {
        name = "FocusGroupProperties"
    }

    override fun hashCode() = "FocusGroupProperties".hashCode()

    override fun equals(other: Any?) = other === this
}

private object FocusTargetPropertiesElement : ModifierNodeElement<FocusTargetPropertiesNode>() {
    override fun create(): FocusTargetPropertiesNode = FocusTargetPropertiesNode()

    override fun update(node: FocusTargetPropertiesNode) {}

    override fun InspectorInfo.inspectableProperties() {
        name = "FocusTargetProperties"
    }

    override fun hashCode() = "FocusTargetProperties".hashCode()

    override fun equals(other: Any?) = other === this
}

private fun Modifier.Node.getEmbeddedView(): View {
    @OptIn(InternalComposeUiApi::class)
    return checkNotNull(node.requireLayoutNode().getInteropView()) {
        "Could not fetch interop view"
    }
}

private fun View.containsDescendant(other: View): Boolean {
    var viewParent = other.parent
    while (viewParent != null) {
        if (viewParent === this.parent) return true
        viewParent = viewParent.parent
    }
    return false
}

private fun getCurrentlyFocusedRect(
    focusOwner: FocusOwner,
    hostView: View,
    embeddedView: View
): Rect? {
    val hostViewOffset = IntArray(2).also { hostView.getLocationOnScreen(it) }
    val embeddedViewOffset = IntArray(2).also { embeddedView.getLocationOnScreen(it) }
    val focusedRect = focusOwner.getFocusRect() ?: return null
    return Rect(
        focusedRect.left.toInt() + hostViewOffset[0] - embeddedViewOffset[0],
        focusedRect.top.toInt() + hostViewOffset[1] - embeddedViewOffset[1],
        focusedRect.right.toInt() + hostViewOffset[0] - embeddedViewOffset[0],
        focusedRect.bottom.toInt() + hostViewOffset[1] - embeddedViewOffset[1]
    )
}
