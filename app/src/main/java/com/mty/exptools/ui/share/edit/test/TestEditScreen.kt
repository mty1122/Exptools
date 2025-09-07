package com.mty.exptools.ui.share.edit.test

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
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
import com.mty.exptools.ui.SynthesisEditRoute
import com.mty.exptools.ui.share.AlertDialogShared
import com.mty.exptools.ui.share.edit.SetMillisTimeDialog
import com.mty.exptools.ui.share.edit.syn.BackConfirmDialog
import com.mty.exptools.ui.share.edit.syn.LoadSynthesisSheet

@Composable
fun TestEditScreen(
    navController: NavController,
    viewModel: TestEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allSynDrafts by viewModel.allSynDrafts.collectAsStateWithLifecycle()
    val allTestDrafts by viewModel.allTestDrafts.collectAsStateWithLifecycle()
    var showBackConfirmDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    val blur by animateDpAsState(
        targetValue = if (uiState.backgroundBlur || showBackConfirmDialog) 12.dp else 0.dp,
        animationSpec = tween(200),
        label = "edit-blur"
    )

    BackHandler {
        if (uiState.mode == TestMode.EDIT)
            showBackConfirmDialog = true
        else
            navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.blur(blur),
        topBar = {
            TestEditTopBar(
                mode = uiState.mode,
                isNew = uiState.isNew,
                onBack = {
                    if (uiState.mode == TestMode.EDIT)
                        showBackConfirmDialog = true
                    else
                        navController.popBackStack()
                },
                onLoadOther = { viewModel.onAction(TestAction.LoadTest) },
                onSave = { viewModel.onAction(TestAction.Save) },
                onEdit = { viewModel.onAction(TestAction.Edit) },
                onDelete = { viewModel.onAction(TestAction.DeleteDraft) },
            )
        }
    ) { inner ->
        TestEditForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = inner.calculateTopPadding()) // 列表内容向下延申
                .imePadding(),
            ui = uiState,
            onClickMaterialName = { name ->
                navController.navigate(SynthesisEditRoute(name))
            },
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
        uiState.dialogState.openSetStartAtDialog -> {
            SetMillisTimeDialog(
                visible = true,
                initialCompletedAt = uiState.draft.startAt,
                onConfirm = {
                    viewModel.setStartAt(it)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        uiState.dialogState.openLoadOtherSheet -> {
            LoadTestSheet(
                visible = true,
                drafts = allTestDrafts,
                onDismiss = { viewModel.closeDialog() },
                setBackgroundBlur = viewModel::setBackgroundBlur,
                onPick = { draft ->
                    viewModel.loadDraftFrom(draft)
                    viewModel.closeDialog()
                }
            )
        }
        uiState.dialogState.openLoadMaterialSheet -> {
            LoadSynthesisSheet(
                visible = true,
                drafts = allSynDrafts,
                onDismiss = { viewModel.closeDialog() },
                setBackgroundBlur = viewModel::setBackgroundBlur,
                onPick = { draft ->
                    viewModel.onAction(TestAction.UpdateMaterial(draft.materialName))
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