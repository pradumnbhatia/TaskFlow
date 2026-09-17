package com.taskflow.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.taskflow.core.model.Priority

val Priority.indicatorColor: Color
    get() = when (this) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
    }
