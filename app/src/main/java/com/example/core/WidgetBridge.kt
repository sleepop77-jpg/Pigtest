package com.example.core

import kotlinx.coroutines.flow.MutableStateFlow

object WidgetBridge {
    val seconds = MutableStateFlow(25 * 60)
    val running = MutableStateFlow(false)
}

object NavBridge {
    val routeFlow = MutableStateFlow<String?>(null)
}
