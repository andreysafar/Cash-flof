package ru.cashflow.statement.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.absoluteValue

/** Денежный формат: $12 500 / −$3 400. */
fun money(v: Long): String {
    val sign = if (v < 0) "−" else ""
    val digits = v.absoluteValue.toString()
    val sb = StringBuilder()
    for ((i, c) in digits.withIndex()) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(' ')
        sb.append(c)
    }
    return "$sign\$$sb"
}

/**
 * Денежное значение с анимацией счётчика при изменении — даёт явную
 * визуальную обратную связь, что действие совершено.
 */
@Composable
fun AnimatedMoney(
    value: Long,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
) {
    // Анимируем в пределах Int, чтобы не переполниться на огромных суммах.
    val clamped = value.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
    val animated by animateIntAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 550, easing = LinearOutSlowInEasing),
        label = "money",
    )
    val shown = if (clamped.toLong() == value) animated.toLong() else value
    Text(
        money(shown),
        modifier = modifier,
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
    )
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: Long,
    strong: Boolean = false,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val rowMod = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clip(RoundedCornerShape(10.dp)) else Modifier)
    Row(
        rowMod.padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            money(value),
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            color = when {
                highlight && value >= 0 -> MaterialTheme.colorScheme.primary
                value < 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

/** Современная кнопка быстрого действия — крупная тач-цель, скруглённая. */
@Composable
fun ActionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Числовое поле ввода (целые, без знака), возвращает значение через onValue. */
@Composable
fun NumberField(
    label: String,
    initial: Long = 0,
    onValue: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(if (initial == 0L) "" else initial.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() }.take(12)
            text = filtered
            onValue(filtered.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth().padding(vertical = 3.dp),
    )
}

@Composable
fun TextInputField(
    label: String,
    initial: String = "",
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(initial) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onValue(it) },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 3.dp),
    )
}
