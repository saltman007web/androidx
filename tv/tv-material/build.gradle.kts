/*
 * Copyright (C) 2022 The Android Open Source Project
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

/**
 * This file was created using the `create_project.py` script located in the
 * `<AndroidX root>/development/project-creator` directory.
 *
 * Please use that script when creating a new project, rather than copying an existing project and
 * modifying its settings.
 */

import androidx.build.LibraryType

plugins {
    id("AndroidXPlugin")
    id("AndroidXComposePlugin")
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    api(libs.kotlinStdlib)

    api("androidx.annotation:annotation:1.8.1")

    val composeVersion = "1.6.8"
    api("androidx.compose.animation:animation:$composeVersion")
    api("androidx.compose.foundation:foundation:$composeVersion")
    api("androidx.compose.foundation:foundation-layout:$composeVersion")
    api("androidx.compose.runtime:runtime:$composeVersion")
    api("androidx.compose.material:material-icons-core:$composeVersion")
    api("androidx.compose.ui:ui-util:$composeVersion")
    api("androidx.compose.ui:ui:$composeVersion")
    api("androidx.compose.ui:ui-graphics:$composeVersion")
    api("androidx.compose.ui:ui-text:$composeVersion")

    implementation("androidx.profileinstaller:profileinstaller:1.4.0")

    androidTestImplementation(libs.truth)
    androidTestImplementation(project(":compose:runtime:runtime"))
    androidTestImplementation(project(":compose:ui:ui-test"))
    androidTestImplementation(project(":compose:ui:ui-test-junit4"))
    androidTestImplementation(project(":compose:test-utils"))
    androidTestImplementation(project(":test:screenshot:screenshot"))
    androidTestImplementation(project(":tv:tv-foundation"))
    androidTestImplementation(libs.testRunner)
}

android {
    compileSdk = 35
    namespace = "androidx.tv.material"
}

androidx {
    name = "TV Material"
    type = LibraryType.PUBLISHED_LIBRARY_ONLY_USED_BY_KOTLIN_CONSUMERS
    mavenVersion = LibraryVersions["TV_MATERIAL"]
    inceptionYear = "2022"
    description = "build TV applications using controls that adhere to Material Design Language."
    legacyDisableKotlinStrictApiMode = true
    metalavaK2UastEnabled = false
    samples(project(":tv:tv-material-samples"))
    addGoldenImageAssets()
}
