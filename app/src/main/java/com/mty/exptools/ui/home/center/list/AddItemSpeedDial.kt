package com.mty.exptools.ui.home.center.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mty.exptools.R

enum class ItemType { SYNTHESIS, PHOTOCATALYSIS, TEST, OTHER }

/**
 * 由当前界面控制 expanded 也可以；如果想组件自持有状态，把 expanded/onExpandedChange 去掉，
 * 在内部用 rememberSaveable { mutableStateOf(false) } 即可。
 */
@Composable
fun AddItemSpeedDial(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAdd: (ItemType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        // 半透明遮罩（点击空白收起）
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onExpandedChange(false) }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {

            SpeedItem(
                visible = expanded,
                icon = painterResource(id = R.drawable.icon_exp),
                label = "合成",
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    onExpandedChange(false)
                    onAdd(ItemType.SYNTHESIS)
                },
                index = 3
            )
            SpeedItem(
                visible = expanded,
                icon = painterResource(id = R.drawable.icon_sun),
                label = "光催化",
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    onExpandedChange(false)
                    onAdd(ItemType.PHOTOCATALYSIS)
                },
                index = 2
            )
            SpeedItem(
                visible = expanded,
                icon = painterResource(id = R.drawable.icon_test),
                label = "测试",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = {
                    onExpandedChange(false)
                    onAdd(ItemType.TEST)
                },
                index = 1
            )
            SpeedItem(
                visible = expanded,
                icon = painterResource(id = R.drawable.icon_other),
                label = "其他",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = {
                    onExpandedChange(false)
                    onAdd(ItemType.OTHER)
                },
                index = 0
            )

            // FAB 旋转不变
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 45f else 0f,
                animationSpec = tween(220),
                label = "fab-rotate"
            )
            FloatingActionButton(
                onClick = { onExpandedChange(!expanded) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (expanded) "收起" else "展开",
                    modifier = modifier.graphicsLayer {
                        rotationZ = rotation
                    }
                )
            }
        }

    }
}

@Composable
private fun SpeedItem(
    visible: Boolean,
    icon: Painter,
    label: String,
    color: Color,
    onClick: () -> Unit,
    index: Int,
    total: Int = 4
) {
    val delayPerItem = 50
    // 微缩放：展开时从 0.92 → 1.0
    val targetScale by remember(visible) { mutableFloatStateOf(if (visible) 1f else 0.92f) }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 160, delayMillis = index * delayPerItem),
        label = "pill-scale"
    )
    val exitDelay = (total - 1 - index) * delayPerItem

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(180, delayMillis = index * delayPerItem)
        ) + fadeIn(animationSpec = tween(180, delayMillis = index * delayPerItem)),
        exit = slideOutVertically(                // ← 新增：收回时向下滑出一点
            targetOffsetY = { it / 3 },
            animationSpec = tween(durationMillis = 150, delayMillis = exitDelay)
        ) + fadeOut(animationSpec = tween(150, delayMillis = exitDelay))
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = color,
            tonalElevation = 6.dp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(6.dp, RoundedCornerShape(14.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 120.dp)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Image(icon, contentDescription = null)
                Text(label, fontWeight = FontWeight.Medium)
            }
        }
    }
}