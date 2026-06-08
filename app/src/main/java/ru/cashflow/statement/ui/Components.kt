package ru.cashflow.statement.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
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
 * Денежное значение с заметной анимацией при изменении: счётчик докручивается
 * и значение коротко «пульсирует» (увеличивается и пружинит назад) — явная
 * обратная связь, что действие совершено.
 */
@Composable
fun AnimatedMoney(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
) {
    val clamped = value.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
    val animated by animateIntAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "money",
    )
    val shown = if (clamped.toLong() == value) animated.toLong() else value

    var prev by remember { mutableStateOf(value) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(value) {
        if (value != prev) {
            prev = value
            scale.snapTo(1f)
            scale.animateTo(1.22f, tween(140, easing = FastOutSlowInEasing))
            scale.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow))
        }
    }
    Text(
        money(shown),
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
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

/** Описание кнопки быстрого действия для сетки. */
data class QuickAction(
    val label: String,
    val emphasized: Boolean = false,
    val onClick: () -> Unit,
)

/** Современная кнопка быстрого действия — крупная тач-цель (≥56dp), скруглённая. */
@Composable
fun ActionButton(
    label: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = if (emphasized) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors,
    ) {
        Text(
            label,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Сетка крупных кнопок действий: [columns] в ряд, равная ширина. */
@Composable
fun ActionGrid(actions: List<QuickAction>, columns: Int = 2) {
    actions.chunked(columns).forEach { rowItems ->
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowItems.forEach { a ->
                ActionButton(a.label, Modifier.weight(1f), a.emphasized, a.onClick)
            }
            repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/**
 * Крупное читаемое уведомление о совершённом действии: выезжает сверху,
 * держится пару секунд и уезжает. Высокий контраст (тёмная плашка), сумма
 * выделена цветом. Не перекрывается нижним рекламным баннером.
 */
@Composable
fun ActionToast(event: ActionEvent?, onShown: () -> Unit, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf<ActionEvent?>(null) }
    LaunchedEffect(event?.id) {
        val e = event ?: return@LaunchedEffect
        shown = e
        visible = true
        delay(2000)
        visible = false
        delay(250)
        onShown()
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        shown?.let { e ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shadowElevation = 10.dp,
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inversePrimary,
                    )
                    Column {
                        Text(
                            e.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (e.amount != 0L) {
                            Text(
                                (if (e.amount > 0) "+" else "") + money(e.amount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (e.amount < 0) Color(0xFFFFB4AB) else Color(0xFF7FF0C8),
                            )
                        }
                    }
                }
            }
        }
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
