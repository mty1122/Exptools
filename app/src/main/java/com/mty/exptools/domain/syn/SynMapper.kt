package com.mty.exptools.domain.syn

import com.mty.exptools.domain.StepTimer
import com.mty.exptools.logic.model.syn.SynthesisDraftEntity
import com.mty.exptools.logic.model.syn.SynthesisDraftWithSteps
import com.mty.exptools.logic.model.syn.SynthesisStepEntity
import com.mty.exptools.util.toTimeUnit
import com.mty.exptools.util.asString

fun SynthesisDraftWithSteps.toDomain(): SynthesisDraft =
    SynthesisDraft(
        materialName = draft.materialName,
        rawMaterials = draft.rawMaterials,
        conditionSummary = draft.conditionSummary,
        expDetails = draft.expDetails,
        steps = steps
            .sortedBy { it.orderIndex }
            .map {
                SynthesisStep(
                    orderIndex = it.orderIndex,
                    content = it.content,
                    unit = it.unit.toTimeUnit(),
                    timer = StepTimer(
                        requiredMillis = it.requiredMillis,
                        accumulatedMillis = it.accumulatedMillis,
                        startEpochMs = it.startEpochMs
                    )
                )
            }
    )

fun SynthesisDraft.toEntity(): SynthesisDraftEntity =
    SynthesisDraftEntity(
        // id 由数据库维持
        materialName = materialName,
        rawMaterials = rawMaterials,
        conditionSummary = conditionSummary,
        expDetails = expDetails,
        updatedAt = System.currentTimeMillis()
    )

fun SynthesisStep.toEntity(orderIndex: Int): SynthesisStepEntity =
    SynthesisStepEntity(
        // id 由数据库维持
        draftId = 0, // 由DAO提供
        orderIndex = orderIndex,
        content = content,
        unit = unit.asString(),
        requiredMillis = timer.requiredMillis,
        accumulatedMillis = timer.accumulatedMillis,
        startEpochMs = timer.startEpochMs
    )
