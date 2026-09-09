package com.stym.intervai.data

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.PrintWriter
import java.io.StringWriter

data class AppErrorEvent(
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object AppLogger {

    private const val GLOBAL_TAG = "IntervAI_AppLogger"

    private val _errorFlow = MutableSharedFlow<AppErrorEvent>(extraBufferCapacity = 64)
    val errorFlow: SharedFlow<AppErrorEvent> = _errorFlow.asSharedFlow()

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        val formattedMessage = if (throwable != null) {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            "$message\nStackTrace:\n$sw"
        } else {
            message
        }

        Log.e(tag, formattedMessage)
        Log.e(GLOBAL_TAG, "[$tag] $formattedMessage")

        val event = AppErrorEvent(tag = tag, message = message, throwable = throwable)
        _errorFlow.tryEmit(event)
    }

    inline fun <T> runCatchingCentralized(
        tag: String,
        actionMessage: String,
        block: () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (t: Throwable) {
            logError(tag, actionMessage, t)
            Result.failure(t)
        }
    }
}
