/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.camera.core;

import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;

import androidx.annotation.GuardedBy;
import androidx.camera.core.impl.ImageReaderProxy;
import androidx.camera.core.impl.utils.MainThreadAsyncHandler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Executor;

/**
 * An {@link ImageReaderProxy} which wraps around an {@link ImageReader}.
 *
 * <p>All methods map one-to-one between this {@link ImageReaderProxy} and the wrapped {@link
 * ImageReader}.
 */
class AndroidImageReaderProxy implements ImageReaderProxy {
    @GuardedBy("mLock")
    private final ImageReader mImageReader;
    private final Object mLock = new Object();

    @GuardedBy("mLock")
    private boolean mIsImageAvailableListenerCleared = true;

    /**
     * Creates a new instance which wraps the given image reader.
     *
     * @param imageReader to wrap
     * @return new {@link AndroidImageReaderProxy} instance
     */
    AndroidImageReaderProxy(ImageReader imageReader) {
        mImageReader = imageReader;
    }

    @Override
    public @Nullable ImageProxy acquireLatestImage() {
        synchronized (mLock) {
            Image image;
            try {
                image = mImageReader.acquireLatestImage();
            } catch (RuntimeException e) {
            /* In API level 23 or below,  it will throw "java.lang.RuntimeException:
               ImageReaderContext is not initialized" when ImageReader is closed. To make the
               behavior consistent as newer API levels,  we make it return null Image instead.*/
                if (isImageReaderContextNotInitializedException(e)) {
                    image = null;
                } else {
                    throw e;  // only catch RuntimeException:ImageReaderContext is not initialized
                }
            }

            if (image == null) {
                return null;
            }
            return new AndroidImageProxy(image);
        }
    }

    @Override
    public @Nullable ImageProxy acquireNextImage() {
        synchronized (mLock) {
            Image image;
            try {
                image = mImageReader.acquireNextImage();
            } catch (RuntimeException e) {
            /* In API level 23 or below,  it will throw "java.lang.RuntimeException:
               ImageReaderContext is not initialized" when ImageReader is closed. To make the
               behavior consistent as newer API levels,  we make it return null Image instead.*/
                if (isImageReaderContextNotInitializedException(e)) {
                    image = null;
                } else {
                    throw e;  // only catch RuntimeException:ImageReaderContext is not initialized
                }
            }

            if (image == null) {
                return null;
            }
            return new AndroidImageProxy(image);
        }
    }

    private boolean isImageReaderContextNotInitializedException(RuntimeException e) {
        return "ImageReaderContext is not initialized".equals(e.getMessage());
    }

    @Override
    public void close() {
        synchronized (mLock) {
            mImageReader.close();
        }
    }

    @Override
    public int getHeight() {
        synchronized (mLock) {
            return mImageReader.getHeight();
        }
    }

    @Override
    public int getWidth() {
        synchronized (mLock) {
            return mImageReader.getWidth();
        }
    }

    @Override
    public int getImageFormat() {
        synchronized (mLock) {
            return mImageReader.getImageFormat();
        }
    }

    @Override
    public int getMaxImages() {
        synchronized (mLock) {
            return mImageReader.getMaxImages();
        }
    }

    @Override
    public @Nullable Surface getSurface() {
        synchronized (mLock) {
            return mImageReader.getSurface();
        }
    }

    @Override
    public void setOnImageAvailableListener(
            @NonNull OnImageAvailableListener listener,
            @NonNull Executor executor) {
        synchronized (mLock) {
            mIsImageAvailableListenerCleared = false;
            // ImageReader does not accept an executor. As a workaround, the callback is run on main
            // handler then immediately posted to the executor.
            ImageReader.OnImageAvailableListener transformedListener = (imageReader) -> {
                synchronized (mLock) {
                    // There might be a timing issue that the listener is executed after
                    // clearOnImageAvailableListener() has been called. Uses a flag to skip the
                    // execution if the listener has actually been cleared.
                    if (!mIsImageAvailableListenerCleared) {
                        executor.execute(
                                () -> listener.onImageAvailable(AndroidImageReaderProxy.this));
                    }
                }
            };
            mImageReader.setOnImageAvailableListener(transformedListener,
                    MainThreadAsyncHandler.getInstance());
        }
    }

    @Override
    public void clearOnImageAvailableListener() {
        synchronized (mLock) {
            mIsImageAvailableListenerCleared = true;
            mImageReader.setOnImageAvailableListener(null, null);
        }
    }
}
