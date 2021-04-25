/*
 * Copyright (c) 2020-2021, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY,WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tomg.githubreleasemonitor.login

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.tomg.githubreleasemonitor.TOP_LEVEL_PACKAGE_NAME

object AeadFactory {

    private const val KEYSET_NAME = TOP_LEVEL_PACKAGE_NAME + "PREF_KEY_KEYSET"
    private const val PREFERENCE_FILE_NAME = TOP_LEVEL_PACKAGE_NAME + "PREF_MASTER_KEY"
    private const val URI_MASTER_KEY = "android-keystore://$KEYSET_NAME"

    fun create(context: Context): Aead = AndroidKeysetManager
        .Builder()
        .withSharedPref(context.applicationContext, KEYSET_NAME, PREFERENCE_FILE_NAME)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri(URI_MASTER_KEY)
        .build()
        .keysetHandle
        .getPrimitive(Aead::class.java)
}

class LazyAead(
    val produceAead: () -> Aead
) : Lazy<Aead> {

    @Volatile
    private var _value: Aead? = null

    override val value: Aead
        get() {
            val v = _value
            return v ?: synchronized(this) {
                val v2 = produceAead()
                _value = v2
                v2
            }
        }

    override fun isInitialized() = _value != null
}

fun Context.lazyAead() = LazyAead {
    AeadConfig.register()
    AeadFactory.create(this)
}
