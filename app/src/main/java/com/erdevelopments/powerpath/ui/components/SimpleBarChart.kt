package com.erdevelopments.powerpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBarChart(values: List<Float>, modifier: Modifier = Modifier) {
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(modifier = modifier.height(160.dp).fillMaxWidth()) {
        val safeCount = values.size.coerceAtLeast(1)
        val barWidth = size.width / (safeCount * 1.5f)
        val gap = barWidth / 2f

        values.forEachIndexed { i, v ->
            val h = (v / max) * size.height
            val left = gap + i * (barWidth + gap)
            drawRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(left, size.height - h),
                size = Size(barWidth, h)
            )
        }
    }
}