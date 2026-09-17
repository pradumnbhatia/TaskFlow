package com.taskflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taskflow.core.common.label
import com.taskflow.core.designsystem.theme.indicatorColor
import com.taskflow.core.model.Priority

@Composable
fun PrioritySelector(
    selected: Priority,
    onSelected: (Priority) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Priority.entries.forEach { priority ->
            FilterChip(
                selected = selected == priority,
                onClick = { onSelected(priority) },
                label = { Text(priority.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = priority.indicatorColor,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}
