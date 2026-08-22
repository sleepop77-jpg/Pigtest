package com.example.core

import kotlinx.coroutines.flow.MutableStateFlow

object WidgetBridge {
    val seconds = MutableStateFlow(25 * 60)
    val running = MutableStateFlow(false)
    val frame = MutableStateFlow(0)
    val streak = MutableStateFlow(4)
}

object NavBridge {
    val routeFlow = MutableStateFlow<String?>(null)
}
