/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package androidx.leanback.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RestrictTo;
import androidx.leanback.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * DividerPresenter provides a default presentation for {@link DividerRow} in HeadersFragment.
 */
public class DividerPresenter extends Presenter {

    private final int mLayoutResourceId;

    public DividerPresenter() {
        this(R.layout.lb_divider);
    }

    /**
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public DividerPresenter(int layoutResourceId) {
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public Presenter.@NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        View headerView = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutResourceId, parent, false);

        return new ViewHolder(headerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
    }

}
