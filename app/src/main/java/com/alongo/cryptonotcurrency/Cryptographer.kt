package com.alongo.cryptonotcurrency

import android.content.Context
import androidx.security.app.authenticator.AppAuthenticator

object Cryptographer {
    fun verifyApp(context: Context, packageName: String): Boolean {
        val result = AppAuthenticator.createFromResource(context, R.xml.app_public_key)
            .checkAppIdentity(packageName)
        return when (result) {
            AppAuthenticator.SIGNATURE_MATCH -> {
                println("Application has a valid signature")
                true
            }

            AppAuthenticator.SIGNATURE_NO_MATCH -> {
                println("Application has invalid signature or is not installed")
                false
            }

            else -> {
                println("Unknown AppAuthenticator result")
                false
            }
        }
    }
}
