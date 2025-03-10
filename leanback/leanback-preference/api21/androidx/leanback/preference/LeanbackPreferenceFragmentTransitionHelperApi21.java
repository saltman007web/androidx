/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.leanback.preference;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.transition.Transition;
import android.view.Gravity;

import androidx.annotation.RestrictTo;
import androidx.leanback.transition.FadeAndShortSlide;

/**
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class LeanbackPreferenceFragmentTransitionHelperApi21 {

    @SuppressLint("ReferencesDeprecated")
    public static void addTransitions(Fragment f) {
        final Transition transitionStartEdge = new FadeAndShortSlide(Gravity.START);
        final Transition transitionEndEdge = new FadeAndShortSlide(Gravity.END);

        f.setEnterTransition(transitionEndEdge);
        f.setExitTransition(transitionStartEdge);
        f.setReenterTransition(transitionStartEdge);
        f.setReturnTransition(transitionEndEdge);
    }

    /**
     * Setup preference fragment transition.
     * @param f The preference fragment.
     */
    static void addTransitions(androidx.fragment.app.Fragment f) {
        final Transition transitionStartEdge = new FadeAndShortSlide(Gravity.START);
        final Transition transitionEndEdge = new FadeAndShortSlide(Gravity.END);

        f.setEnterTransition(transitionEndEdge);
        f.setExitTransition(transitionStartEdge);
        f.setReenterTransition(transitionStartEdge);
        f.setReturnTransition(transitionEndEdge);
    }

    private LeanbackPreferenceFragmentTransitionHelperApi21() {
    }
}
