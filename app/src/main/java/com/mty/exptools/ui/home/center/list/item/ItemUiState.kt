package com.mty.exptools.ui.home.center.list.item

import com.mty.exptools.util.MillisTime

sealed interface ItemUiState {
    val listItemId: Int
    val title: String
    val info: String
    val rightTime: MillisTime
    val status: ItemStatus

    private enum class ItemCat { SYNTHESIS, PHOTOCATALYSIS, TEST, OTHER }

    fun matchesQuery(raw: String): Boolean {
        val q = raw.trim().lowercase()
        if (q.isEmpty()) return true

        // 分词
        val tokens = q.replace('　', ' ').split(Regex("\\s+")).filter { it.isNotBlank() }
        val typeTokens = setOf("合成", "光催化", "测试", "其他")
        val keywordTokens = tokens.filterNot { it in typeTokens }

        // 1. 文本优先
        if (keywordTokens.isNotEmpty()) {
            val haystack = ("$title $info").lowercase()
            val textOk = keywordTokens.all { haystack.contains(it) }
            if (textOk) return true
        }

        // 2. 类型匹配
        val wantedCats = mutableSetOf<ItemCat>()
        tokens.forEach { t ->
            when (t) {
                "合成" -> wantedCats += ItemCat.SYNTHESIS
                "光催化" -> wantedCats += ItemCat.PHOTOCATALYSIS
                "测试" -> wantedCats += ItemCat.TEST
                "其他" -> wantedCats += ItemCat.OTHER
            }
        }
        if (wantedCats.isNotEmpty()) {
            return when (this) {
                is ItemSynUiState   -> ItemCat.SYNTHESIS in wantedCats
                is ItemPhotoUiState -> ItemCat.PHOTOCATALYSIS in wantedCats
                is ItemTestUiState  -> ItemCat.TEST in wantedCats
                is ItemOtherUiState -> ItemCat.OTHER in wantedCats
            }
        }
        return false
    }
}