package com.mty.exptools.ui.home.center.list

import kotlinx.serialization.Serializable

sealed class ListDestination {
    @Serializable data object AddTask
    @Serializable data object EditTask
}