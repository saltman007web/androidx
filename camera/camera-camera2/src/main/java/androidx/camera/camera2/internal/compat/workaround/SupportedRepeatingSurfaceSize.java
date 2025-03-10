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

package androidx.camera.camera2.internal.compat.workaround;

import android.util.Size;

import androidx.camera.camera2.internal.compat.quirk.DeviceQuirks;
import androidx.camera.camera2.internal.compat.quirk.RepeatingStreamConstraintForVideoRecordingQuirk;
import androidx.camera.core.impl.utils.CompareSizesByArea;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gets the supported surface for configurations.
 */
public class SupportedRepeatingSurfaceSize {

    private final @Nullable RepeatingStreamConstraintForVideoRecordingQuirk mQuirk;

    public SupportedRepeatingSurfaceSize() {
        mQuirk = DeviceQuirks.get(RepeatingStreamConstraintForVideoRecordingQuirk.class);
    }

    private static final Size MINI_PREVIEW_SIZE_HUAWEI_MATE_9 = new Size(320, 240);

    private static final Comparator<Size> SIZE_COMPARATOR = new CompareSizesByArea();

    /** Gets the supported output resolution of the device. */
    public Size @NonNull [] getSupportedSizes(Size @NonNull [] sizes) {
        if (mQuirk != null) {
            if (RepeatingStreamConstraintForVideoRecordingQuirk.isHuaweiMate9()) {
                List<Size> supportedSizes = new ArrayList<>();
                for (Size s : sizes) {
                    if (SIZE_COMPARATOR.compare(s, MINI_PREVIEW_SIZE_HUAWEI_MATE_9) >= 0) {
                        supportedSizes.add(s);
                    }
                }
                return supportedSizes.toArray(new Size[0]);
            }
        }
        return sizes;
    }
}
