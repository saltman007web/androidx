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

package androidx.wear.protolayout.material;

import static androidx.wear.protolayout.DimensionBuilders.dp;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.wear.protolayout.DimensionBuilders.DpProp;

import org.jspecify.annotations.NonNull;

/** Contains the default values used by {@link CircularProgressIndicator}. */
public class ProgressIndicatorDefaults {
    private ProgressIndicatorDefaults() {}

    /** The default stroke width for {@link CircularProgressIndicator} */
    public static final @NonNull DpProp DEFAULT_STROKE_WIDTH = dp(8);

    /** The default padding for {@link CircularProgressIndicator} */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static final @NonNull DpProp DEFAULT_PADDING = dp(6);

    /** The recommended colors for {@link CircularProgressIndicator}. */
    public static final @NonNull ProgressIndicatorColors DEFAULT_COLORS =
            ProgressIndicatorColors.progressIndicatorColors(Colors.DEFAULT);

    static final float DEFAULT_GAP_LENGTH = 47.8f;

    /** The recommended start angle for {@link CircularProgressIndicator} if there's a gap. */
    public static final float GAP_START_ANGLE = 180 + DEFAULT_GAP_LENGTH / 2 - 360;

    /** The recommended end angle for {@link CircularProgressIndicator} if there's a gap. */
    public static final float GAP_END_ANGLE = 180 - DEFAULT_GAP_LENGTH / 2;

    /** Start angle for full length {@link CircularProgressIndicator}. */
    static final float DEFAULT_START_ANGLE = 0;

    /** End angle for full length {@link CircularProgressIndicator}. */
    static final float DEFAULT_END_ANGLE = 360;
}
