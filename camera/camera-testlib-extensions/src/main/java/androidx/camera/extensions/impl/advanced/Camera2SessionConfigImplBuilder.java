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

package androidx.camera.extensions.impl.advanced;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder implementation to help OEM build the {@link Camera2SessionConfigImpl} instance.
 */
public class Camera2SessionConfigImplBuilder {
    private int mSessionTemplateId = CameraDevice.TEMPLATE_PREVIEW;
    private int mSessionType = SessionConfiguration.SESSION_REGULAR;
    Map<CaptureRequest.Key<?>, Object> mSessionParameters = new HashMap<>();
    List<Camera2OutputConfigImpl> mCamera2OutputConfigs = new ArrayList<>();

    public Camera2SessionConfigImplBuilder() {
    }

    /**
     * Adds a output config.
     */
    public @NonNull Camera2SessionConfigImplBuilder addOutputConfig(
            @NonNull Camera2OutputConfigImpl outputConfig) {
        mCamera2OutputConfigs.add(outputConfig);
        return this;
    }

    /**
     * Sets session parameters.
     */
    public <T> @NonNull Camera2SessionConfigImplBuilder addSessionParameter(
            CaptureRequest.@NonNull Key<T> key, @NonNull T value) {
        mSessionParameters.put(key, value);
        return this;
    }

    /**
     * Sets the template id for session parameters request.
     */
    public @NonNull Camera2SessionConfigImplBuilder setSessionTemplateId(int templateId) {
        mSessionTemplateId = templateId;
        return this;
    }

    /**
     * Sets the session type for the session.
     */
    public @NonNull Camera2SessionConfigImplBuilder setSessionType(int sessionType) {
        mSessionType = sessionType;
        return this;
    }

    /**
     * Gets the session template id.
     */
    public int getSessionTemplateId() {
        return mSessionTemplateId;
    }

    /**
     * Gets the session parameters.
     */
    public @NonNull Map<CaptureRequest.Key<?>, Object> getSessionParameters() {
        return mSessionParameters;
    }

    /**
     * Gets all the output configs.
     */
    public @NonNull List<Camera2OutputConfigImpl> getCamera2OutputConfigs() {
        return mCamera2OutputConfigs;
    }

    /**
     * Gets the camera capture session type.
     */
    public int getSessionType() {
        return mSessionType;
    }

    /**
     * Builds a {@link Camera2SessionConfigImpl} instance.
     */
    public @NonNull Camera2SessionConfigImpl build() {
        return new Camera2SessionConfigImplImpl(this);
    }

    private static class Camera2SessionConfigImplImpl implements
            Camera2SessionConfigImpl {
        private final int mSessionTemplateId;
        private final int mSessionType;
        private final Map<CaptureRequest.Key<?>, Object> mSessionParameters;
        private final List<Camera2OutputConfigImpl> mCamera2OutputConfigs;

        Camera2SessionConfigImplImpl(@NonNull Camera2SessionConfigImplBuilder builder) {
            mSessionTemplateId = builder.getSessionTemplateId();
            mSessionParameters = builder.getSessionParameters();
            mCamera2OutputConfigs = builder.getCamera2OutputConfigs();
            mSessionType = builder.getSessionType();
        }

        @Override
        public @NonNull List<Camera2OutputConfigImpl> getOutputConfigs() {
            return mCamera2OutputConfigs;
        }

        @Override
        public @NonNull Map<CaptureRequest.Key<?>, Object> getSessionParameters() {
            return mSessionParameters;
        }

        @Override
        public int getSessionTemplateId() {
            return mSessionTemplateId;
        }

        @Override
        public int getSessionType() {
            return mSessionType;
        }
    }
}

