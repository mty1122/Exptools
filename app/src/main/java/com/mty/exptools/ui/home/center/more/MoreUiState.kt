package com.mty.exptools.ui.home.center.more

data class MoreUiState(
    val autoRefreshSeconds: Int = 10,           // 自动刷新间隔（分钟）
    val exportDirUri: String? = null,
    val exportingRange: Boolean = false,
    val exportingAll: Boolean = false
)