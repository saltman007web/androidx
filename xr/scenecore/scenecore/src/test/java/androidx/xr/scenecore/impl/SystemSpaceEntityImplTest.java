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

package androidx.xr.scenecore.impl;

import static androidx.xr.runtime.testing.math.MathAssertions.assertPose;
import static androidx.xr.runtime.testing.math.MathAssertions.assertVector3;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.verify;

import androidx.xr.extensions.node.Mat4f;
import androidx.xr.runtime.math.Matrix4;
import androidx.xr.runtime.math.Pose;
import androidx.xr.runtime.math.Quaternion;
import androidx.xr.runtime.math.Vector3;
import androidx.xr.scenecore.JxrPlatformAdapter.SystemSpaceEntity.OnSpaceUpdatedListener;
import androidx.xr.scenecore.testing.FakeScheduledExecutorService;
import androidx.xr.scenecore.testing.FakeXrExtensions.FakeCloseable;
import androidx.xr.scenecore.testing.FakeXrExtensions.FakeNode;
import androidx.xr.scenecore.testing.FakeXrExtensions.FakeNodeTransform;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Abstract test class for {@link SystemSpaceEntityImpl} implementations.
 *
 * <p>Concrete implementations of {@link SystemSpaceEntityImpl} should extend this class and provide
 * implementations for its abstract methods to ensure they comply with the abstract class.
 */
public abstract class SystemSpaceEntityImplTest {

    /** Returns the {@link SystemSpaceEntityImpl} instance to test. */
    protected abstract SystemSpaceEntityImpl getSystemSpaceEntityImpl();

    /** Returns the default fake executor used by the {@link SystemSpaceEntityImpl} constructor. */
    protected abstract FakeScheduledExecutorService getDefaultFakeExecutor();

    /** Returns an arbitrary {@link AndroidXrEntity} instance which can set its parent. */
    protected abstract AndroidXrEntity createChildAndroidXrEntity();

    /** Returns the {@link ActivitySpaceImpl} instance which is the root of the Activity Space. */
    protected abstract ActivitySpaceImpl getActivitySpaceEntity();

    @Test
    public void systemSpaceEntityImplConstructor_setsNodeTransformSubscription() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeScheduledExecutorService fakeExecutor = getDefaultFakeExecutor();
        FakeNode node = (FakeNode) systemSpaceEntity.getNode();
        assertThat(node.getTransformListener()).isNotNull();
        assertThat(node.getTransformExecutor()).isEqualTo(fakeExecutor);
        assertThat(systemSpaceEntity.nodeTransformCloseable).isNotNull();
    }

    @Test
    public void dispose_closesNodeTransformSubscription() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeCloseable nodeTransformCloseable =
                (FakeCloseable) systemSpaceEntity.nodeTransformCloseable;
        assertThat(nodeTransformCloseable.isClosed()).isFalse();

        systemSpaceEntity.dispose();
        assertThat(nodeTransformCloseable.isClosed()).isTrue();
    }

    @Test
    public void getPoseInOpenXrReferenceSpace_defaultsToNull() {
        assertThat(getSystemSpaceEntityImpl().getPoseInOpenXrReferenceSpace()).isNull();
    }

    @Test
    public void setOnSpaceUpdatedListener_callListenersOnActivitySpaceUpdated() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        OnSpaceUpdatedListener listener1 = Mockito.mock(OnSpaceUpdatedListener.class);
        FakeScheduledExecutorService executor1 = new FakeScheduledExecutorService();

        systemSpaceEntity.setOnSpaceUpdatedListener(listener1, executor1);
        systemSpaceEntity.onSpaceUpdated();
        assertThat(executor1.hasNext()).isTrue();
        executor1.runAll();

        verify(listener1).onSpaceUpdated();
    }

    @Test
    public void
            setOnSpaceUpdatedListener_multipleListeners_callLastListenersOnActivitySpaceUpdated() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        OnSpaceUpdatedListener listener1 = Mockito.mock(OnSpaceUpdatedListener.class);
        OnSpaceUpdatedListener listener2 = Mockito.mock(OnSpaceUpdatedListener.class);
        FakeScheduledExecutorService executor1 = new FakeScheduledExecutorService();
        FakeScheduledExecutorService executor2 = new FakeScheduledExecutorService();

        systemSpaceEntity.setOnSpaceUpdatedListener(listener1, executor1);
        // This should override the previous listener.
        systemSpaceEntity.setOnSpaceUpdatedListener(listener2, executor2);
        systemSpaceEntity.onSpaceUpdated();

        assertThat(executor1.hasNext()).isFalse();
        assertThat(executor2.hasNext()).isTrue();

        executor1.runAll();
        executor2.runAll();

        verify(listener1, Mockito.never()).onSpaceUpdated();
        verify(listener2).onSpaceUpdated();
    }

    @Test
    public void setOnSpaceUpdatedListener_withNullExecutor_usesInternalExecutor() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeScheduledExecutorService fakeExecutor = getDefaultFakeExecutor();
        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);

        systemSpaceEntity.setOnSpaceUpdatedListener(listener, null);
        systemSpaceEntity.onSpaceUpdated();

        assertThat(fakeExecutor.hasNext()).isTrue();
        fakeExecutor.runAll();
        verify(listener).onSpaceUpdated();
    }

    @Test
    public void setOnSpaceUpdatedListener_withNullListener_noListenerCallOnActivitySpaceUpdated() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);
        FakeScheduledExecutorService executor = new FakeScheduledExecutorService();
        systemSpaceEntity.setOnSpaceUpdatedListener(listener, executor);
        systemSpaceEntity.setOnSpaceUpdatedListener(null, executor);

        systemSpaceEntity.onSpaceUpdated();
        executor.runAll();

        verify(listener, Mockito.never()).onSpaceUpdated();
    }

    @Test
    public void getPoseInOpenXrReferenceSpace_returnsPoseFromSubscribeToNodeTransform() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeNode node = (FakeNode) systemSpaceEntity.getNode();
        Mat4f mat4f =
                new Mat4f( // --                Column major, right handed 4x4 Transformation Matrix
                        // with
                        new float[] { // --         translation of (4, 8, 12) and rotation 90 (@)
                            // around Z axis
                            0f, 1f, 0f, 0f, // --     cos(@),   sin(@), 0,  0
                            -1f, 0f, 0f, 0f, // --    -sin(@),  cos(@), 0,  0
                            0f, 0f, 1f, 0f, // --     0,        0,      1,  0
                            4f, 8f, 12f, 1f, // --    tx,       ty,     tz, 1
                        });
        FakeNodeTransform nodeTransformEvent = new FakeNodeTransform(mat4f);

        node.sendTransformEvent(nodeTransformEvent);
        getDefaultFakeExecutor().runAll();

        Pose expectedPose =
                new Pose(
                        new Vector3(4f, 8f, 12f),
                        Quaternion.fromAxisAngle(new Vector3(0f, 0f, 1f), 90f));

        assertPose(systemSpaceEntity.getPoseInOpenXrReferenceSpace(), expectedPose);
    }

    @Test
    public void setOnSpaceUpdatedListener_callsListenerOnNodeTransformEvent() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeNode node = (FakeNode) systemSpaceEntity.getNode();
        Mat4f mat4f = new Mat4f(Matrix4.Identity.getData());
        FakeNodeTransform nodeTransformEvent = new FakeNodeTransform(mat4f);

        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);
        FakeScheduledExecutorService executor = new FakeScheduledExecutorService();
        systemSpaceEntity.setOnSpaceUpdatedListener(listener, executor);

        node.sendTransformEvent(nodeTransformEvent);
        getDefaultFakeExecutor().runAll();

        assertThat(executor.hasNext()).isTrue();
        executor.runAll();

        verify(listener).onSpaceUpdated();
    }

    @Test
    public void
            setOnSpaceUpdatedListener_multipleListeners_callsLastListenerOnNodeTransformEvent() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeScheduledExecutorService fakeExecutor = getDefaultFakeExecutor();
        FakeNode node = (FakeNode) systemSpaceEntity.getNode();
        Mat4f mat4f = new Mat4f(Matrix4.Identity.getData());
        FakeNodeTransform nodeTransformEvent = new FakeNodeTransform(mat4f);

        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);
        OnSpaceUpdatedListener listener2 = Mockito.mock(OnSpaceUpdatedListener.class);
        FakeScheduledExecutorService executor = new FakeScheduledExecutorService();
        systemSpaceEntity.setOnSpaceUpdatedListener(listener, executor);
        systemSpaceEntity.setOnSpaceUpdatedListener(listener2, executor);

        node.sendTransformEvent(nodeTransformEvent);
        fakeExecutor.runAll();
        assertThat(executor.hasNext()).isTrue();
        executor.runAll();

        verify(listener, Mockito.never()).onSpaceUpdated();
        verify(listener2).onSpaceUpdated();
    }

    @Test
    public void
            setOnSpaceUpdatedListener_withNullExecutor_callsListenerOnNodeTransformEventExecutor() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeNode node = (FakeNode) systemSpaceEntity.getNode();
        Mat4f mat4f = new Mat4f(Matrix4.Identity.getData());
        FakeNodeTransform nodeTransformEvent = new FakeNodeTransform(mat4f);

        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);
        systemSpaceEntity.setOnSpaceUpdatedListener(listener, null);

        node.sendTransformEvent(nodeTransformEvent);
        getDefaultFakeExecutor().runAll();

        verify(listener).onSpaceUpdated();
    }

    @Test
    public void setOnSpaceUpdatedListener_withNullListener_noListenerCalledOnNodeTransformEvent() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        FakeNode node = (FakeNode) systemSpaceEntity.getNode();
        Mat4f mat4f = new Mat4f(Matrix4.Identity.getData());
        FakeNodeTransform nodeTransformEvent = new FakeNodeTransform(mat4f);

        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);
        systemSpaceEntity.setOnSpaceUpdatedListener(listener, null);
        systemSpaceEntity.setOnSpaceUpdatedListener(null, null);

        node.sendTransformEvent(nodeTransformEvent);
        getDefaultFakeExecutor().runAll();

        verify(listener, Mockito.never()).onSpaceUpdated();
    }

    @Test
    public void dispose_disposesChildren() throws Exception {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        AndroidXrEntity childEntity = createChildAndroidXrEntity();

        systemSpaceEntity.addChild(childEntity);

        // Verify the parent of the child node is the space node before disposing it.
        FakeNode systemSpaceNode = (FakeNode) systemSpaceEntity.getNode();
        FakeNode childNode = (FakeNode) childEntity.getNode();
        assertThat(childNode.getParent()).isEqualTo(systemSpaceNode);

        // Dispose the space entity and verify that the children were disposed.
        systemSpaceEntity.dispose();

        assertThat(childNode.getParent()).isNull();
    }

    @Test
    public void setPoseInOpenXrReferenceSpace_callsOnSpaceUpdated() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        OnSpaceUpdatedListener listener = Mockito.mock(OnSpaceUpdatedListener.class);
        FakeScheduledExecutorService executor = new FakeScheduledExecutorService();

        systemSpaceEntity.setOnSpaceUpdatedListener(listener, executor);
        systemSpaceEntity.setOpenXrReferenceSpacePose(Matrix4.Identity);
        executor.runAll();

        verify(listener).onSpaceUpdated();
    }

    @Test
    public void setPoseInOpenXrReferenceSpace_updatesPose() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        Matrix4 matrix =
                new Matrix4( // --               Column major, right handed 4x4 Transformation
                        // Matrix with
                        new float[] { // --         translation of (4, 8, 12) and rotation 90 (@)
                            // around Z axis
                            0f, 1f, 0f, 0f, // --     cos(@),   sin(@), 0,  0
                            -1f, 0f, 0f, 0f, // --    -sin(@),  cos(@), 0,  0
                            0f, 0f, 1f, 0f, // --     0,        0,      1,  0
                            4f, 8f, 12f, 1f, // --    tx,       ty,     tz, 1
                        });
        Pose pose =
                new Pose(
                        new Vector3(4f, 8f, 12f),
                        Quaternion.fromAxisAngle(new Vector3(0f, 0f, 1f), 90f));

        systemSpaceEntity.setOpenXrReferenceSpacePose(matrix);
        assertPose(systemSpaceEntity.getPoseInOpenXrReferenceSpace(), pose);
    }

    @Test
    public void setPoseInOpenXrReferenceSpace_updatesScale() {
        SystemSpaceEntityImpl systemSpaceEntity = getSystemSpaceEntityImpl();
        Matrix4 matrix =
                new Matrix4( // --               Column major, right handed 4x4 Transformation
                        // Matrix with
                        new float // --             translation of (4, 8, 12) and rotation 90 (@)
                                // around Z axis,
                                [] { // --              and scale of 3.3.
                            0f, 3.3f, 0f, 0f, // --     cos(@),   sin(@), 0,  0
                            -3.3f, 0f, 0f, 0f, // --    -sin(@),  cos(@), 0,  0
                            0f, 0f, 3.3f, 0f, // --     0,        0,      1,  0
                            4f, 8f, 12f, 1f, // --      tx,       ty,     tz, 1
                        });
        Vector3 scale = new Vector3(3.3f, 3.3f, 3.3f);

        systemSpaceEntity.setOpenXrReferenceSpacePose(matrix);
        assertVector3(
                systemSpaceEntity.getActivitySpaceScale(),
                scale.div(getActivitySpaceEntity().getWorldSpaceScale()));
        assertVector3(systemSpaceEntity.getWorldSpaceScale(), scale);
        assertVector3(systemSpaceEntity.getScale(), scale);
    }
}
