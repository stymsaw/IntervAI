package com.stym.intervai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stym.intervai.ui.BaseActivity
import com.stym.intervai.ui.navigation.AppNavGraph
import com.stym.intervai.ui.theme.IntervAITheme

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntervAITheme {
                AppNavGraph()
            }
        }
    }
}