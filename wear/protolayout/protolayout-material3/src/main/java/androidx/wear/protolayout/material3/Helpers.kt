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

package androidx.wear.protolayout.material3

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.DP
import androidx.annotation.Dimension.Companion.SP
import androidx.annotation.FloatRange
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.DpProp
import androidx.wear.protolayout.DimensionBuilders.WrappedDimensionProp
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_END
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_RIGHT
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_START
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_UNDEFINED
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.TextAlignment
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Corner
import androidx.wear.protolayout.ModifiersBuilders.ElementMetadata
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ModifiersBuilders.Padding
import androidx.wear.protolayout.materialcore.fontscaling.FontScaleConverterFactory
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.argb
import java.nio.charset.StandardCharsets

/**
 * The breakpoint value defining the screen width on and after which, some properties should be
 * changed, depending on the use case.
 */
internal const val SCREEN_SIZE_BREAKPOINT_DP = 225

/** Minimum tap target for any clickable element. */
internal val MINIMUM_TAP_TARGET_SIZE: DpProp = dp(48f)

/** Returns byte array representation of tag from String. */
internal fun String.toTagBytes(): ByteArray = toByteArray(StandardCharsets.UTF_8)

/** Returns String representation of tag from Metadata. */
internal fun ElementMetadata.toTagName(): String = String(tagData, StandardCharsets.UTF_8)

internal fun <T> Iterable<T>.addBetween(newItem: T): Sequence<T> = sequence {
    var isFirst = true
    for (element in this@addBetween) {
        if (!isFirst) {
            yield(newItem)
        } else {
            isFirst = false
        }
        yield(element)
    }
}

@Dimension(unit = SP)
internal fun Float.dpToSp(fontScale: Float): Float =
    (if (SDK_INT >= UPSIDE_DOWN_CAKE) FontScaleConverterFactory.forScale(fontScale) else null)
        ?.convertDpToSp(this) ?: dpToSpLinear(fontScale)

@Dimension(unit = SP) private fun Float.dpToSpLinear(fontScale: Float): Float = this / fontScale

internal fun Int.toDp() = dp(this.toFloat())

internal fun String.toElementMetadata() = ElementMetadata.Builder().setTagData(toTagBytes()).build()

/** Builds a horizontal Spacer, with width set to expand and height set to the given value. */
internal fun horizontalSpacer(@Dimension(unit = DP) heightDp: Int): Spacer =
    Spacer.Builder().setWidth(expand()).setHeight(heightDp.toDp()).build()

/** Builds a vertical Spacer, with height set to expand and width set to the given value. */
internal fun verticalSpacer(@Dimension(unit = DP) widthDp: Int): Spacer =
    Spacer.Builder().setWidth(widthDp.toDp()).setHeight(expand()).build()

/** Builds a vertical Spacer, with height set to expand and width set to the given value. */
internal fun verticalSpacer(width: DimensionBuilders.ExpandedDimensionProp): Spacer =
    Spacer.Builder().setWidth(width).setHeight(expand()).build()

/**
 * Returns [wrap] but with minimum dimension of [MINIMUM_TAP_TARGET_SIZE] for accessibility
 * requirements of tap targets.
 */
internal fun wrapWithMinTapTargetDimension(): WrappedDimensionProp =
    WrappedDimensionProp.Builder().setMinimumSize(MINIMUM_TAP_TARGET_SIZE).build()

/** Returns the [Modifiers] object containing this padding and nothing else. */
internal fun Padding.toModifiers(): Modifiers = Modifiers.Builder().setPadding(this).build()

/** Returns the [Background] object containing this color and nothing else. */
internal fun LayoutColor.toBackground(): Background = Background.Builder().setColor(prop).build()

/** Returns the [Background] object containing this corner and nothing else. */
internal fun Corner.toBackground(): Background = Background.Builder().setCorner(this).build()

/**
 * Changes the opacity/transparency of the given color.
 *
 * Note that this only looks at the static value of the [LayoutColor], any dynamic value will be
 * ignored.
 */
public fun LayoutColor.withOpacity(@FloatRange(from = 0.0, to = 1.0) ratio: Float): LayoutColor {
    // From androidx.core.graphics.ColorUtils
    require(!(ratio < 0 || ratio > 1)) { "setOpacityForColor ratio must be between 0 and 1." }
    val fullyOpaque = 255
    val alphaMask = 0x00ffffff
    val alpha = (ratio * fullyOpaque).toInt()
    val alphaPosition = 24
    return ((this.staticArgb and alphaMask) or (alpha shl alphaPosition)).argb
}

/** Returns corresponding text alignment based on the given horizontal alignment. */
@TextAlignment
internal fun Int.horizontalAlignToTextAlign(): Int =
    when (this) {
        HORIZONTAL_ALIGN_CENTER -> LayoutElementBuilders.TEXT_ALIGN_CENTER
        HORIZONTAL_ALIGN_LEFT,
        HORIZONTAL_ALIGN_START -> LayoutElementBuilders.TEXT_ALIGN_START
        HORIZONTAL_ALIGN_END,
        HORIZONTAL_ALIGN_RIGHT -> LayoutElementBuilders.TEXT_ALIGN_END
        HORIZONTAL_ALIGN_UNDEFINED -> LayoutElementBuilders.TEXT_ALIGN_UNDEFINED
        else -> LayoutElementBuilders.TEXT_ALIGN_UNDEFINED
    }

/**
 * Returns whether the provided DP size is equal or above the [SCREEN_SIZE_BREAKPOINT_DP]
 * breakpoint.
 */
internal fun Int.isBreakpoint() = this >= SCREEN_SIZE_BREAKPOINT_DP

internal fun Int.toPadding(): Padding = Padding.Builder().setAll(this.toDp()).build()
