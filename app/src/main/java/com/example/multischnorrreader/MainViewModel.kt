package com.example.multischnorrreader

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.multischnorrreader.card.AppletCosigner
import com.example.multischnorrreader.card.NfcAppletCosigner
import com.example.multischnorrreader.crypto.PhoneCosigner
import com.example.multischnorrreader.crypto.SignatureManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainViewModel : ViewModel() {
    private val signatureManager = SignatureManager(PhoneCosigner())

    private val _uiState = MutableStateFlow(
        UiState(prob = false, piggy = false, message = "")
    )
    val uiState = _uiState.asStateFlow()
    private val _signState = MutableStateFlow<SignState>(SignState.Ready)
    val signState = _signState.asStateFlow()

    fun onProbChange() {
        _uiState.update { it.copy(prob = !it.prob) }
    }

    fun onPiggyChange() {
        _uiState.update { it.copy(piggy = !it.piggy) }
    }

    fun onMessageChange(value: String) {
        val trimmed = value.filter { char -> char.code < 128 }.take(AppletCosigner.MSG_LEN)
        _uiState.update { it.copy(message = trimmed) }
    }

    fun onReset() {
        signatureManager.reset()
        _signState.value = SignState.Ready
    }

    fun onTagDiscovered(tag: Tag) = viewModelScope.launch {
        val (prob, piggy, message) = uiState.value
        val bytes = message.toByteArray().copyOf(AppletCosigner.MSG_LEN)

        val appletCosigner = NfcAppletCosigner(tag)
        try {
            appletCosigner.init()

            _signState.value = SignState.Grouping
            val group = signatureManager.group(appletCosigner)

            _signState.value = SignState.Signing(attempt = 1)
            val startTime = System.currentTimeMillis()
            signatureManager.sign(appletCosigner, bytes, prob, piggy)
                .collectIndexed { index, signature ->
                    _signState.value = if (signature != null) {
                        val duration = (System.currentTimeMillis() - startTime).milliseconds
                        SignState.Success(
                            group,
                            bytes,
                            signature,
                            duration,
                        )
                    } else {
                        SignState.Signing(attempt = index + 2)
                    }
                }
        } catch (e: Exception) {
            _signState.value = SignState.Error(e.message ?: "Unknown error")
        } finally {
            appletCosigner.finish()
        }
    }
}