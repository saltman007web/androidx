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

package androidx.wear.tiles.material;

import static com.google.common.truth.Truth.assertThat;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(AndroidJUnit4.class)
@DoNotInstrument
@SuppressWarnings("deprecation")
public class ButtonTest {
    private static final String RESOURCE_ID = "icon";
    private static final String TEXT = "ABC";
    private static final String CONTENT_DESCRIPTION = "clickable button";
    private static final androidx.wear.tiles.ModifiersBuilders.Clickable CLICKABLE =
            new androidx.wear.tiles.ModifiersBuilders.Clickable.Builder()
                    .setOnClick(
                            new androidx.wear.tiles.ActionBuilders.LaunchAction.Builder().build())
                    .setId("action_id")
                    .build();
    private static final Context CONTEXT = ApplicationProvider.getApplicationContext();
    private static final androidx.wear.tiles.LayoutElementBuilders.LayoutElement CONTENT =
            new Text.Builder(CONTEXT, "ABC")
                    .setColor(androidx.wear.tiles.ColorBuilders.argb(0))
                    .build();

    @Test
    public void testButtonCustomAddedContentNoContentDesc() {
        Button button = new Button.Builder(CONTEXT, CLICKABLE).setCustomContent(CONTENT).build();

        assertButton(
                button,
                ButtonDefaults.DEFAULT_SIZE,
                new ButtonColors(Colors.PRIMARY, 0),
                null,
                Button.METADATA_TAG_CUSTOM_CONTENT,
                null,
                null,
                null,
                CONTENT);
    }

    @Test
    public void testButtonCustom() {
        androidx.wear.tiles.DimensionBuilders.DpProp mSize = ButtonDefaults.LARGE_SIZE;
        ButtonColors mButtonColors = new ButtonColors(0x11223344, 0);

        Button button =
                new Button.Builder(CONTEXT, CLICKABLE)
                        .setCustomContent(CONTENT)
                        .setSize(mSize)
                        .setButtonColors(mButtonColors)
                        .setContentDescription(CONTENT_DESCRIPTION)
                        .build();

        assertButton(
                button,
                mSize,
                mButtonColors,
                CONTENT_DESCRIPTION,
                Button.METADATA_TAG_CUSTOM_CONTENT,
                null,
                null,
                null,
                CONTENT);
    }

    @Test
    public void testButtonSetIcon() {

        Button button =
                new Button.Builder(CONTEXT, CLICKABLE)
                        .setIconContent(RESOURCE_ID)
                        .setContentDescription(CONTENT_DESCRIPTION)
                        .build();

        assertButton(
                button,
                ButtonDefaults.DEFAULT_SIZE,
                ButtonDefaults.PRIMARY_COLORS,
                CONTENT_DESCRIPTION,
                Button.METADATA_TAG_ICON,
                null,
                RESOURCE_ID,
                null,
                null);
    }

    @Test
    public void testButtonSetIconSetSize() {
        Button button =
                new Button.Builder(CONTEXT, CLICKABLE)
                        .setIconContent(RESOURCE_ID)
                        .setSize(ButtonDefaults.LARGE_SIZE)
                        .setContentDescription(CONTENT_DESCRIPTION)
                        .build();

        assertButton(
                button,
                ButtonDefaults.LARGE_SIZE,
                ButtonDefaults.PRIMARY_COLORS,
                CONTENT_DESCRIPTION,
                Button.METADATA_TAG_ICON,
                null,
                RESOURCE_ID,
                null,
                null);
    }

    @Test
    public void testButtonSetIconCustomSize() {
        androidx.wear.tiles.DimensionBuilders.DpProp mSize =
                androidx.wear.tiles.DimensionBuilders.dp(36);

        Button button =
                new Button.Builder(CONTEXT, CLICKABLE)
                        .setIconContent(RESOURCE_ID, mSize)
                        .setContentDescription(CONTENT_DESCRIPTION)
                        .build();

        assertButton(
                button,
                ButtonDefaults.DEFAULT_SIZE,
                ButtonDefaults.PRIMARY_COLORS,
                CONTENT_DESCRIPTION,
                Button.METADATA_TAG_ICON,
                null,
                RESOURCE_ID,
                null,
                null);
    }

    @Test
    public void testButtonSetText() {
        Button button =
                new Button.Builder(CONTEXT, CLICKABLE)
                        .setTextContent(TEXT)
                        .setContentDescription(CONTENT_DESCRIPTION)
                        .build();

        assertButton(
                button,
                ButtonDefaults.DEFAULT_SIZE,
                ButtonDefaults.PRIMARY_COLORS,
                CONTENT_DESCRIPTION,
                Button.METADATA_TAG_TEXT,
                TEXT,
                null,
                null,
                null);
    }

    @Test
    public void testButtonSetTextSetSize() {
        Button button =
                new Button.Builder(CONTEXT, CLICKABLE)
                        .setTextContent(TEXT)
                        .setContentDescription(CONTENT_DESCRIPTION)
                        .setSize(ButtonDefaults.EXTRA_LARGE_SIZE)
                        .build();

        assertButton(
                button,
                ButtonDefaults.EXTRA_LARGE_SIZE,
                ButtonDefaults.PRIMARY_COLORS,
                CONTENT_DESCRIPTION,
                Button.METADATA_TAG_TEXT,
                TEXT,
                null,
                null,
                null);
    }

    @Test
    public void testWrongElementForButton() {
        androidx.wear.tiles.LayoutElementBuilders.Column box =
                new androidx.wear.tiles.LayoutElementBuilders.Column.Builder().build();

        assertThat(Button.fromLayoutElement(box)).isNull();
    }

    @Test
    public void testWrongBoxForButton() {
        androidx.wear.tiles.LayoutElementBuilders.Box box =
                new androidx.wear.tiles.LayoutElementBuilders.Box.Builder().build();

        assertThat(Button.fromLayoutElement(box)).isNull();
    }

    @Test
    public void testWrongTagForButton() {
        androidx.wear.tiles.LayoutElementBuilders.Box box =
                new androidx.wear.tiles.LayoutElementBuilders.Box.Builder()
                        .setModifiers(
                                new androidx.wear.tiles.ModifiersBuilders.Modifiers.Builder()
                                        .setMetadata(
                                                new androidx.wear.tiles.ModifiersBuilders
                                                                .ElementMetadata.Builder()
                                                        .setTagData("test".getBytes(UTF_8))
                                                        .build())
                                        .build())
                        .build();

        assertThat(Button.fromLayoutElement(box)).isNull();
    }

    private void assertButton(
            @NonNull Button actualButton,
            androidx.wear.tiles.DimensionBuilders.@NonNull DpProp expectedSize,
            @NonNull ButtonColors expectedButtonColors,
            @Nullable String expectedContentDescription,
            @NonNull String expectedMetadataTag,
            @Nullable String expectedTextContent,
            @Nullable String expectedIconContent,
            @Nullable String expectedImageContent,
            androidx.wear.tiles.LayoutElementBuilders.@Nullable LayoutElement
                    expectedCustomContent) {
        assertButtonIsEqual(
                actualButton,
                expectedSize,
                expectedButtonColors,
                expectedContentDescription,
                expectedMetadataTag,
                expectedTextContent,
                expectedIconContent,
                expectedImageContent,
                expectedCustomContent);

        assertFromLayoutElementButtonIsEqual(
                actualButton,
                expectedSize,
                expectedButtonColors,
                expectedContentDescription,
                expectedMetadataTag,
                expectedTextContent,
                expectedIconContent,
                expectedImageContent,
                expectedCustomContent);

        assertThat(Button.fromLayoutElement(actualButton)).isEqualTo(actualButton);
    }

    private void assertButtonIsEqual(
            @NonNull Button actualButton,
            androidx.wear.tiles.DimensionBuilders.@NonNull DpProp expectedSize,
            @NonNull ButtonColors expectedButtonColors,
            @Nullable String expectedContentDescription,
            @NonNull String expectedMetadataTag,
            @Nullable String expectedTextContent,
            @Nullable String expectedIconContent,
            @Nullable String expectedImageContent,
            androidx.wear.tiles.LayoutElementBuilders.@Nullable LayoutElement
                    expectedCustomContent) {
        // Mandatory
        assertThat(actualButton.getMetadataTag()).isEqualTo(expectedMetadataTag);
        assertThat(actualButton.getClickable().toProto()).isEqualTo(CLICKABLE.toProto());
        assertThat(actualButton.getSize().toContainerDimensionProto())
                .isEqualTo(expectedSize.toContainerDimensionProto());
        assertThat(actualButton.getButtonColors().getBackgroundColor().getArgb())
                .isEqualTo(expectedButtonColors.getBackgroundColor().getArgb());
        assertThat(actualButton.getButtonColors().getContentColor().getArgb())
                .isEqualTo(expectedButtonColors.getContentColor().getArgb());

        // Nullable
        if (expectedContentDescription == null) {
            assertThat(actualButton.getContentDescription()).isNull();
        } else {
            assertThat(actualButton.getContentDescription().toString())
                    .isEqualTo(expectedContentDescription);
        }

        if (expectedTextContent == null) {
            assertThat(actualButton.getTextContent()).isNull();
        } else {
            assertThat(actualButton.getTextContent()).isEqualTo(expectedTextContent);
        }

        if (expectedIconContent == null) {
            assertThat(actualButton.getIconContent()).isNull();
        } else {
            assertThat(actualButton.getIconContent()).isEqualTo(expectedIconContent);
        }

        if (expectedImageContent == null) {
            assertThat(actualButton.getImageContent()).isNull();
        } else {
            assertThat(actualButton.getImageContent()).isEqualTo(expectedImageContent);
        }

        if (expectedCustomContent == null) {
            assertThat(actualButton.getCustomContent()).isNull();
        } else {
            assertThat(actualButton.getCustomContent().toLayoutElementProto())
                    .isEqualTo(expectedCustomContent.toLayoutElementProto());
        }
    }

    private void assertFromLayoutElementButtonIsEqual(
            @NonNull Button button,
            androidx.wear.tiles.DimensionBuilders.@NonNull DpProp expectedSize,
            @NonNull ButtonColors expectedButtonColors,
            @Nullable String expectedContentDescription,
            @NonNull String expectedMetadataTag,
            @Nullable String expectedTextContent,
            @Nullable String expectedIconContent,
            @Nullable String expectedImageContent,
            androidx.wear.tiles.LayoutElementBuilders.@Nullable LayoutElement
                    expectedCustomContent) {
        androidx.wear.tiles.LayoutElementBuilders.Box box =
                new androidx.wear.tiles.LayoutElementBuilders.Box.Builder()
                        .addContent(button)
                        .build();

        Button newButton = Button.fromLayoutElement(box.getContents().get(0));

        assertThat(newButton).isNotNull();
        assertButtonIsEqual(
                newButton,
                expectedSize,
                expectedButtonColors,
                expectedContentDescription,
                expectedMetadataTag,
                expectedTextContent,
                expectedIconContent,
                expectedImageContent,
                expectedCustomContent);
    }
}
