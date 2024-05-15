package com.alongo.cryptonotcurrency

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.app.authenticator.AppAuthenticator
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File

object Cryptographer {
    init {
        KeyGenParameterSpec.Builder(
            masterKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(masterKeySizeBits)
            .build().apply { MasterKeys.getOrCreate(this) }
    }

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

    fun setSharedPrefValue(context: Context): Int {
        val sharedPreferences = getSharedPrefs(context)
        val result = sharedPreferences.getInt(SHARED_PREFERENCES_INT_KEY, 0)
        val newInt = result + 1
        sharedPreferences.edit().putInt(SHARED_PREFERENCES_INT_KEY, newInt).apply()
        return newInt
    }

    fun getSharedPrefValue(context: Context): Int {
        val sharedPreferences = getSharedPrefs(context)
        return sharedPreferences.getInt(SHARED_PREFERENCES_INT_KEY, 0)
    }

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            "shared_pref_file_encrypted",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}


private const val SHARED_PREFERENCES_INT_KEY = "example_int"
const val masterKeyAlias = "notcrypto"
const val masterKeySizeBits = 256
