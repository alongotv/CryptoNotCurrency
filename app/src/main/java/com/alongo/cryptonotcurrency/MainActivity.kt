package com.alongo.cryptonotcurrency

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.alongo.cryptonotcurrency.ui.theme.CryptoNotCurrencyTheme
import java.io.File


class MainActivity : ComponentActivity() {
    private val kgps: KeyGenParameterSpec =
        KeyGenParameterSpec.Builder(
            masterKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

    private val masterKey = MasterKeys.getOrCreate(kgps)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoNotCurrencyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Content()
                    }
                }
                LaunchedEffect(Unit) {
                    test()
                }
            }
        }
    }

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        PackageIdentityValidator()
    }

    @Composable
    fun PackageIdentityValidator() {
        val ctx = LocalContext.current
        val isAppValid = remember {
            mutableStateOf<Boolean?>(null)
        }
        val resultText = when (isAppValid.value) {
            true -> {
                "The application has a valid signature"
            }

            false -> {
                "The application has invalid signature or is not installed"
            }

            else -> {
                ""
            }
        }
        Text(text = resultText)
        Column {
            Button(onClick = {
                isAppValid.value = Cryptographer.verifyApp(
                    context = ctx,
                    packageName = "org.telegram.messenger"
                )
            }) {
                Text(text = "verify app")
            }
        }
    }

    fun enc() {
        val sharedPreferences = EncryptedSharedPreferences.create(
            "shared_pref_file_encrypted",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().putInt("example_int", 2).apply()

    }

    fun test() {
        val file = File(filesDir, "encryptedFile")
        if (file.exists()) {
            file.delete()
        }

        val encryptedFile = EncryptedFile.Builder(
            file,
            this, masterKeyAlias, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val outputStream = encryptedFile.openFileOutput()

        outputStream.use {
            it.write("hello world".toByteArray())
        }

        val text = file.readText()
        println(text)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CryptoNotCurrencyTheme {
        Greeting("Android")
    }
}

const val masterKeyAlias = "notcrypto"
