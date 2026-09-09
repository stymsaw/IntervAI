package com.stym.intervai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.stym.intervai.data.AppLogger

abstract class BaseActivity : ComponentActivity() {

    protected open val activityTag: String
        get() = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUncaughtExceptionHandler()
    }

    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.logError(
                tag = activityTag,
                message = "Uncaught exception on thread: ${thread.name} in $activityTag",
                throwable = throwable
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
