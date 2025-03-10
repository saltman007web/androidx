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

package androidx.xr.scenecore

import androidx.annotation.RestrictTo

/**
 * PointSourceAttributes is used to configure a sound be spatialized as a 3D point.
 *
 * If the audio being played is stereo or multichannel AND the AudioAttributes USAGE_TYPE is
 * USAGE_MEDIA then the point provided will serve as the focal point of the media sound bed.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class PointSourceAttributes(public val entity: Entity) {

    internal val rtPointSourceAttributes: JxrPlatformAdapter.PointSourceAttributes =
        JxrPlatformAdapter.PointSourceAttributes((entity as BaseEntity<*>).rtEntity)
}

internal fun JxrPlatformAdapter.PointSourceAttributes.toPointSourceAttributes(
    session: Session
): PointSourceAttributes? {
    val jxrEntity = session.getEntityForRtEntity(entity)
    return jxrEntity?.let { PointSourceAttributes(it) }
}
