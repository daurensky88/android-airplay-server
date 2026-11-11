package io.github.jqssun.airplay.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.theme.Geologica
import io.github.jqssun.airplay.ui.theme.LiluColors
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.withFrameNanos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.min
import kotlin.random.Random

private const val DURATION = 2.6f

private fun easeOut(x: Float) = 1f - (1f - x) * (1f - x) * (1f - x)

/** Капля цифрового дождя: позиция по ширине, множитель скорости, длина. */
private data class Drop(val x: Float, val speed: Float, val len: Float)

/**
 * Стартовая заставка: 2.6 секунды, синхронно со звуковым акцентом.
 * Кадры считаются на лету, поэтому на 4K остаётся чётко.
 */
@Composable
fun SplashScreen(playSound: Boolean, onFinished: () -> Unit) {
    val ctx = LocalContext.current
    val logo = ImageBitmap.imageResource(R.drawable.splash_logo)
    val logoSoft = ImageBitmap.imageResource(R.drawable.splash_logo_soft)
    val drops = remember {
        val r = Random(5)
        List(70) { Drop(r.nextFloat(), 0.4f + r.nextFloat() * 0.6f, 0.12f + r.nextFloat() * 0.26f) }
    }
    var t by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (t < DURATION) {
            t = withFrameNanos { it }.let { now -> (now - start) / 1_000_000_000f }
        }
        onFinished()
    }

    if (playSound) {
        DisposableEffect(Unit) {
            val mp = MediaPlayer.create(ctx, R.raw.startup)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                start()
            }
            onDispose { runCatching { mp?.release() } }
        }
    }

    Box(
        Modifier.fillMaxSize().background(LiluColors.BgDeep),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 0.0–0.35: бирюзовая черта раскрывается из центра
            if (t < 0.5f) {
                val p = easeOut(min(1f, t / 0.35f))
                val half = w * 0.46f * p
                drawRect(
                    LiluColors.Cyan,
                    topLeft = Offset(w / 2 - half, h / 2 - 1.5f),
                    size = Size(half * 2, 3f)
                )
            }

            // 0.28–1.35: цифровой дождь
            if (t > 0.28f && t < 1.35f) {
                val p = (t - 0.28f) / 1.07f
                drops.forEach { d ->
                    val y = -h * 0.25f + (h * 1.5f) * ((p * d.speed * 1.9f) % 1f)
                    drawLine(
                        LiluColors.RainGreen.copy(alpha = 0.55f),
                        Offset(d.x * w, y), Offset(d.x * w, y + d.len * h), strokeWidth = 2f
                    )
                }
            }

            // 0.35+: неоновая сетка уходит к горизонту
            if (t > 0.35f) {
                val p = min(1f, (t - 0.35f) / 0.8f)
                val horizon = h * 0.62f
                for (k in 0..9) {
                    val yy = horizon + (h - horizon) * pow18(k / 9f)
                    drawLine(
                        LiluColors.Cyan.copy(alpha = 0.26f * p),
                        Offset(0f, yy), Offset(w, yy), strokeWidth = 1.4f
                    )
                }
                for (k in -7..7) {
                    drawLine(
                        LiluColors.Cyan.copy(alpha = 0.20f * p),
                        Offset(w / 2 + k * w * 0.016f, horizon),
                        Offset(w / 2 + k * w * 0.13f, h), strokeWidth = 1.2f
                    )
                }
            }

            // 1.45–2.1: ударное кольцо
            if (t > 1.45f && t < 2.1f) {
                val p = (t - 1.45f) / 0.65f
                val r = w * 0.05f + w * 0.52f * easeOut(p)
                drawOval(
                    LiluColors.Cyan.copy(alpha = 0.85f * (1f - p)),
                    topLeft = Offset(w / 2 - r, h * 0.40f - r * 0.38f),
                    size = Size(r * 2, r * 0.76f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
            }

            // 0.95+: логотип влетает, уменьшаясь, со шлейфом и вспышкой
            if (t > 0.95f) {
                val p = min(1f, (t - 0.95f) / 0.55f)
                val e = easeOut(p)
                val side = (h * 1.30f - h * 0.69f * e)
                val cy = h * 0.40f
                if (p < 1f) addImage(logoSoft, side * 1.12f, w / 2, cy, 0.35f * (1f - p))
                addImage(logo, side, w / 2, cy, min(1f, p * 2f))
                if (t > 1.45f && t < 1.75f) {
                    addImage(logoSoft, side * 1.35f, w / 2, cy, 0.55f * (1f - (t - 1.45f) / 0.3f))
                }
            }

            // скан-линии
            var y = 0f
            while (y < h) {
                drawLine(Color.Black.copy(alpha = 0.24f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += 4f
            }

            // финальное затемнение
            if (t > 2.35f) {
                drawRect(LiluColors.BgDeep.copy(alpha = min(1f, (t - 2.35f) / 0.25f)))
            }
        }

        if (t > 1.55f) {
            val p = min(1f, (t - 1.55f) / 0.5f)
            val full = "LILU AIR"
            val shown = full.take(max(1, (full.length * min(1f, p * 1.3f)).toInt()))
            val fade = if (t > 2.35f) 1f - min(1f, (t - 2.35f) / 0.25f) else 1f
            Column(
                Modifier.fillMaxSize().padding(bottom = 36.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    shown,
                    fontFamily = Geologica,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFAF4FF).copy(alpha = fade),
                    textAlign = TextAlign.Center,
                )
                if (p > 0.8f) {
                    Text(
                        "ПРИЁМНИК AIRPLAY",
                        fontFamily = Geologica,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp,
                        color = LiluColors.TextDim.copy(alpha = fade),
                    )
                }
            }
        }
    }
}

/** Кривая сгущения линий сетки к горизонту. */
private fun pow18(x: Float): Float = x.toDouble().pow(1.8).toFloat()

/** Аддитивное наложение: свечение логотипа складывается с фоном, рамки не видно. */
private fun DrawScope.addImage(img: ImageBitmap, side: Float, cx: Float, cy: Float, alpha: Float) {
    if (alpha <= 0.01f || side <= 1f) return
    val s = side.toInt()
    drawImage(
        image = img,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(img.width, img.height),
        dstOffset = IntOffset((cx - side / 2).toInt(), (cy - side / 2).toInt()),
        dstSize = IntSize(s, s),
        alpha = alpha.coerceIn(0f, 1f),
        blendMode = BlendMode.Plus,
    )
}
