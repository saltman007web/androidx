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

package androidx.wear.tiles;

import org.jspecify.annotations.NonNull;

/**
 * Interface used for a Tile Service to notify a Tile Renderer that it should fetch a new Timeline
 * from it.
 */
public interface TileUpdateRequester {
    /** Notify the Tile Renderer that it should fetch a new Timeline from this Tile Service. */
    void requestUpdate(@NonNull Class<? extends TileService> tileService);
}
