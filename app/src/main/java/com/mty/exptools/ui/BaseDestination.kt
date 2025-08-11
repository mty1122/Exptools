package com.mty.exptools.ui

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

sealed interface EditRoute

@Serializable
data class SynthesisEditRoute(val materialName: String? = null) : EditRoute