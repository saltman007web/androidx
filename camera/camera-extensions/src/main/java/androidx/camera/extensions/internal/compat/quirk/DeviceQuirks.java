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

package androidx.camera.extensions.internal.compat.quirk;

import static androidx.camera.core.impl.utils.executor.CameraXExecutors.directExecutor;

import androidx.camera.core.Logger;
import androidx.camera.core.impl.Quirk;
import androidx.camera.core.impl.QuirkSettingsHolder;
import androidx.camera.core.impl.Quirks;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provider of device specific quirks, which are used for device specific workarounds.
 * <p>
 * Device specific quirks depend on device properties, including the manufacturer
 * ({@link android.os.Build#MANUFACTURER}), model ({@link android.os.Build#MODEL}) and OS
 * level ({@link android.os.Build.VERSION#SDK_INT}).
 * <p>
 * Device specific quirks are lazily loaded, i.e. They are loaded the first time they're needed.
 */
public class DeviceQuirks {
    private static final String TAG = "DeviceQuirks";

    /** @noinspection NotNullFieldNotInitialized*/
    private static volatile @NonNull Quirks sQuirks;

    static {
        // Direct executor will initialize quirks immediately, guaranteeing it's never null.
        QuirkSettingsHolder.instance().observe(directExecutor(), quirkSettings -> {
            sQuirks = new Quirks(DeviceQuirksLoader.loadQuirks(quirkSettings));
            Logger.d(TAG, "extensions DeviceQuirks = " + Quirks.toString(sQuirks));
        });
    }

    private DeviceQuirks() {
    }

    /** Returns all device specific quirks loaded on the current device. */
    public static @NonNull Quirks getAll() {
        return sQuirks;
    }

    /**
     * Retrieves a specific device {@link Quirk} instance given its type.
     *
     * @param quirkClass The type of device quirk to retrieve.
     * @return A device {@link Quirk} instance of the provided type, or {@code null} if it isn't
     * found.
     */
    public static <T extends Quirk> @Nullable T get(final @NonNull Class<T> quirkClass) {
        return sQuirks.get(quirkClass);
    }
}
