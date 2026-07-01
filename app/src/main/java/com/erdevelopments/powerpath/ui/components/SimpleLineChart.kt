package com.erdevelopments.powerpath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    val safe = if (values.isEmpty()) listOf(0f) else values
    val maxV = safe.maxOrNull() ?: 1f
    val minV = safe.minOrNull() ?: 0f
    val range = (maxV - minV).let { if (it <= 0f) 1f else it }

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
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

        if (n > 1) {
            val fillPath = Path().apply {
                moveTo(x(0), h)
                lineTo(x(0), y(safe[0]))
                for (i in 1 until n) lineTo(x(i), y(safe[i]))
                lineTo(x(n - 1), h)
                close()
            }
            drawPath(fillPath, fillColor)
        }

        for (i in 0 until n - 1) {
            drawLine(
                color = lineColor,
                start = Offset(x(i), y(safe[i])),
                end = Offset(x(i + 1), y(safe[i + 1])),
                strokeWidth = 4f
            )
        }

        for (i in 0 until n) {
            drawCircle(color = dotColor, radius = 6f, center = Offset(x(i), y(safe[i])))
        }
    }
}
