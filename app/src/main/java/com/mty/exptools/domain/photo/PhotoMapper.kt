package com.mty.exptools.domain.photo

import com.mty.exptools.domain.StepTimer
import com.mty.exptools.logic.model.photo.PhotoDraftEntity
import com.mty.exptools.logic.model.photo.PhotoDraftWithSteps
import com.mty.exptools.logic.model.photo.PhotoStepEntity

fun PhotocatalysisDraft.toEntity(): PhotoDraftEntity =
    PhotoDraftEntity(
        id = dbId,
        catalystName = catalystName,
        targetName = target.name,
        targetWavelengthNm = target.wavelengthNm,
        initialConcValue = target.initialConcValue,
        initialConcUnit = target.initialConcUnit.name,
        stdCurveK = target.stdCurveK,
        stdCurveB = target.stdCurveB,
        light = light.value,
        details = details,
        completedAt = completedAt
    )

fun PhotocatalysisStep.toEntity(draftId: Long): PhotoStepEntity =
    PhotoStepEntity(
        // id 由数据库维持
        draftId = draftId,
        orderIndex = orderIndex,
        name = name,
        concValueText = concValueText,
        concUnit = concUnit.name,
        requiredMillis = timer.requiredMillis,
        accumulatedMillis = timer.accumulatedMillis,
        startEpochMs = timer.startEpochMs
    )

private fun PhotoStepEntity.toDomain(): PhotocatalysisStep =
    PhotocatalysisStep(
        orderIndex = orderIndex,
        name = name,
        concValueText = concValueText,
        concUnit = runCatching { ConcUnit.valueOf(concUnit) }.getOrElse { ConcUnit.MG_L },
        timer = StepTimer(
            requiredMillis = requiredMillis,
            accumulatedMillis = accumulatedMillis,
            startEpochMs = startEpochMs
        )
    )

fun PhotoDraftWithSteps.toDomain(): PhotocatalysisDraft =
    PhotocatalysisDraft(
        dbId = draft.id,
        catalystName = draft.catalystName,
        target = PhotocatalysisTarget(
            name = draft.targetName,
            wavelengthNm = draft.targetWavelengthNm,
            initialConcValue = draft.initialConcValue,
            initialConcUnit = runCatching { ConcUnit.valueOf(draft.initialConcUnit) }
                .getOrElse { ConcUnit.MG_L },
            stdCurveK = draft.stdCurveK,
            stdCurveB = draft.stdCurveB
        ),
        light = LightSource(draft.light),
        details = draft.details,
        completedAt = draft.completedAt,
        steps = steps.sortedBy { it.orderIndex }.map { it.toDomain() }
    )