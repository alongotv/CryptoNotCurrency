package com.alongo.cryptonotcurrency

import android.content.Context
import androidx.security.app.authenticator.AppAuthenticator
import androidx.security.crypto.EncryptedFile
import java.io.File

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

    fun encryptText(context: Context, filesDir: File, text: String): Pair<String, String> {
        val file = File(filesDir, "encryptedFile")
        if (file.exists()) {
            file.delete()
        }

        val encryptedFile = EncryptedFile.Builder(
            file,
            context, masterKeyAlias, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val outputStream = encryptedFile.openFileOutput()

        outputStream.use {
            it.write(text.toByteArray())
        }

        val inputStream = encryptedFile.openFileInput()
        var decryptedText: String?
        inputStream.use {
            decryptedText = it.bufferedReader().readText()
        }
        return Pair(decryptedText.orEmpty(), file.readText())
    }
}
