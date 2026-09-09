package com.stym.intervai.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.stym.intervai.data.AppErrorEvent
import com.stym.intervai.data.AppLogger
import kotlinx.coroutines.flow.collectLatest
import java.io.PrintWriter
import java.io.StringWriter

@Composable
fun GlobalErrorDialogContainer() {
    var activeErrorEvent by remember { mutableStateOf<AppErrorEvent?>(null) }

    LaunchedEffect(Unit) {
        AppLogger.errorFlow.collectLatest { event ->
            activeErrorEvent = event
        }
    }

    activeErrorEvent?.let { errorEvent ->
        GlobalErrorDialog(
            errorEvent = errorEvent,
            onDismiss = { activeErrorEvent = null }
        )
    }
}

@Composable
fun GlobalErrorDialog(
    errorEvent: AppErrorEvent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.40f).dp

    val stackTraceString = remember(errorEvent.throwable) {
        errorEvent.throwable?.let { throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            sw.toString()
        } ?: ""
    }

    val fullCopyText = remember(errorEvent, stackTraceString) {
        buildString {
            append("Tag: ").append(errorEvent.tag).append("\n")
            append("Message: ").append(errorEvent.message).append("\n")
            if (stackTraceString.isNotBlank()) {
                append("StackTrace:\n").append(stackTraceString)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .height(maxDialogHeight)
                .zIndex(Float.MAX_VALUE)
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Title & Subtitle)
                Column {
                    Text(
                        text = "⚠️ Application Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Source: ${errorEvent.tag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Error Details & StackTrace Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = errorEvent.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (stackTraceString.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stackTraceString,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons (Copy & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("IntervAI Error Log", fullCopyText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Error details copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("📋 Copy")
                    }

                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
