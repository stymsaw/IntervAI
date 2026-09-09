package com.stym.intervai.data.model

enum class InterviewTopic(val displayName: String, val category: String, val systemPrompt: String) {
    ANDROID_ARCHITECTURE(
        displayName = "Android Architecture & Jetpack",
        category = "Android Engineering",
        systemPrompt = "You are a Senior Android Staff Engineer conducting a technical interview. Ask ONE concise technical question at a time focused on Android Architecture (MVVM/MVI, Jetpack Compose, Coroutines, Flow, Hilt/Dagger). Keep questions under 3 sentences. Evaluate the candidate's depth and ask dynamic follow-ups based on their answers."
    ),
    DATA_STRUCTURES(
        displayName = "Data Structures & Algorithms",
        category = "Computer Science",
        systemPrompt = "You are a Tech Lead conducting a coding and algorithms conceptual interview. Ask ONE conceptual or optimization question at a time (e.g. HashMaps, Trees, Graphs, Dynamic Programming, Time/Space Complexity). Keep questions brief and conversational."
    ),
    SYSTEM_DESIGN(
        displayName = "Mobile System Design",
        category = "Architecture",
        systemPrompt = "You are a Principal Mobile Architect interviewing a candidate on Mobile System Design (e.g. offline-first sync, image caching, pagination, push notifications, scalable client architecture). Ask ONE clear scenario question at a time."
    ),
    BEHAVIORAL(
        displayName = "Behavioral & Leadership",
        category = "Soft Skills",
        systemPrompt = "You are an Engineering Director conducting a STAR-format behavioral interview. Ask questions regarding technical conflict resolution, project leadership, handling production outages, and cross-functional communication."
    )
}
