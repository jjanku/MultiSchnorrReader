package com.example.multischnorrreader.ui

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.multischnorrreader.SignState
import com.example.multischnorrreader.UiState
import com.example.multischnorrreader.card.IsoException
import com.example.multischnorrreader.crypto.SchemeParameters
import com.example.multischnorrreader.crypto.Signature
import com.example.multischnorrreader.ui.theme.MultiSchnorrReaderTheme
import com.example.multischnorrreader.ui.theme.onSuccessContainer
import com.example.multischnorrreader.ui.theme.successContainer
import com.example.multischnorrreader.util.toJson
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: UiState,
    signState: SignState,
    onProbChange: () -> Unit,
    onPiggyChange: () -> Unit,
    onMessageChange: (String) -> Unit,
    onReset: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }
    if (showResetDialog) {
        ResetDialog(
            onReset = {
                showResetDialog = false
                onReset()
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MultiSchnorrReader") },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                ControlRow(
                    probSelected = uiState.prob,
                    onProbChange = onProbChange,
                    piggySelected = uiState.piggy,
                    onPiggyChange = onPiggyChange,
                    modifier = Modifier.padding(8.dp),
                )
                MessageField(
                    message = uiState.message,
                    onMessageChange = onMessageChange,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    ) { padding ->
        MainContent(
            signState,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun MainContent(
    state: SignState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusIndicator(state)
        Spacer(modifier = Modifier.height(16.dp))
        StatusDescription(state)
        if (state is SignState.Success) {
            Spacer(modifier = Modifier.height(16.dp))
            SignatureCard(state)
        }
    }
}

@Composable
fun StatusIndicator(
    state: SignState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val color = when (state) {
        is SignState.Error -> colors.errorContainer
        is SignState.Success -> colors.successContainer
        else -> colors.primaryContainer
    }
    val contentColor = if (color == colors.successContainer)
        colors.onSuccessContainer else colors.contentColorFor(color)

    val icons = Icons.Filled
    val imageVector = when (state) {
        is SignState.Error -> icons.Warning
        is SignState.Success -> icons.Done
        else -> icons.CreditCard
    }

    val working = (state is SignState.Grouping)
            || (state is SignState.Signing)

    Surface(
        modifier = modifier.size(96.dp),
        color = color,
        contentColor = contentColor,
        shape = CircleShape,
    ) {
        if (working) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
            )
        }
        Icon(
            imageVector,
            contentDescription = null,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
fun StatusDescription(
    state: SignState,
    modifier: Modifier = Modifier,
) {
    val text = when (state) {
        is SignState.Error -> "Error"
        SignState.Grouping -> "Establishing group"
        SignState.Ready -> "Hold a card near the reader"
        is SignState.Signing -> "Signing"
        is SignState.Success -> "Success"
    }
    val description = when (state) {
        is SignState.Error -> state.msg
        is SignState.Signing ->
            if (state.attempt > 1) "Attempt #${state.attempt}" else null
        is SignState.Success ->
            "Finished in ${state.duration.toString(DurationUnit.MILLISECONDS)}"
        else -> null
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge)
        if (description != null)
            Text(description)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureCard(
    state: SignState.Success,
    modifier: Modifier = Modifier
) {
    val (nonce, value) = state.signature

    val context = LocalContext.current

    OutlinedCard(
        onClick = {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, state.toJson())
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            ContextCompat.startActivity(context, shareIntent, null)
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Signature",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "nonce",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "$nonce",
                fontFamily = FontFamily.Monospace,
            )
            Text(
                "value",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "$value",
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlRow(
    probSelected: Boolean,
    onProbChange: () -> Unit,
    piggySelected: Boolean,
    onPiggyChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier
            .height(32.dp)
            .horizontalScroll(scrollState)
    ) {
        FilterChip(
            selected = probSelected,
            onClick = onProbChange,
            label = { Text("Probabilistic") },
            leadingIcon = if (probSelected) {
                { Icon(Icons.Filled.Done, "") }
            } else null
        )
        Spacer(modifier = Modifier.width(16.dp))
        FilterChip(
            selected = piggySelected,
            onClick = onPiggyChange,
            label = { Text("Piggyback") },
            leadingIcon = if (piggySelected) {
                { Icon(Icons.Filled.Done, "") }
            } else null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageField(
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = message,
        onValueChange = onMessageChange,
        placeholder = { Text("Enter a message") },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun ResetDialog(
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Filled.RestartAlt, contentDescription = null)
        },
        title = {
            Text("Reset state?")
        },
        text = {
            Text(
                "Any established group and cached nonce will be reset. "
                        + "They can be regenerated during the next signature attempt, "
                        + "which will thus take more time."
            )
        },
        confirmButton = {
            TextButton(onClick = onReset) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

class StatePreviewParameterProvider : PreviewParameterProvider<SignState> {
    override val values = sequenceOf(
        SignState.Ready,
        SignState.Grouping,
        SignState.Signing(attempt = 2),
        SignState.Success(
            group = SchemeParameters.ec.g,
            message = byteArrayOf(),
            signature = Signature(SchemeParameters.ec.g, SchemeParameters.ec.n),
            duration = 1700.milliseconds,
        ),
        SignState.Error(msg = IsoException(status = 0xbad0).message ?: "")
    )
}

@Preview(name = "light mode")
@Preview(name = "dark mode", uiMode = UI_MODE_NIGHT_YES)
annotation class ModePreviews

@ModePreviews
@Composable
fun MainContentPreview(
    @PreviewParameter(StatePreviewParameterProvider::class)
    state: SignState,
) {
    MultiSchnorrReaderTheme {
        Surface {
            MainContent(state)
        }
    }
}

@Preview
@Composable
fun ResetDialogPreview() {
    ResetDialog(onReset = {}, onDismiss = {})
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    var uiState by remember {
        mutableStateOf(UiState(prob = true, piggy = false, message = ""))
    }
    MainScreen(
        uiState = uiState,
        signState = SignState.Ready,
        onProbChange = { uiState = uiState.copy(prob = !uiState.prob) },
        onPiggyChange = {},
        onMessageChange = {},
        onReset = {},
    )
}
