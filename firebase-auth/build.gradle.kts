/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

version = project.property("firebase-auth.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    //id("com.quittle.android-emulator") version "0.2.0"
}

//buildscript {
//    repositories {
//        google()
//        gradlePluginPortal()
//    }
//    dependencies {
//        classpath("com.android.tools.build:gradle:3.6.1")
//    }
//}

android {
    compileSdk = property("targetSdkVersion") as Int
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
        targetSdk = property("targetSdkVersion") as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.srcDir(file("src/androidAndroidTest/kotlin"))
            manifest.srcFile("src/androidAndroidTest/AndroidManifest.xml")
        }
    }
    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
    packagingOptions {
        resources.pickFirsts.add("META-INF/kotlinx-serialization-core.kotlin_module")
        resources.pickFirsts.add("META-INF/AL2.0")
        resources.pickFirsts.add("META-INF/LGPL2.1")
    }
    lint {
        abortOnError = false
    }
}

// Optional configuration
//androidEmulator {
//    emulator {
//        name("givlive_emulator")
//        sdkVersion(28)
//        abi("x86_64")
//        includeGoogleApis(true) // Defaults to false
//
//    }
//    headless(false)
//    logEmulatorOutput(false)
//}

val supportIosTarget = project.property("skipIosTarget") != "true"

kotlin {

    android {
        publishAllLibraryVariants()
    }

    if (supportIosTarget) {
        ios()
        iosSimulatorArm64()
        cocoapods {
            ios.deploymentTarget = "11.0"
            framework {
                baseName = "FirebaseAuth"
            }
            noPodspec()
            pod("FirebaseAuth") {
                version = "10.7.0"
            }
        }
    }

    js {
        useCommonJs()
        nodejs {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                apiVersion = "1.8"
                languageVersion = "1.8"
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-auth")
            }
        }

        val jvmMain by getting {
            kotlin.srcDir("src/androidMain/kotlin")
        }

        if (supportIosTarget) {
            val iosMain by getting
            val iosSimulatorArm64Main by getting
            iosSimulatorArm64Main.dependsOn(iosMain)
            val iosTest by sourceSets.getting
            val iosSimulatorArm64Test by getting
            iosSimulatorArm64Test.dependsOn(iosTest)
        }

        val jsMain by getting
    }
}

if (project.property("firebase-auth.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-auth.skipJsTests") == "true") {
    tasks.forEach {
        if (it.name.contains("js", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
