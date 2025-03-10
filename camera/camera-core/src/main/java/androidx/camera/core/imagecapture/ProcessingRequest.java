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

package androidx.camera.core.imagecapture;

import static java.util.Objects.requireNonNull;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;

import androidx.annotation.MainThread;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.impl.CaptureBundle;
import androidx.camera.core.impl.CaptureStage;

import com.google.common.util.concurrent.ListenableFuture;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A post-processing request and its callback.
 */
class ProcessingRequest {
    private final int mRequestId;
    @NonNull TakePictureRequest mTakePictureRequest;
    private final ImageCapture.@Nullable OutputFileOptions mOutputFileOptions;
    private final ImageCapture.@Nullable OutputFileOptions mSecondaryOutputFileOptions;
    private final @NonNull Rect mCropRect;
    private final int mRotationDegrees;
    private final int mJpegQuality;
    private final @NonNull Matrix mSensorToBufferTransform;
    private final @NonNull TakePictureCallback mCallback;
    private final @NonNull String mTagBundleKey;
    private final @NonNull List<Integer> mStageIds;

    final @NonNull ListenableFuture<Void> mCaptureFuture;
    static final int PROGRESS_NOT_RECEIVED = -1;
    private int mLastCaptureProcessProgressed = PROGRESS_NOT_RECEIVED;

    ProcessingRequest(
            @NonNull CaptureBundle captureBundle,
            @NonNull TakePictureRequest takePictureRequest,
            @NonNull TakePictureCallback callback,
            @NonNull ListenableFuture<Void> captureFuture) {
        this(captureBundle, takePictureRequest, callback, captureFuture, 0);
    }
    ProcessingRequest(
            @NonNull CaptureBundle captureBundle,
            @NonNull TakePictureRequest takePictureRequest,
            @NonNull TakePictureCallback callback,
            @NonNull ListenableFuture<Void> captureFuture,
            int requestId) {
        mRequestId = requestId;
        mTakePictureRequest = takePictureRequest;
        mOutputFileOptions = takePictureRequest.getOutputFileOptions();
        mSecondaryOutputFileOptions = takePictureRequest.getSecondaryOutputFileOptions();
        mJpegQuality = takePictureRequest.getJpegQuality();
        mRotationDegrees = takePictureRequest.getRotationDegrees();
        mCropRect = takePictureRequest.getCropRect();
        mSensorToBufferTransform = takePictureRequest.getSensorToBufferTransform();
        mCallback = callback;
        mTagBundleKey = String.valueOf(captureBundle.hashCode());
        mStageIds = new ArrayList<>();
        for (CaptureStage captureStage : requireNonNull(captureBundle.getCaptureStages())) {
            mStageIds.add(captureStage.getId());
        }
        mCaptureFuture = captureFuture;
    }

    @NonNull String getTagBundleKey() {
        return mTagBundleKey;
    }

    @NonNull List<Integer> getStageIds() {
        return mStageIds;
    }

    public int getRequestId() {
        return mRequestId;
    }

    @NonNull TakePictureRequest getTakePictureRequest() {
        return mTakePictureRequest;
    }

    ImageCapture.@Nullable OutputFileOptions getOutputFileOptions() {
        return mOutputFileOptions;
    }

    ImageCapture.@Nullable OutputFileOptions getSecondaryOutputFileOptions() {
        return mSecondaryOutputFileOptions;
    }

    @NonNull Rect getCropRect() {
        return mCropRect;
    }

    int getRotationDegrees() {
        return mRotationDegrees;
    }

    int getJpegQuality() {
        return mJpegQuality;
    }

    @NonNull Matrix getSensorToBufferTransform() {
        return mSensorToBufferTransform;
    }

    boolean isInMemoryCapture() {
        return getOutputFileOptions() == null && getSecondaryOutputFileOptions() == null;
    }

    /**
     * @see TakePictureCallback#onCaptureStarted()
     */
    @MainThread
    void onCaptureStarted() {
        mCallback.onCaptureStarted();
    }

    @MainThread
    void onCaptureProcessProgressed(int progress) {
        if (mLastCaptureProcessProgressed != progress) {
            mLastCaptureProcessProgressed = progress;
            mCallback.onCaptureProcessProgressed(progress);
        }
    }

    /**
     * @see TakePictureCallback#onImageCaptured()
     */
    @MainThread
    void onImageCaptured() {
        // If process progress has ever been sent, ensure progress 100 is sent before image sent.
        if (mLastCaptureProcessProgressed != PROGRESS_NOT_RECEIVED) {
            onCaptureProcessProgressed(100);
        }

        mCallback.onImageCaptured();
    }

    /**
     * @see TakePictureCallback#onFinalResult
     */
    @MainThread
    void onFinalResult(ImageCapture.@NonNull OutputFileResults outputFileResults) {
        mCallback.onFinalResult(outputFileResults);
    }

    void onPostviewBitmapAvailable(@NonNull Bitmap bitmap) {
        mCallback.onPostviewBitmapAvailable(bitmap);
    }

    /**
     * @see TakePictureCallback#onFinalResult
     */
    @MainThread
    void onFinalResult(@NonNull ImageProxy imageProxy) {
        mCallback.onFinalResult(imageProxy);
    }

    /**
     * @see TakePictureCallback#onProcessFailure
     */
    @MainThread
    void onProcessFailure(@NonNull ImageCaptureException imageCaptureException) {
        mCallback.onProcessFailure(imageCaptureException);
    }

    /**
     * @see TakePictureCallback#onCaptureFailure
     */
    @MainThread
    void onCaptureFailure(@NonNull ImageCaptureException imageCaptureException) {
        mCallback.onCaptureFailure(imageCaptureException);
    }

    /**
     * Returns true if the request has been aborted by the app/lifecycle.
     */
    boolean isAborted() {
        return mCallback.isAborted();
    }

    @NonNull ListenableFuture<Void> getCaptureFuture() {
        return mCaptureFuture;
    }
}
