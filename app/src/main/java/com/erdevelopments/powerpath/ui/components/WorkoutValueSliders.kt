package com.erdevelopments.powerpath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

const val MAX_SETS = 5
const val MAX_REPS = 30
const val MAX_WEIGHT_KG = 90f
private val StepperButtonWidth = 74.dp
private val StepperButtonHeight = 46.dp

@Composable
fun IntValueSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    valueSuffix: String = ""
) {
    val start = valueRange.first
    val endInclusive = valueRange.last
    var text by remember { mutableStateOf(value.coerceIn(start, endInclusive).toString()) }

    LaunchedEffect(value, start, endInclusive) {
        text = value.coerceIn(start, endInclusive).toString()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    onValueChange((value - 1).coerceIn(start, endInclusive))
                },
                enabled = value > start,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                modifier = Modifier
                    .width(StepperButtonWidth)
                    .height(StepperButtonHeight)
            ) {
                Text("-")
            }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toIntOrNull()?.let { parsed ->
                        onValueChange(parsed.coerceIn(start, endInclusive))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                suffix = if (valueSuffix.isNotBlank()) {
                    {
                        Text(
                            text = valueSuffix,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = {
                    onValueChange((value + 1).coerceIn(start, endInclusive))
                },
                enabled = value < endInclusive,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                modifier = Modifier
                    .width(StepperButtonWidth)
                    .height(StepperButtonHeight)
            ) {
                Text("+")
            }
        }
    }
}

@Composable
fun FloatValueSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    stepSize: Float,
    valueSuffix: String = ""
) {
    val start = valueRange.start
    val endInclusive = valueRange.endInclusive
    var text by remember { mutableStateOf(formatFloatValue(value.coerceIn(start, endInclusive))) }

    LaunchedEffect(value, start, endInclusive) {
        text = formatFloatValue(value.coerceIn(start, endInclusive))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    onValueChange((value - stepSize).snapToStep(start, stepSize, endInclusive))
                },
                enabled = value > start,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                modifier = Modifier
                    .width(StepperButtonWidth)
                    .height(StepperButtonHeight)
            ) {
                Text("-")
            }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toFloatOrNull()?.let { parsed ->
                        onValueChange(parsed.snapToStep(start, stepSize, endInclusive))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                suffix = if (valueSuffix.isNotBlank()) {
                    {
                        Text(
                            text = valueSuffix,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = {
                    onValueChange((value + stepSize).snapToStep(start, stepSize, endInclusive))
                },
                enabled = value < endInclusive,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                modifier = Modifier
                    .width(StepperButtonWidth)
                    .height(StepperButtonHeight)
            ) {
                Text("+")
            }
        }
    }
}

private fun Float.snapToStep(start: Float, stepSize: Float, endInclusive: Float): Float {
    val steppedValue = ((this - start) / stepSize).roundToInt() * stepSize + start
    return steppedValue.coerceIn(start, endInclusive)
}

private fun formatFloatValue(value: Float): String {
    return if (value.roundToInt().toFloat() == value) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
