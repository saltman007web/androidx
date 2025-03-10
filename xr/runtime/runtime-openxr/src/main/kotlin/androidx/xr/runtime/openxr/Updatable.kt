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

package androidx.xr.runtime.openxr

/** Describes an entity that can be updated across time. */
internal interface Updatable {
    /**
     * Updates the entity retrieving its state at [xrTime].
     *
     * @param xrTime the number of nanoseconds since the start of the OpenXR epoch.
     */
    fun update(xrTime: Long)
}
