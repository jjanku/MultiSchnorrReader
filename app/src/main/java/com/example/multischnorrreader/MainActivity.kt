package com.example.multischnorrreader

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.multischnorrreader.ui.MainScreen
import com.example.multischnorrreader.ui.theme.MultiSchnorrReaderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val signState by viewModel.signState.collectAsStateWithLifecycle()

            MultiSchnorrReaderTheme {
                MainScreen(
                    uiState = uiState,
                    signState = signState,
                    onProbChange = viewModel::onProbChange,
                    onPiggyChange = viewModel::onPiggyChange,
                    onMessageChange = viewModel::onMessageChange,
                    onReset = viewModel::onReset,
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(
            this,
            viewModel::onTagDiscovered,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B
                    or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }
}