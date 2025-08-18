package com.mty.exptools.ui

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

sealed interface EditRoute

@Serializable
data class SynthesisEditRoute(val materialName: String? = null) : EditRoute

@Serializable
data class PhotoEditRoute(val dbId: Long? = null) : EditRoute

@Serializable
data class TestEditRoute(val dbId: Long? = null) : EditRoute

@Serializable
data class OtherEditRoute(val dbId: Long? = null) : EditRoute