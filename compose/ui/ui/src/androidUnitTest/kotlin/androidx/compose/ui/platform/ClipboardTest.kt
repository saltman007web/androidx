/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.compose.ui.platform

import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClipboardTest {

    @Test
    fun clipboardManagerWithoutHasText_returnsTrue_withNonEmptyGetText() {
        val clipboardManager = FakeClipboardManager("Something")
        assertThat(clipboardManager.hasText()).isTrue()
    }

    @Test
    fun clipboardManagerWithoutHasText_returnsFalse_withNullGetText() {
        val clipboardManager = FakeClipboardManager()
        assertThat(clipboardManager.hasText()).isFalse()
    }

    @Test
    fun clipboardManagerWithoutHasText_returnsFalse_withEmptyGetText() {
        val clipboardManager = FakeClipboardManager("")
        assertThat(clipboardManager.hasText()).isFalse()
    }
}

@Suppress("DEPRECATION")
private fun FakeClipboardManager(initialText: String? = null) =
    object : ClipboardManager {
        private var currentText: AnnotatedString? = initialText?.let { AnnotatedString(it) }

        override fun setText(annotatedString: AnnotatedString) {
            currentText = annotatedString
        }

        override fun getText(): AnnotatedString? = currentText

        override fun getClip(): ClipEntry? = null

        override fun setClip(clipEntry: ClipEntry?) = Unit
    }
