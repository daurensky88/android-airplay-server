package io.github.jqssun.airplay.ui

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.ui.theme.LiluColors
import androidx.compose.foundation.shape.RoundedCornerShape

fun Context.isTvDevice(): Boolean =
    packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
        (getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).currentModeType ==
        Configuration.UI_MODE_TYPE_TELEVISION

@Composable
fun isTv(): Boolean {
    val ctx = LocalContext.current
    return remember(ctx) { ctx.isTvDevice() }
}

// На телевизоре мыши нет: человек ходит стрелками и должен мгновенно понимать,
// где находится. Поэтому обводка бирюзовая — она контрастирует с фиолетовым
// интерфейсом не только яркостью, но и цветом.
fun Modifier.dpadFocus(shape: Shape = RoundedCornerShape(12.dp)): Modifier = composed {
    var focused by remember { mutableStateOf(false) }
    onFocusChanged { focused = it.isFocused }
        .thenIf(focused) { background(LiluColors.SurfaceFocus, shape) }
        .border(
            if (focused) 3.dp else 0.dp,
            if (focused) LiluColors.Cyan else Color.Transparent,
            shape
        )
}

fun Modifier.dpadAdjust(onLeft: () -> Unit, onRight: () -> Unit): Modifier = composed {
    val focus = LocalFocusManager.current
    onPreviewKeyEvent { e ->
        if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when (e.key) {
            Key.DirectionLeft -> { onLeft(); true }
            Key.DirectionRight -> { onRight(); true }
            // slider
            Key.DirectionUp -> { focus.moveFocus(FocusDirection.Up); true }
            Key.DirectionDown -> { focus.moveFocus(FocusDirection.Down); true }
            else -> false
        }
    }
}
