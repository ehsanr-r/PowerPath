package com.erdevelopments.powerpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val safe = if (values.isEmpty()) listOf(0f) else values
    val maxV = safe.maxOrNull() ?: 1f
    val minV = safe.minOrNull() ?: 0f
    val range = (maxV - minV).let { if (it <= 0f) 1f else it }

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val w = size.width
        val h = size.height
        val n = safe.size

        fun x(i: Int): Float {
            if (n <= 1) return w / 2f
            return (i.toFloat() / (n - 1)) * w
        }

        fun y(v: Float): Float {
            val norm = (v - minV) / range
            return h - (norm * h)
        }

        // Draw line segments
        for (i in 0 until n - 1) {
            val p1 = Offset(x(i), y(safe[i]))
            val p2 = Offset(x(i + 1), y(safe[i + 1]))
            drawLine(
                color = Color.Black.copy(alpha = 0.35f),
                start = p1,
                end = p2,
                strokeWidth = 4f
            )
        }

        // Draw points
        for (i in 0 until n) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.55f),
                radius = 6f,
                center = Offset(x(i), y(safe[i]))
            )
        }
    }
}