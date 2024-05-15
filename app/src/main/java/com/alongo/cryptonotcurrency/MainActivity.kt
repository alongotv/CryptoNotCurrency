package com.alongo.cryptonotcurrency

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.alongo.cryptonotcurrency.ui.theme.CryptoNotCurrencyTheme


class MainActivity : ComponentActivity() {
    private val kgps: KeyGenParameterSpec =
        KeyGenParameterSpec.Builder(
            masterKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(masterKeySizeBits)
            .build().apply { MasterKeys.getOrCreate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoNotCurrencyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    @Composable
    fun Content() {
        PackageIdentityValidator()
        FileEncrypter()
    }

    @Composable
    fun PackageIdentityValidator() {
        val ctx = LocalContext.current
        val appPackageName = remember {
            mutableStateOf("org.telegram.messenger")
        }

        val isAppValid = remember(appPackageName) {
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
        TextField(value = appPackageName.value, onValueChange = {
            appPackageName.value = it
        })

        Text(text = resultText)
        Column {
            Button(onClick = {
                isAppValid.value = Cryptographer.verifyApp(
                    context = ctx,
                    packageName = appPackageName.value
                )
            }) {
                Text(text = "verify app")
            }
        }
    }

    @Composable
    fun FileEncrypter() {
        Column(Modifier.padding(16.dp)) {
            val ctx = LocalContext.current
            val textToEncrypt = remember {
                mutableStateOf("Hello world!")
            }
            val encryptionResult = remember(textToEncrypt.value) {
                Cryptographer.encryptText(ctx, filesDir, textToEncrypt.value)
            }

            TextField(value = textToEncrypt.value, onValueChange = {
                textToEncrypt.value = it
            })

            Text(text = "Decrypted text: ${encryptionResult.first}")
            Text(text = "Encrypted text: ${encryptionResult.second}")
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
}

const val masterKeyAlias = "notcrypto"
const val masterKeySizeBits = 256