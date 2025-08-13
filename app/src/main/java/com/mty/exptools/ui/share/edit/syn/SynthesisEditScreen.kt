package com.mty.exptools.ui.share.edit.syn

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mty.exptools.domain.syn.SynthesisStep
import com.mty.exptools.ui.share.AlertDialogShared

@Composable
fun SynthesisEditScreen(
    onBack: () -> Unit,
    onPickOther: () -> Unit,
    onSetAlarmForCurrent: (materialName: String, stepIndex: Int, step: SynthesisStep) -> Unit,
    viewModel: SynthesisEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val blur by animateDpAsState(
        targetValue = if (uiState.openPrevConfirmDialog || uiState.openNextConfirmDialog
            || uiState.openDeleteConfirmDialog || uiState.openCompleteConfirmDialog)
            12.dp else 0.dp,
        animationSpec = tween(200),
        label = "edit-blur"
    )

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            SynthesisEditTopBar(
                mode = uiState.mode,
                running = uiState.running,
                isFinished = uiState.draft.isFinished,
                onBack = onBack,
                onLoadOther = onPickOther,
                onSave = { viewModel.onAction(SynthesisAction.Save) },
                onEdit = { viewModel.onAction(SynthesisAction.Edit) },
                onSetAlarm = {
                    val i = uiState.currentStepIndex
                    uiState.draft.steps.getOrNull(i)?.let { step ->
                        onSetAlarmForCurrent(uiState.draft.materialName, i, step)
                    }
                },
                onToggleRun = { viewModel.onAction(SynthesisAction.ToggleRun) },
                onDelete = { viewModel.onAction(SynthesisAction.DeleteDraft) }
            )
        }
    ) { inner ->
        SynthesisEditForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            mode = uiState.mode,
            draft = uiState.draft,
            currentStepIndex = uiState.currentStepIndex,
            onAction = viewModel::onAction // 单入口事件，沿用你前面的写法
        )
    }

    when {
        uiState.openNextConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeConfirmDialog() },
                onConfirmation = {
                    viewModel.completeCurrentStep()
                    viewModel.closeConfirmDialog()
                },
                dialogTitle = "确认进入下一步？",
                dialogText = "此操作不可撤回！进入下一步会自动暂停，如需开始请点击开始按钮。"
            )
        }
        uiState.openPrevConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeConfirmDialog() },
                onConfirmation = {
                    viewModel.goToPreviousStep()
                    viewModel.closeConfirmDialog()
                },
                dialogTitle = "确认返回上一步？",
                dialogText = "此操作不可撤回！返回上一步会自动暂停，如需开始请点击开始按钮。"
            )
        }
        uiState.openCompleteConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeConfirmDialog() },
                onConfirmation = {
                    viewModel.completeCurrentStep()
                    viewModel.closeConfirmDialog()
                },
                dialogTitle = "确认完成当前步骤？",
                dialogText = "此操作不可撤回！完成当前步骤后，所有步骤均已完成。"
            )
        }
        uiState.openDeleteConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeConfirmDialog() },
                onConfirmation = {
                    viewModel.deleteCurrentDraft()
                    viewModel.closeConfirmDialog()
                },
                dialogTitle = "确认删除当前合成步骤？",
                dialogText = "此操作不可撤回！"
            )
        }
    }
}
