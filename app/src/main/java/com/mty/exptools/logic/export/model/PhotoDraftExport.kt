package com.mty.exptools.logic.export.model

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.photo.toMgL
import com.mty.exptools.util.toMillisTime
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDraftExport(
    val catalystName: String,
    val targetName: String,
    val initialConc: String,
    val lightSource: String,
    val details: String,
    val completedAt: String,
    val performanceList: List<String>
)

fun PhotocatalysisDraft.toExport(): PhotoDraftExport {
    val initialConcValue = toMgL(
        valueText = target.initialConcValue,
        unit = target.initialConcUnit,
        kText = target.stdCurveK,
        bText = target.stdCurveB
    )
    val initialConc = "${"%.2f".format(initialConcValue)} mg/L"
    return PhotoDraftExport(
        catalystName = catalystName,
        targetName = target.name,
        initialConc = initialConc,
        lightSource = light.value,
        details = details,
        completedAt = completedAt?.toMillisTime()?.toDateTime() ?: "",
        performanceList = performanceList
    )
}