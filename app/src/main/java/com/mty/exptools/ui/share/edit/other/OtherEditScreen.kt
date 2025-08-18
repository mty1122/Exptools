package com.mty.exptools.ui.share.edit.other

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
import com.mty.exptools.ui.share.AlertDialogShared
import com.mty.exptools.ui.share.edit.SetMillisTimeDialog
import com.mty.exptools.ui.share.edit.syn.BackConfirmDialog

@Composable
fun OtherEditScreen(
    navController: NavController,
    viewModel: OtherEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allOtherDrafts by viewModel.allOtherDrafts.collectAsStateWithLifecycle()
    var showBackConfirmDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    val blur by animateDpAsState(
        targetValue = if (uiState.backgroundBlur || showBackConfirmDialog) 12.dp else 0.dp,
        animationSpec = tween(200),
        label = "edit-blur"
    )

    BackHandler(enabled = uiState.mode == OtherMode.EDIT) {
        showBackConfirmDialog = true
    }

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            OtherEditTopBar(
                mode = uiState.mode,
                isNew = uiState.isNew,
                onBack = {
                    if (uiState.mode == OtherMode.EDIT)
                        showBackConfirmDialog = true
                    else
                        navController.popBackStack()
                },
                onLoadOther = { viewModel.onAction(OtherEditAction.LoadOther) },
                onSave = { viewModel.onAction(OtherEditAction.Save) },
                onEdit = { viewModel.onAction(OtherEditAction.Edit) },
                onDelete = { viewModel.onAction(OtherEditAction.DeleteDraft) },
            )
        }
    ) { inner ->
        OtherEditForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            ui = uiState,
            onAction = viewModel::onAction,
        )
    }

    when {
        uiState.dialogState.openDeleteConfirmDialog -> {
            AlertDialogShared(
                onDismissRequest = { viewModel.closeDialog() },
                onConfirmation = {
                    viewModel.deleteCurrentDraft()
                    viewModel.closeDialog()
                },
                dialogTitle = "确认删除当前测试？",
                dialogText = "此操作不可撤回！"
            )
        }
        uiState.dialogState.openSetCompletedAtDialog -> {
            SetMillisTimeDialog(
                visible = true,
                initialCompletedAt = uiState.draft.completedAt,
                onConfirm = {
                    viewModel.setCompletedAt(it)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        uiState.dialogState.openLoadOtherSheet -> {
            LoadOtherSheet(
                visible = true,
                drafts = allOtherDrafts,
                onDismiss = { viewModel.closeDialog() },
                setBackgroundBlur = viewModel::setBackgroundBlur,
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