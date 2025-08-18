package com.mty.exptools.ui.home.center.list.item

import android.icu.util.TimeUnit
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mty.exptools.R
import com.mty.exptools.domain.photo.LightSource
import com.mty.exptools.domain.photo.PhotoTargetMaterial
import com.mty.exptools.ui.theme.ExptoolsTheme
import com.mty.exptools.util.MillisTime
import com.mty.exptools.util.asString

@Composable
fun ItemPhotocatalysis(
    uiState: ItemPhotoUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 左侧图标
                Image(
                    painter = painterResource(id = R.drawable.icon_sun),
                    contentDescription = "光催化任务",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 6.dp)
                )

                // 中间文本区域
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.materialName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = statusToString(uiState.status, uiState.rightTime.toTime().unit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildString {
                            append(uiState.target.name)
                            if (uiState.target.waveLength != "")
                                append(" | ${uiState.target.waveLength}nm")
                            append(" | ${uiState.lightSource.value}")
                            if (uiState.status != ItemStatus.STATUS_COMPLETE) {
                                append("\n预计结束于 ${uiState.remainTime.stringValue} ")
                                append(uiState.remainTime.unit.asString())
                                append("后")
                            } else if (uiState.performanceList.isNotEmpty()) {
                                append("\n性能：")
                                append(uiState.performanceList.joinToString(" "))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 17.sp
                    )
                }

                // 右侧状态数字
                Text(
                    text = uiState.rightTime.toTime().value.toInt().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (uiState.status != ItemStatus.STATUS_COMPLETE) {
                Spacer(modifier = Modifier.height(9.dp))
                // 进度条
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Round,
                    gapSize = (-4).dp,
                    drawStopIndicator = {}
                )
            } else {
                // 保持上下填充一致
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

private fun statusToString(status: ItemStatus, timeUnit: TimeUnit) = when(status) {
    ItemStatus.STATUS_PAUSE -> "（已暂停）下次测量（${timeUnit.asString()}）"
    ItemStatus.STATUS_START -> "下次测量（${timeUnit.asString()}）"
    ItemStatus.STATUS_COMPLETE -> "已完成（${timeUnit.asString()}）"
}

@Preview(showBackground = true)
@Composable
fun ItemPhotocatalysisPreview() {
    ExptoolsTheme {
        val uiState = ItemPhotoUiState(
            listItemId = 1,
            materialName = "钨酸铋-实验2-22",
            target = PhotoTargetMaterial.TC,
            lightSource = LightSource.XENON_L,
            remainTime = MillisTime(1_800_000).toTime(),
            progress = 0.75f,
            rightTime = MillisTime(1_800_000),
            status = ItemStatus.STATUS_START
        )
        ItemPhotocatalysis(uiState)
    }
}
