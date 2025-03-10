/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.camera.core.streamsharing;

import static androidx.camera.core.impl.UseCaseConfig.OPTION_CAPTURE_TYPE;
import static androidx.camera.core.internal.TargetConfig.OPTION_TARGET_CLASS;
import static androidx.camera.core.internal.TargetConfig.OPTION_TARGET_NAME;

import androidx.annotation.RestrictTo;
import androidx.camera.core.UseCase;
import androidx.camera.core.impl.CaptureConfig;
import androidx.camera.core.impl.MutableConfig;
import androidx.camera.core.impl.MutableOptionsBundle;
import androidx.camera.core.impl.OptionsBundle;
import androidx.camera.core.impl.SessionConfig;
import androidx.camera.core.impl.UseCaseConfig;
import androidx.camera.core.impl.UseCaseConfigFactory;
import androidx.camera.core.internal.TargetConfig;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * A empty builder for {@link StreamSharing}.
 *
 * <p> {@link StreamSharing} does not need a public builder. Instead, the caller should call the
 * constructor directly. This class exists because the {@link UseCase#getUseCaseConfigBuilder}
 * method requires a builder for each {@link UseCase}.
 */
class StreamSharingBuilder implements
        UseCaseConfig.Builder<StreamSharing, StreamSharingConfig, StreamSharingBuilder> {

    private static final String UNSUPPORTED_MESSAGE =
            "Operation not supported by StreamSharingBuilder.";

    private final MutableOptionsBundle mMutableConfig;

    StreamSharingBuilder() {
        this(MutableOptionsBundle.create());
    }

    StreamSharingBuilder(@NonNull MutableOptionsBundle mutableConfig) {
        mMutableConfig = mutableConfig;
        Class<?> oldConfigClass =
                mutableConfig.retrieveOption(TargetConfig.OPTION_TARGET_CLASS, null);
        if (oldConfigClass != null && !oldConfigClass.equals(StreamSharing.class)) {
            throw new IllegalArgumentException(
                    "Invalid target class configuration for "
                            + StreamSharingBuilder.this
                            + ": "
                            + oldConfigClass);
        }
        setCaptureType(UseCaseConfigFactory.CaptureType.STREAM_SHARING);
        setTargetClass(StreamSharing.class);
    }

    @Override
    public @NonNull MutableConfig getMutableConfig() {
        return mMutableConfig;
    }

    @Override
    public @NonNull StreamSharing build() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setDefaultSessionConfig(
            @NonNull SessionConfig sessionConfig) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setDefaultCaptureConfig(
            @NonNull CaptureConfig captureConfig) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setSessionOptionUnpacker(
            SessionConfig.@NonNull OptionUnpacker optionUnpacker) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setCaptureOptionUnpacker(
            CaptureConfig.@NonNull OptionUnpacker optionUnpacker) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setSurfaceOccupancyPriority(int priority) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setZslDisabled(boolean disabled) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingBuilder setHighResolutionDisabled(boolean disabled) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public @NonNull StreamSharingConfig getUseCaseConfig() {
        return new StreamSharingConfig(OptionsBundle.from(mMutableConfig));
    }

    @Override
    public @NonNull StreamSharingBuilder setTargetClass(@NonNull Class<StreamSharing> targetClass) {
        getMutableConfig().insertOption(OPTION_TARGET_CLASS, targetClass);
        // If no name is set yet, then generate a unique name
        if (null == getMutableConfig().retrieveOption(OPTION_TARGET_NAME, null)) {
            String targetName = targetClass.getCanonicalName() + "-" + UUID.randomUUID();
            setTargetName(targetName);
        }
        return this;
    }

    @Override
    public @NonNull StreamSharingBuilder setTargetName(@NonNull String targetName) {
        getMutableConfig().insertOption(OPTION_TARGET_NAME, targetName);
        return this;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Override
    public @NonNull StreamSharingBuilder setCaptureType(
            UseCaseConfigFactory.@NonNull CaptureType captureType) {
        getMutableConfig().insertOption(OPTION_CAPTURE_TYPE, captureType);
        return this;
    }
}
