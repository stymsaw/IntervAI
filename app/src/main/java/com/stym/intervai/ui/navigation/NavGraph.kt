package com.stym.intervai.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stym.intervai.ui.components.GlobalErrorDialogContainer
import com.stym.intervai.ui.interview.InterviewViewModel
import com.stym.intervai.ui.screens.EvaluationReportScreen
import com.stym.intervai.ui.screens.LiveInterviewScreen
import com.stym.intervai.ui.screens.TopicSelectionScreen

sealed class Screen(val route: String) {
    object TopicSelection : Screen("topic_selection")
    object LiveInterview : Screen("live_interview")
    object EvaluationReport : Screen("evaluation_report")
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: InterviewViewModel = viewModel()
) {
    var lastReport by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.TopicSelection.route
        ) {
            composable(Screen.TopicSelection.route) {
                TopicSelectionScreen(
                    onTopicSelected = { topic ->
                        viewModel.startInterview(topic)
                        navController.navigate(Screen.LiveInterview.route)
                    }
                )
            }

            composable(Screen.LiveInterview.route) {
                LiveInterviewScreen(
                    viewModel = viewModel,
                    onInterviewFinished = { report ->
                        lastReport = report
                        navController.navigate(Screen.EvaluationReport.route) {
                            popUpTo(Screen.TopicSelection.route)
                        }
                    }
                )
            }

            composable(Screen.EvaluationReport.route) {
                EvaluationReportScreen(
                    reportText = lastReport,
                    onHomeClicked = {
                        navController.navigate(Screen.TopicSelection.route) {
                            popUpTo(Screen.TopicSelection.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        GlobalErrorDialogContainer()
    }
}
