/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.material

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.testutils.assertAgainstGolden
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputMode.Companion.Keyboard
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.screenshot.AndroidXScreenshotTestRule
import org.junit.After
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class RadioButtonScreenshotTest {

    @get:Rule val rule = createComposeRule()

    @get:Rule val screenshotRule = AndroidXScreenshotTestRule(GOLDEN_MATERIAL)

    // TODO(b/267253920): Add a compose test API to set/reset InputMode.
    @After
    fun resetTouchMode() =
        with(InstrumentationRegistry.getInstrumentation()) {
            if (SDK_INT < 33) setInTouchMode(true) else resetInTouchMode()
        }

    private val wrap = Modifier.wrapContentSize(Alignment.TopStart)

    // TODO: this test tag as well as Boxes inside tests are temporarty, remove then b/157687898
    //  is fixed
    private val wrapperTestTag = "radioButtonWrapper"

    @Test
    fun radioButtonTest_selected() {
        rule.setMaterialContent {
            Box(wrap.testTag(wrapperTestTag)) { RadioButton(selected = true, onClick = {}) }
        }
        assertSelectableAgainstGolden("radioButton_selected")
    }

    @Test
    fun radioButtonTest_notSelected() {
        rule.setMaterialContent {
            Box(wrap.testTag(wrapperTestTag)) { RadioButton(selected = false, onClick = {}) }
        }
        assertSelectableAgainstGolden("radioButton_notSelected")
    }

    @Test
    @Ignore("b/355413615")
    fun radioButtonTest_pressed() {
        rule.setMaterialContent {
            Box(wrap.testTag(wrapperTestTag)) { RadioButton(selected = false, onClick = {}) }
        }
        rule.onNodeWithTag(wrapperTestTag).performTouchInput { down(center) }

        // Ripples are drawn on the RenderThread, not the main (UI) thread, so we can't wait for
        // synchronization. Instead just wait until after the ripples are finished animating.
        Thread.sleep(300)

        assertSelectableAgainstGolden("radioButton_pressed")
    }

    @Test
    fun radioButtonTest_hovered() {
        rule.setMaterialContent {
            Box(wrap.testTag(wrapperTestTag)) { RadioButton(selected = false, onClick = {}) }
        }
        rule.onNodeWithTag(wrapperTestTag).performMouseInput { enter(center) }

        assertSelectableAgainstGolden("radioButton_hovered")
    }

    @Test
    fun radioButtonTest_focused() {
        val focusRequester = FocusRequester()
        var localInputModeManager: InputModeManager? = null

        rule.setMaterialContent {
            localInputModeManager = LocalInputModeManager.current
            Box(wrap.testTag(wrapperTestTag)) {
                RadioButton(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.focusRequester(focusRequester)
                )
            }
        }

        rule.runOnIdle {
            localInputModeManager!!.requestInputMode(InputMode.Keyboard)
            focusRequester.requestFocus()
        }

        rule.waitForIdle()

        assertSelectableAgainstGolden("radioButton_focused")
    }

    @Test
    fun radioButtonTest_disabled_selected() {
        rule.setMaterialContent {
            Box(wrap.testTag(wrapperTestTag)) {
                RadioButton(selected = true, onClick = {}, enabled = false)
            }
        }
        assertSelectableAgainstGolden("radioButton_disabled_selected")
    }

    @Test
    fun radioButtonTest_disabled_notSelected() {
        rule.setMaterialContent {
            Box(wrap.testTag(wrapperTestTag)) {
                RadioButton(selected = false, onClick = {}, enabled = false)
            }
        }
        assertSelectableAgainstGolden("radioButton_disabled_notSelected")
    }

    @Test
    fun radioButton_notSelected_animateToSelected() {
        rule.setMaterialContent {
            val isSelected = remember { mutableStateOf(false) }
            Box(wrap.testTag(wrapperTestTag)) {
                RadioButton(
                    selected = isSelected.value,
                    onClick = { isSelected.value = !isSelected.value }
                )
            }
        }

        rule.mainClock.autoAdvance = false

        rule
            .onNode(isSelectable())
            // split click into (down) and (move, up) to enforce a composition in between
            .performTouchInput { down(center) }
            .performTouchInput {
                move()
                up()
            }

        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle() // Wait for measure
        rule.mainClock.advanceTimeBy(milliseconds = 80)

        // Ripples are drawn on the RenderThread, not the main (UI) thread, so we can't wait for
        // synchronization. Instead just wait until after the ripples are finished animating.
        Thread.sleep(300)

        assertSelectableAgainstGolden("radioButton_animateToSelected")
    }

    @Test
    fun radioButton_selected_animateToNotSelected() {
        rule.setMaterialContent {
            val isSelected = remember { mutableStateOf(true) }
            Box(wrap.testTag(wrapperTestTag)) {
                RadioButton(
                    selected = isSelected.value,
                    onClick = { isSelected.value = !isSelected.value }
                )
            }
        }

        rule.mainClock.autoAdvance = false

        rule
            .onNode(isSelectable())
            // split click into (down) and (move, up) to enforce a composition in between
            .performTouchInput { down(center) }
            .performTouchInput {
                move()
                up()
            }

        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle() // Wait for measure
        rule.mainClock.advanceTimeBy(milliseconds = 80)

        // Ripples are drawn on the RenderThread, not the main (UI) thread, so we can't wait for
        // synchronization. Instead just wait until after the ripples are finished animating.
        Thread.sleep(300)

        assertSelectableAgainstGolden("radioButton_animateToNotSelected")
    }

    private fun assertSelectableAgainstGolden(goldenName: String) {
        // TODO: replace with find(isInMutuallyExclusiveGroup()) after b/157687898 is fixed
        rule
            .onNodeWithTag(wrapperTestTag)
            .captureToImage()
            .assertAgainstGolden(screenshotRule, goldenName)
    }
}
