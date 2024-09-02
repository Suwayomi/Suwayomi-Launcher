package suwayomi.tachidesk.launcher.util

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

suspend fun checkIfPortInUse(
    ip: String,
    port: Int,
): Boolean {
    val appIP = if (ip == "0.0.0.0") "127.0.0.1" else ip

    try {
        withContext(Dispatchers.IO) {
            val client =
                OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.MILLISECONDS)
                    .build()

            client.newCall(
                Request.Builder()
                    .get()
                    .url("http://$appIP:$port/api/v1/settings/about/")
                    .build(),
            ).execute().body.string()
        }
        return true
    } catch (e: IOException) {
        return false
    }
}
