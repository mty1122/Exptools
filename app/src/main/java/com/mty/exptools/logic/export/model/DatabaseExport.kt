package com.mty.exptools.logic.export.model

import com.mty.exptools.logic.model.other.OtherDraftEntity
import com.mty.exptools.logic.model.photo.PhotoDraftWithSteps
import com.mty.exptools.logic.model.syn.SynthesisDraftWithSteps
import com.mty.exptools.logic.model.test.TestDraftEntity
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseExport(
    val synthesisDrafts: List<SynthesisDraftWithSteps>,
    val photoDrafts: List<PhotoDraftWithSteps>,
    val testDrafts: List<TestDraftEntity>,
    val otherDrafts: List<OtherDraftEntity>
)
