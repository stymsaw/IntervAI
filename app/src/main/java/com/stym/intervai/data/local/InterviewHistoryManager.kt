package com.stym.intervai.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stym.intervai.data.AppLogger

data class InterviewReportEntity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val topicName: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val reportText: String
)

class InterviewHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("intervai_history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyHistory = "saved_interview_history"
    private val tag = "InterviewHistoryManager"

    fun saveReport(topicName: String, reportText: String) {
        AppLogger.runCatchingCentralized(
            tag = tag,
            actionMessage = "Failed to save interview report to SharedPreferences"
        ) {
            val currentList = getReports().toMutableList()
            val newReport = InterviewReportEntity(
                topicName = topicName,
                reportText = reportText
            )
            currentList.add(0, newReport)
            
            val json = gson.toJson(currentList)
            prefs.edit().putString(keyHistory, json).apply()
        }
    }

    fun getReports(): List<InterviewReportEntity> {
        return try {
            val json = prefs.getString(keyHistory, null) ?: return emptyList()
            val type = object : TypeToken<List<InterviewReportEntity>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            AppLogger.logError(tag, "Error reading interview history", e)
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(keyHistory).apply()
    }
}
