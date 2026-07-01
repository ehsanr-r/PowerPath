package com.erdevelopments.powerpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBarChart(values: List<Float>, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.primary
    val barBg = MaterialTheme.colorScheme.surfaceVariant

    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Canvas(modifier = modifier.height(180.dp).fillMaxWidth()) {
        val safeCount = values.size.coerceAtLeast(1)
        val barWidth = size.width / (safeCount * 1.5f)
        val gap = barWidth / 2f
        val cornerRadius = CornerRadius(barWidth / 4f)

        values.forEachIndexed { i, v ->
            val left = gap + i * (barWidth + gap)

            drawRoundRect(
                color = barBg,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = cornerRadius
            )

            val h = (v / max) * size.height
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, size.height - h),
                size = Size(barWidth, h),
                cornerRadius = cornerRadius
            )
        }
    }
}
