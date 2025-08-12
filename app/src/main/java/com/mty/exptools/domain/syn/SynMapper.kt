package com.mty.exptools.domain.syn

import android.icu.util.TimeUnit
import com.mty.exptools.domain.StepTimer
import com.mty.exptools.logic.model.syn.SynthesisDraftEntity
import com.mty.exptools.logic.model.syn.SynthesisDraftWithSteps
import com.mty.exptools.logic.model.syn.SynthesisStepEntity

// Domain <-> Entity 中建议把 enum 转 String
private fun String.toTimeUnit(): TimeUnit =
    if (this == "HOUR") TimeUnit.HOUR else TimeUnit.MINUTE

private fun TimeUnit.asString(): String = if (this == TimeUnit.HOUR) "HOUR" else "MINUTE"

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

fun SynthesisDraft.toEntity(now: Long = System.currentTimeMillis()): SynthesisDraftEntity =
    SynthesisDraftEntity(
        // id 由数据库维持
        materialName = materialName,
        rawMaterials = rawMaterials,
        conditionSummary = conditionSummary,
        expDetails = expDetails,
        updatedAt = now
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
