package com.erdevelopments.powerpath.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class WorkoutBarItem(
    val dayLabel: String,
    val volume: Float,
    val weightKg: Float,
    val sets: Int,
    val reps: Int
)

@Composable
fun WorkoutProgressBarChart(
    items: List<WorkoutBarItem>,
    modifier: Modifier = Modifier,
    yAxisTicks: Int = 4
) {
    val density = LocalDensity.current
    val labelSizePx = with(density) { 11.sp.toPx() }
    val insideSizePx = with(density) { 10.sp.toPx() }

    val axisColor = Color.Black.copy(alpha = 0.35f)
    val barColor = Color.Black.copy(alpha = 0.25f)
    val textColor = Color.Black.copy(alpha = 0.75f)

    val maxValue = (items.maxOfOrNull { it.volume } ?: 0f).coerceAtLeast(1f)

    val leftPad = with(density) { 54.dp.toPx() }   // space for y labels
    val bottomPad = with(density) { 26.dp.toPx() } // x labels
    val topPad = with(density) { 10.dp.toPx() }
    val rightPad = with(density) { 10.dp.toPx() }

    val yLabelPaint = Paint().apply {
        isAntiAlias = true
        color = textColor.toArgb()
        textSize = labelSizePx
    }
    val xLabelPaint = Paint().apply {
        isAntiAlias = true
        color = textColor.toArgb()
        textSize = labelSizePx
        textAlign = Paint.Align.CENTER
    }
    val insidePaint = Paint().apply {
        isAntiAlias = true
        color = textColor.toArgb()
        textSize = insideSizePx
        textAlign = Paint.Align.CENTER
    }

    Canvas(modifier = modifier.fillMaxWidth().height(230.dp)) {
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val origin = Offset(leftPad, topPad + chartH)

        // Axes
        drawLine(axisColor, start = origin, end = Offset(origin.x + chartW, origin.y), strokeWidth = 2f)
        drawLine(axisColor, start = origin, end = Offset(origin.x, origin.y - chartH), strokeWidth = 2f)

        // Y ticks + labels
        val ticks = max(1, yAxisTicks)
        for (i in 0..ticks) {
            val t = i.toFloat() / ticks
            val y = origin.y - t * chartH
            val v = t * maxValue

            drawLine(axisColor, start = Offset(origin.x - 6f, y), end = Offset(origin.x, y), strokeWidth = 2f)

            drawContext.canvas.nativeCanvas.drawText(
                "%,.0f".format(v),
                2f,
                y + (labelSizePx / 3f),
                yLabelPaint
            )
        }

        if (items.isEmpty()) return@Canvas

        val n = items.size
        val gap = (chartW * 0.10f) / (n + 1)        // adaptive spacing
        val barW = (chartW - gap * (n + 1)) / n

        items.forEachIndexed { idx, item ->
            val x = origin.x + gap + idx * (barW + gap)
            val h = (item.volume / maxValue) * chartH

            val topLeft = Offset(x, origin.y - h)
            drawRect(barColor, topLeft, Size(barW, h))

            // X label
            drawContext.canvas.nativeCanvas.drawText(
                item.dayLabel,
                x + barW / 2f,
                origin.y + labelSizePx + 10f,
                xLabelPaint
            )

            // Inside labels
            val lines = listOf(
                "${item.weightKg}kg",
                "S:${item.sets} R:${item.reps}"
            )

            val minInside = insideSizePx * (lines.size + 0.8f)
            val drawInside = h >= minInside

            val baseY = if (drawInside) {
                topLeft.y + h / 2f - (lines.size - 1) * insideSizePx * 0.6f
            } else {
                topLeft.y - 8f - (lines.size - 1) * insideSizePx * 1.2f
            }

            lines.forEachIndexed { li, line ->
                drawContext.canvas.nativeCanvas.drawText(
                    line,
                    x + barW / 2f,
                    baseY + li * insideSizePx * 1.25f,
                    insidePaint
                )
            }
        }
    }
}