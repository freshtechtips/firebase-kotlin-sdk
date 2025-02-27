/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.storage

import kotlinx.coroutines.CoroutineScope

actual val emulatorHost: String = "127.0.0.1"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) {
    kotlinx.coroutines.test.runTest { test() }
}