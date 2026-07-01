package com.erdevelopments.powerpath.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
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
    yAxisTicks: Int = 4,
    onBarClick: ((WorkoutBarItem) -> Unit)? = null
) {
    val density = LocalDensity.current
    val labelSizePx = with(density) { 11.sp.toPx() }
    val insideSizePx = with(density) { 10.sp.toPx() }

    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val barColor = MaterialTheme.colorScheme.primary
    val barColorAlpha = barColor.copy(alpha = 0.75f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val subtleTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxValue = (items.maxOfOrNull { it.volume } ?: 0f).coerceAtLeast(1f)

    val leftPad = with(density) { 54.dp.toPx() }
    val bottomPad = with(density) { 26.dp.toPx() }
    val topPad = with(density) { 10.dp.toPx() }
    val rightPad = with(density) { 10.dp.toPx() }

    val yLabelPaint = Paint().apply {
        isAntiAlias = true
        color = subtleTextColor.toArgb()
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

    val clickableModifier =
        if (onBarClick == null) modifier
        else modifier.pointerInput(items, onBarClick) {
            detectTapGestures { tap ->
                if (items.isEmpty()) return@detectTapGestures

                val chartW = size.width - leftPad - rightPad
                val chartH = size.height - topPad - bottomPad
                val originX = leftPad
                val originY = topPad + chartH

                if (tap.x < originX || tap.x > originX + chartW) return@detectTapGestures
                if (tap.y < topPad || tap.y > originY) return@detectTapGestures

                val n = items.size
                val gap = (chartW * 0.10f) / (n + 1)
                val barW = (chartW - gap * (n + 1)) / n

                for (idx in 0 until n) {
                    val barLeft = originX + gap + idx * (barW + gap)
                    val barRight = barLeft + barW

                    if (tap.x in barLeft..barRight) {
                        onBarClick(items[idx])
                        return@detectTapGestures
                    }
                }
            }
        }

    Canvas(modifier = clickableModifier.fillMaxWidth().height(230.dp)) {
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val origin = Offset(leftPad, topPad + chartH)

        drawLine(axisColor, start = origin, end = Offset(origin.x + chartW, origin.y), strokeWidth = 2f)
        drawLine(axisColor, start = origin, end = Offset(origin.x, origin.y - chartH), strokeWidth = 2f)

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
        val gap = (chartW * 0.10f) / (n + 1)
        val barW = (chartW - gap * (n + 1)) / n
        val cornerRadius = CornerRadius(barW / 4f)

        items.forEachIndexed { idx, item ->
            val x = origin.x + gap + idx * (barW + gap)
            val h = (item.volume / maxValue) * chartH

            val topLeft = Offset(x, origin.y - h)
            drawRoundRect(barColorAlpha, topLeft, Size(barW, h), cornerRadius)

            drawContext.canvas.nativeCanvas.drawText(
                item.dayLabel,
                x + barW / 2f,
                origin.y + labelSizePx + 10f,
                xLabelPaint
            )

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
