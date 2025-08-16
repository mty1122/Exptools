package com.mty.exptools.ui.share.edit.photo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mty.exptools.domain.StepTimer
import com.mty.exptools.domain.photo.LightSource
import com.mty.exptools.domain.photo.PhotoTargetMaterial
import com.mty.exptools.ui.SynthesisEditRoute
import com.mty.exptools.ui.share.edit.syn.BackConfirmDialog
import com.mty.exptools.ui.share.edit.syn.LoadSynthesisSheet

@Composable
fun PhotoEditScreen(
    navController: NavController,
    onSetAlarmForCurrent: (message: String, timer: StepTimer) -> Unit,
    viewModel: PhotoEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allSynDrafts by viewModel.allSynDrafts.collectAsStateWithLifecycle()
    val tick by viewModel.tick.collectAsStateWithLifecycle()
    var showBackConfirmDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    val blur by animateDpAsState(
        targetValue = if (uiState.backgroundBlur || showBackConfirmDialog) 12.dp else 0.dp,
        animationSpec = tween(200),
        label = "edit-blur"
    )

    BackHandler(enabled = uiState.mode == PhotocatalysisMode.EDIT) {
        showBackConfirmDialog = true
    }

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            PhotoEditTopBar(
                mode = uiState.mode,
                running = true, //uiState.running,
                isFinished = false, //uiState.draft.isFinished,
                onBack = {
                    if (uiState.mode == PhotocatalysisMode.EDIT)
                        showBackConfirmDialog = true
                    else
                        navController.popBackStack()
                },
                onLoadOther = {}, //{ viewModel.onAction(PhotoEditAction.LoadSynthesis) },
                onSave = { viewModel.onAction(PhotoEditAction.Save) },
                onEdit = { viewModel.onAction(PhotoEditAction.Edit) },
                onSetAlarm = {
                    val i = uiState.currentStepIndex
                    uiState.draft.steps.getOrNull(i)?.let { step ->
                        onSetAlarmForCurrent(
                            "光催化-${uiState.draft.catalystName} 步骤${i+1} ${step.name}结束",
                            step.timer
                        )
                    }
                },
                onToggleRun = {}, //{ viewModel.onAction(PhotoEditAction.ToggleRun) },
                onDelete = {}, //{ viewModel.onAction(PhotoEditAction.DeleteDraft) },
                onSetCompletedAt = {}, //{ viewModel.onAction(PhotoEditAction.ManualCompletedAt) }
            )
        }
    ) { inner ->
        PhotoEditForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            ui = uiState,
            tick = tick,
            onClickCatalystName = { catalystName ->
                navController.navigate(SynthesisEditRoute(catalystName))
            },
            onAction = viewModel::onAction,
            existingTargets = listOf(PhotoTargetMaterial.TC),
            existingLights = listOf(LightSource.XENON_L.value, LightSource.XENON_R.value)
        )
    }

    when {
        /*
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
    uiState.dialogState.openLoadOtherDialog -> {
        LoadSynthesisSheet(
            visible = true,
            drafts = allSynDrafts,
            onDismiss = { viewModel.closeDialog() },
            onPick = { draft ->
                viewModel.loadDraftFrom(draft)
                viewModel.closeDialog()
            }
        )
    }

 */
        uiState.photoDialogState.openLoadMaterialSheet -> {
            LoadSynthesisSheet(
                visible = true,
                drafts = allSynDrafts,
                onDismiss = { viewModel.closeDialog() },
                setBackgroundBlur = viewModel::setBackgroundBlur,
                onPick = { draft ->
                    viewModel.onAction(PhotoEditAction.UpdateCatalystName(draft.materialName))
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