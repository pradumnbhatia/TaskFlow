package com.taskflow.core.common

import com.taskflow.core.model.Priority

val Priority.label: String
    get() = name.lowercase().replaceFirstChar(Char::uppercase)
