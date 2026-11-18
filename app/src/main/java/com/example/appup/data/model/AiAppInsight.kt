package com.example.appup.data.model

data class AiAppInsight(
    val packageName: String,
    val appName: String,
    val overview: String,
    val updateAgeSignificance: String,
    val attentionAssessment: String,
    val recommendation: String,
    val suggestedAction: String,
    val generatedTimestamp: Long,
    val formattedTimestamp: String,
    val isCached: Boolean = false,
    val modelSource: String = "Gemini Flash (Firebase AI Logic)"
)
