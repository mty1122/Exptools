package com.mty.exptools.ui.home.center

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart

@Composable
fun FrostedBottomBand(
    bandHeight: Dp,                 // 传 innerPadding.calculateBottomPadding()
    blurRadius: Dp = 24.dp,
    overlay: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
    content: @Composable (LazyListState, Boolean) -> Unit
) {
    val mainState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val bandState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val density = LocalDensity.current
    val bandPx = with(density) { bandHeight.roundToPx() }
    val navBarDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navBarPx = with(density) { navBarDp.roundToPx() }
    val totalBandHeight = bandHeight + navBarDp
    val totalBandPx = with(density) { totalBandHeight.roundToPx() }
    val blurPx = with(density) { blurRadius.toPx() }

    LaunchedEffect(mainState) {
        snapshotFlow { mainState.firstVisibleItemIndex to mainState.firstVisibleItemScrollOffset }
            .onStart {
                repeat (10) {
                    if (mainState.firstVisibleItemScrollOffset != bandState.firstVisibleItemScrollOffset)
                        return@repeat
                    emit(mainState.firstVisibleItemIndex to mainState.firstVisibleItemScrollOffset)
                    delay(50)
                }
            }
            .collect { (index, offset) ->
                if (
                    bandState.firstVisibleItemIndex != index ||
                    bandState.firstVisibleItemScrollOffset != offset
                ) {
                    bandState.scrollToItem(index, offset)
                }

                val delta = navBarPx.toFloat()

                if (delta != 0f) {
                    bandState.scrollBy(2 * delta)
                }
            }
    }

    // 用 SubcomposeLayout 拿到屏幕宽高，并分别测量正常内容/底部条带
    SubcomposeLayout(Modifier.fillMaxSize()) { constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight

        // 1) 正常内容（先绘制）
        val main = subcompose("main") { Box(Modifier.fillMaxSize()) { content(mainState, true) } }
            .map { it.measure(constraints) }

        // 2) 条带：内部自己处理“全屏模糊 + 裁剪到条带”
        val band = subcompose("band") {
            BottomBlurBand(
                fullWidth = w,
                fullHeight = h,
                bandHeightPx = totalBandPx,
                navBarHeight = navBarPx,
                blurPx = blurPx,
                overlay = overlay,
                content = { content(bandState, false) }
            )
        }.map { it.measure(Constraints.fixed(w, totalBandPx)) }

        layout(w, h) {
            main.forEach { it.place(0, 0) }
            // 把条带放到底部
            band.forEach { it.place(0, h - bandPx) }
        }
    }
}

// 这个 Composable 是“底部条带”的内部：
// 1) 条带自身 clipToBounds
// 2) 条带里塞一份“全屏尺寸”的模糊副本，并向上平移 (全屏高 - 条带高)
// 3) 叠一层半透明色让磨砂自然
@Composable
private fun BottomBlurBand(
    fullWidth: Int,
    fullHeight: Int,
    bandHeightPx: Int,
    navBarHeight: Int,
    blurPx: Float,
    overlay: Color,
    content: @Composable () -> Unit
) {
    val bandHeightDp = with(LocalDensity.current) { bandHeightPx.toDp() }

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .height(bandHeightDp)
            .clipToBounds(),          // 只让条带区域可见
        content = {
            // 用全屏尺寸测量“模糊副本”
            Box(
                Modifier.graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                    renderEffect = BlurEffect(null, blurPx, blurPx, TileMode.Decal)
                }
            ) { content() }

            // 叠加一层半透明蒙版，磨砂更自然
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind { drawRect(overlay) }
            )
        }
    ) { measurables, _ ->
        // 第一个 child 是“模糊副本”，强制用全屏尺寸测量
        val blurred = measurables[0].measure(Constraints.fixed(fullWidth, fullHeight))
        // 第二个 child 是半透明覆盖，正好条带大小
        val cover = measurables[1].measure(Constraints.fixed(fullWidth, bandHeightPx))

        layout(fullWidth, bandHeightPx) {
            // 把“模糊副本”上移 (全屏高 - 条带高)，让条带里露出“底部那截”
            blurred.place(0, -(fullHeight - bandHeightPx - navBarHeight))
            cover.place(0, 0)
        }
    }
}
