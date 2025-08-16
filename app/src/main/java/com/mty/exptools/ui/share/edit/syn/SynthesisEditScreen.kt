package com.mty.exptools.ui.share.edit.syn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mty.exptools.domain.StepTimer
import com.mty.exptools.ui.share.AlertDialogShared

@Composable
fun SynthesisEditScreen(
    navController: NavController,
    onSetAlarmForCurrent: (message: String, timer: StepTimer) -> Unit,
    viewModel: SynthesisEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allDrafts by viewModel.allDrafts.collectAsStateWithLifecycle()
    val tick by viewModel.tick.collectAsStateWithLifecycle()
    var showBackConfirmDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    val blur by animateDpAsState(
        targetValue = if (uiState.backgroundBlur || showBackConfirmDialog) 12.dp else 0.dp,
        animationSpec = tween(200),
        label = "edit-blur"
    )

    BackHandler(enabled = uiState.mode == SynthesisMode.EDIT) {
        showBackConfirmDialog = true
    }

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            SynthesisEditTopBar(
                mode = uiState.mode,
                running = uiState.running,
                isFinished = uiState.draft.isFinished,
                loadEnable = uiState.nameEditable,
                onBack = {
                    if (uiState.mode == SynthesisMode.EDIT)
                        showBackConfirmDialog = true
                    else
                        navController.popBackStack()
                },
                onLoadOther = { viewModel.onAction(SynthesisAction.LoadSynthesis) },
                onSave = { viewModel.onAction(SynthesisAction.Save) },
                onEdit = { viewModel.onAction(SynthesisAction.Edit) },
                onSetAlarm = {
                    val i = uiState.currentStepIndex
                    uiState.draft.steps.getOrNull(i)?.let { step ->
                        onSetAlarmForCurrent(
                            "合成-${uiState.draft.materialName} 步骤${i+1} ${step.content}结束",
                            step.timer
                        )
                    }
                },
                onToggleRun = { viewModel.onAction(SynthesisAction.ToggleRun) },
                onDelete = { viewModel.onAction(SynthesisAction.DeleteDraft) },
                onSetCompletedAt = { viewModel.onAction(SynthesisAction.ManualCompletedAt) }
            )
        }
    ) { inner ->
        SynthesisEditForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            mode = uiState.mode,
            draft = uiState.draft,
            nameEditable = uiState.nameEditable,
            tick = tick,
            currentStepIndex = uiState.currentStepIndex,
            completedAt = uiState.draft.completedAt,
            onAction = viewModel::onAction // 单入口事件
        )
    }

    when {
        uiState.dialogState.openSubsConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeDialog() },
                onConfirmation = {
                    viewModel.goToSubsequentStep()
                    viewModel.closeDialog()
                },
                dialogTitle = "确认跳转至第${uiState.jumpTargetIndex?.plus(1)}步？",
                dialogText = "此操作不可撤回！该步之前的所有步骤都会变为已完成状态。"
            )
        }
        uiState.dialogState.openPrevConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeDialog() },
                onConfirmation = {
                    viewModel.goToPreviousStep()
                    viewModel.closeDialog()
                },
                dialogTitle = "确认跳转至第${uiState.jumpTargetIndex?.plus(1)}步？",
                dialogText = "此操作不可撤回！该步之后的所有步骤都会变为未完成状态。"
            )
        }
        uiState.dialogState.openCompleteConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeDialog() },
                onConfirmation = {
                    viewModel.completeLastStep()
                    viewModel.closeDialog()
                },
                dialogTitle = "确认完成当前步骤？",
                dialogText = "此操作不可撤回！完成当前步骤后，所有步骤均已完成。"
            )
        }
        uiState.dialogState.openDeleteConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeDialog() },
                onConfirmation = {
                    viewModel.deleteCurrentDraft()
                    viewModel.closeDialog()
                },
                dialogTitle = "确认删除当前合成步骤？",
                dialogText = "此操作不可撤回！"
            )
        }
        uiState.dialogState.openManualCompleteAtDialog -> {
            ManualCompletedAtDialog(
                visible = true,
                initialCompletedAt = uiState.draft.completedAt,
                onConfirm = {
                    viewModel.setCompletedAtWithUiState(it)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        uiState.dialogState.openLoadOtherSheet -> {
            LoadSynthesisSheet(
                visible = true,
                drafts = allDrafts,
                setBackgroundBlur = viewModel::setBackgroundBlur,
                onDismiss = { viewModel.closeDialog() },
                onPick = { draft ->
                    viewModel.loadDraftFrom(draft)
                    viewModel.closeDialog()
                }
            )
        }
    }
    BackConfirmDialog(
        visible = showBackConfirmDialog,
        onDismiss = { showBackConfirmDialog = false },
        onNavigateBack = { navController.popBackStack() }
    )
}
