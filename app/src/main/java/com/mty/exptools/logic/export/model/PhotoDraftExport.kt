package com.mty.exptools.logic.export.model

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.domain.photo.toMgL
import com.mty.exptools.util.toMillisTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PhotoDraftExport(
    @Transient val catalystName: String = "",
    @SerialName("目标名称") val targetName: String,
    @SerialName("初始浓度") val initialConc: String,
    @SerialName("光源") val lightSource: String,
    @SerialName("实验细节") val details: String,
    @SerialName("完成时间") val completedAt: String,
    @SerialName("性能") val performanceList: List<String>
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