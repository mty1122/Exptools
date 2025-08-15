package com.mty.exptools.ui.share.edit.syn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import com.mty.exptools.ui.share.AlertDialogShared

@Composable
fun BackConfirmDialog(
    visible: Boolean,
    onDismiss: () -> Unit,          // 纯状态：把 visible=false
    onNavigateBack: () -> Unit      // 执行 popBackStack()/navigateUp()
) {
    var pendingBack by rememberSaveable { mutableStateOf(false) }

    if (visible) {
        AlertDialogShared(
            onDismissRequest = onDismiss,
            onConfirmation = {
                pendingBack = true
                onDismiss()
            },
            dialogTitle = "确认退出当前页面？",
            dialogText = "如未点击保存按钮，所做的更改不会保存！"
        )
    }

    // 2) 等弹窗从 true→false 提交后，再退栈（等两帧更稳）
    LaunchedEffect(visible, pendingBack) {
        if (pendingBack && !visible) {
            withFrameNanos { }   // 下一帧
            withFrameNanos { }   // 再下一帧（避免和过渡/布局抢时序）
            pendingBack = false
            onNavigateBack()
        }
    }
}