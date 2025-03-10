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

package androidx.core.google.shortcuts.builders;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.core.google.shortcuts.builders.Constants.PARAMETER_TYPE;
import static androidx.core.google.shortcuts.builders.Constants.PARAMETER_VALUE_KEY;

import androidx.annotation.RestrictTo;

import com.google.android.gms.appindex.builders.IndexableBuilder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Builder for the Parameter section in the Shortcut Corpus.
 *
 */
@RestrictTo(LIBRARY)
public class ParameterBuilder
        extends IndexableBuilder<ParameterBuilder> {
    private String[] mValues;

    public ParameterBuilder() {
        super(PARAMETER_TYPE);
    }

    /** Sets one or more values for given parameter. */
    public @NonNull ParameterBuilder setValue(String @NonNull ... value) {
        mValues = value;
        return put(PARAMETER_VALUE_KEY, value);
    }

    public String @Nullable [] getValues() {
        return mValues;
    }
}
