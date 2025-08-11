package com.mty.exptools.ui.home.center.list.item

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mty.exptools.R
import com.mty.exptools.ui.theme.ExptoolsTheme

@Composable
fun ItemOther(
    uiState: ItemOtherUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 左侧图标
                Image(
                    painter = painterResource(id = R.drawable.icon_other),
                    contentDescription = "测试任务",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )

                // 中间文本区域
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = statusToString(uiState.status),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${uiState.info} | ${uiState.endDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 右侧状态数字
                Text(
                    text = uiState.rightTimes.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun statusToString(status: ItemStatus) = when(status) {
    ItemStatus.STATUS_START -> "距离结束（天）"
    ItemStatus.STATUS_COMPLETE -> "已结束（天）"
    else -> ""
}

@Preview(showBackground = true)
@Composable
fun ItemItemOtherPreview() {
    ExptoolsTheme {
        val uiState = ItemOtherUiState(
            listItemId = 1,
            title = "某文章撰写",
            info = "引言部分",
            endDate = "2025.03.12",
            rightTimes = 20,
            status = ItemStatus.STATUS_COMPLETE
        )
        ItemOther(uiState)
    }
}
