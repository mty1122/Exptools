package com.mty.exptools.ui.home.center.more

sealed interface MoreUiEvent {
    data class RequestCreateDocument(
        val fileName: String,
        val mime: String,
        val payload: String
    ) : MoreUiEvent

    data class Toast(val msg: String) : MoreUiEvent
}