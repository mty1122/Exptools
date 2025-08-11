package com.mty.exptools.ui.share.edit.syn

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue

@Composable
fun SynthesisEditScreen(
    onBack: () -> Unit,
    onPickOther: () -> Unit,
    onSetAlarmForCurrent: (materialName: String, stepIndex: Int, step: SynthesisStep) -> Unit,
    viewModel: SynthesisEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SynthesisEditTopBar(
                mode = uiState.mode,
                running = uiState.running,
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
                onToggleRun = { viewModel.onAction(SynthesisAction.ToggleRun) }
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
}
