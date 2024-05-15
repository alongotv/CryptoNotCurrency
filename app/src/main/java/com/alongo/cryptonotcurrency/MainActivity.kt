package com.alongo.cryptonotcurrency

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alongo.cryptonotcurrency.ui.theme.CryptoNotCurrencyTheme


class MainActivity : ComponentActivity() {

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
        SharedPrefsInteractor()
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
        Column(Modifier.padding(16.dp)) {
            TextField(value = appPackageName.value, onValueChange = {
                appPackageName.value = it
            }, Modifier.fillMaxWidth())

            Text(text = resultText)

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
            }, Modifier.fillMaxWidth())

            Text(text = "Decrypted text: ${encryptionResult.first}")
            Text(text = "Encrypted text: ${encryptionResult.second}")
        }
    }

    @Composable
    fun SharedPrefsInteractor() {
        val ctx = LocalContext.current

        val currentSharedPrefValue = remember {
            mutableIntStateOf(Cryptographer.getSharedPrefValue(ctx))
        }
        Column(Modifier.padding(16.dp)) {
            Text(text = "Current shared pref value: ${currentSharedPrefValue.intValue}")
            Button(onClick = {
                currentSharedPrefValue.intValue = Cryptographer.setSharedPrefValue(ctx)
            }, Modifier.padding(top = 8.dp)) {
                Text(text = "Update value")
            }
        }
    }
}
