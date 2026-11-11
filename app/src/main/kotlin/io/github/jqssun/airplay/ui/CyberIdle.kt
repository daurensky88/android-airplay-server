package io.github.jqssun.airplay.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import io.github.jqssun.airplay.ui.theme.Geologica
import io.github.jqssun.airplay.ui.theme.LiluColors
import io.github.jqssun.airplay.ui.theme.LiluMono
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Капля цифрового дождя на фоне сцены. */
private data class RainDrop(val x: Float, val speed: Float, val len: Float)

/**
 * Экран ожидания: ноутбук отдаёт данные телевизору по воздуху.
 * Кубики-пакеты летят по дуге, у телевизора расходятся волны приёма.
 * Когда отправитель подключён, поток идёт гуще и быстрее.
 */
@Composable
fun CyberIdleScene(
    title: String,
    subtitle: String,
    active: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val drops = remember {
        val r = Random(17)
        List(26) { RainDrop(r.nextFloat(), 0.35f + r.nextFloat() * 0.8f, 0.08f + r.nextFloat() * 0.18f) }
    }
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            t = withFrameNanos { it }.let { now -> (now - start) / 1_000_000_000f }
        }
    }

    Box(modifier.background(LiluColors.BgDeep), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w < 2f || h < 2f) return@Canvas
            val k = min(w / 440f, h / 250f)          // сцена нарисована под 440x250
            val dim = if (dimmed) 0.35f else 1f
            val flow = if (active) 1.8f else 1f       // при трансляции поток гуще
            val count = if (active) 9 else 6
            val horizon = h * 0.60f

            // цифровой дождь
            drops.forEach { d ->
                val y = -h * 0.2f + (h * 1.4f) * ((t * 0.18f * d.speed) % 1f)
                drawLine(
                    LiluColors.RainGreen.copy(alpha = 0.16f * dim),
                    Offset(d.x * w, y), Offset(d.x * w, y + d.len * h), strokeWidth = 1.6f
                )
            }

            // неоновая сетка-пол
            for (i in 0..11) {
                val p = ((t * 0.12f + i / 11f) % 1f)
                val y = horizon + (h - horizon) * p * p
                drawLine(
                    LiluColors.Cyan.copy(alpha = (0.05f + 0.3f * p) * dim),
                    Offset(0f, y), Offset(w, y), strokeWidth = 0.6f + 1.6f * p
                )
            }
            for (i in -8..8) {
                drawLine(
                    LiluColors.Cyan.copy(alpha = 0.18f * dim),
                    Offset(w / 2 + i * 9f * k, horizon),
                    Offset(w / 2 + i * 74f * k, h), strokeWidth = 1f
                )
            }

            // устройства
            val lap = Rect(30f * k, 104f * k, 118f * k, 158f * k)
            val tv = Rect(w - 186f * k, 76f * k, w - 70f * k, 150f * k)
            glowPool(Offset(lap.center.x, lap.bottom + 18f * k), 62f * k, 13f * k, 0.5f * dim)
            glowPool(Offset(tv.center.x, tv.bottom + 26f * k), 80f * k, 16f * k, 0.55f * dim)
            screen(lap, k, dim, stand = false)
            screen(tv, k, dim, stand = true)

            // волны приёма справа от телевизора
            for (i in 0..2) {
                val p = ((t * 0.55f * flow + i / 3f) % 1f)
                val r = (16f + 50f * p) * k
                drawArc(
                    color = LiluColors.Cyan.copy(alpha = max(0f, 0.8f * (1f - p)) * dim),
                    startAngle = -62f, sweepAngle = 124f, useCenter = false,
                    topLeft = Offset(tv.right - r, tv.center.y - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 3.5f * k)
                )
            }

            // кубики данных по дуге
            val p0 = Offset(lap.right - 4f * k, lap.top + 8f * k)
            val pc = Offset((lap.right + tv.left) / 2f, 22f * k)
            val p2 = Offset(tv.left + 6f * k, tv.top + 26f * k)
            for (i in 0 until count) {
                val p = ((t * 0.32f * flow + i.toFloat() / count) % 1f)
                val pos = quad(p0, pc, p2, p)
                val lift = sin(p * PI).toFloat()
                glowPool(Offset(pos.x, h - 24f * k), (15f - 5f * lift) * k, 4.5f * k, 0.38f * lift * dim)
                cube(pos, 13f * k, p * 6.283f, (0.35f + 0.65f * lift) * dim)
            }
        }

        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                fontFamily = Geologica,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = LiluColors.Text,
                textAlign = TextAlign.Center,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    fontFamily = LiluMono,
                    fontSize = 13.sp,
                    color = LiluColors.TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private fun quad(a: Offset, b: Offset, c: Offset, t: Float): Offset {
    val u = 1 - t
    return Offset(
        u * u * a.x + 2 * u * t * b.x + t * t * c.x,
        u * u * a.y + 2 * u * t * b.y + t * t * c.y,
    )
}

/** Пятно света под объектом — в тёмной теме оно заменяет тень. */
private fun DrawScope.glowPool(center: Offset, rx: Float, ry: Float, alpha: Float) {
    if (alpha <= 0.01f) return
    for (i in 3 downTo 1) {
        val f = i / 3f
        drawOval(
            LiluColors.Magenta.copy(alpha = alpha * 0.16f / i),
            topLeft = Offset(center.x - rx * f, center.y - ry * f),
            size = Size(rx * 2 * f, ry * 2 * f),
        )
    }
}

/** Экран устройства: тёмный корпус, неоновый контур, светящаяся матрица. */
private fun DrawScope.screen(r: Rect, k: Float, dim: Float, stand: Boolean) {
    val radius = androidx.compose.ui.geometry.CornerRadius(10f * k, 10f * k)
    drawRoundRect(
        Color(0xFF120A26).copy(alpha = dim),
        topLeft = r.topLeft, size = r.size, cornerRadius = radius
    )
    drawRoundRect(
        LiluColors.Magenta.copy(alpha = 0.85f * dim),
        topLeft = r.topLeft, size = r.size, cornerRadius = radius,
        style = Stroke(width = 2f * k)
    )
    val inner = Rect(r.left + 8f * k, r.top + 8f * k, r.right - 8f * k, r.bottom - 8f * k)
    drawRoundRect(
        LiluColors.VioletBright.copy(alpha = 0.9f * dim),
        topLeft = inner.topLeft, size = inner.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * k, 6f * k)
    )
    // диагональный блик по стеклу
    val gloss = Path().apply {
        moveTo(inner.left, inner.top); lineTo(inner.right, inner.top)
        lineTo(inner.left, inner.bottom); close()
    }
    drawPath(gloss, Color.White.copy(alpha = 0.10f * dim))
    if (stand) {
        drawRoundRect(
            Color(0xFF2A1550).copy(alpha = dim),
            topLeft = Offset(r.center.x - 16f * k, r.bottom),
            size = Size(32f * k, 9f * k),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * k, 4f * k)
        )
        drawRoundRect(
            Color(0xFF1E0F3C).copy(alpha = dim),
            topLeft = Offset(r.center.x - 30f * k, r.bottom + 9f * k),
            size = Size(60f * k, 6f * k),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * k, 3f * k)
        )
    } else {
        val base = Path().apply {
            moveTo(r.left - 12f * k, r.bottom); lineTo(r.right + 12f * k, r.bottom)
            lineTo(r.right + 20f * k, r.bottom + 9f * k); lineTo(r.left - 20f * k, r.bottom + 9f * k)
            close()
        }
        drawPath(base, Color(0xFF2A1550).copy(alpha = dim))
    }
}

/** Изометрический кубик-пакет: три грани с разной подсветкой и бирюзовое ребро. */
private fun DrawScope.cube(c: Offset, s: Float, spin: Float, alpha: Float) {
    if (alpha <= 0.02f) return
    val kx = 0.5f + 0.2f * sin(spin)
    val hh = s * 0.58f
    val x = c.x; val y = c.y
    val topFace = Path().apply {
        moveTo(x, y - hh); lineTo(x + s * kx, y - hh + hh * kx)
        lineTo(x, y + hh * 0.12f); lineTo(x - s * (1 - kx), y - hh + hh * (1 - kx)); close()
    }
    val leftFace = Path().apply {
        moveTo(x - s * (1 - kx), y - hh + hh * (1 - kx)); lineTo(x, y + hh * 0.12f)
        lineTo(x, y + s * 0.95f); lineTo(x - s * (1 - kx), y + s * 0.95f - hh * (1 - kx)); close()
    }
    val rightFace = Path().apply {
        moveTo(x + s * kx, y - hh + hh * kx); lineTo(x, y + hh * 0.12f)
        lineTo(x, y + s * 0.95f); lineTo(x + s * kx, y + s * 0.95f - hh * kx); close()
    }
    drawPath(leftFace, Color(0xFF8A3EE0).copy(alpha = alpha))
    drawPath(rightFace, Color(0xFF5A1FA8).copy(alpha = alpha))
    drawPath(topFace, Color(0xFFE2B8FF).copy(alpha = alpha))
    drawPath(topFace, LiluColors.Cyan.copy(alpha = 0.75f * alpha), style = Stroke(width = max(1f, s * 0.07f)))
}
