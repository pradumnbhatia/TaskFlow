package com.taskflow.feature.profile

import com.taskflow.core.domain.ProductivityStats

sealed interface ProfileUiState {

    data object Loading : ProfileUiState

    data class Loaded(val stats: ProductivityStats) : ProfileUiState
}
