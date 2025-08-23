package com.mty.exptools.logic.export.model

import com.mty.exptools.domain.photo.PhotocatalysisDraft
import com.mty.exptools.logic.model.syn.SynthesisDraftEntity
import com.mty.exptools.util.toMillisTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDraftWithSynthesis(
    @SerialName("催化剂名称") val catalystName: String,
    @SerialName("反应条件") val conditionSummary: String,
    @SerialName("原材料") val rawMaterials: String,
    @SerialName("实验细节") val expDetails: String,
    @SerialName("完成时间") val completedAt: String,
    @SerialName("性能测试") val photoDraft: List<PhotoDraftExport>
)

fun Pair<SynthesisDraftEntity, List<PhotocatalysisDraft>>.toExport(): PhotoDraftWithSynthesis {
    val (synthesisDraft, photoDrafts) = this
    return PhotoDraftWithSynthesis(
        catalystName = synthesisDraft.materialName,
        conditionSummary = synthesisDraft.conditionSummary,
        rawMaterials = synthesisDraft.rawMaterials,
        expDetails = synthesisDraft.expDetails,
        completedAt = synthesisDraft.completedAt?.toMillisTime()?.toDateTime() ?: "",
        photoDraft = photoDrafts.map { it.toExport() }
    )
}
