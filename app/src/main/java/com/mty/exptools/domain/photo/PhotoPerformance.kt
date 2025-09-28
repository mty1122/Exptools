package com.mty.exptools.domain.photo

/** 把文本浓度（A 或 mg/L）统一换算为 mg/L；失败返回 null */
fun toMgL(valueText: String, unit: ConcUnit, kText: String, bText: String): Double? =
    when (unit) {
        ConcUnit.MG_L -> valueText.toDoubleOrNull()
        ConcUnit.ABSORBANCE_A -> {
            val a = valueText.toDoubleOrNull() ?: return null
            val k = kText.toDoubleOrNull() ?: return null
            val b = bText.toDoubleOrNull() ?: 0.0
            if (k <= 0.0) null else (a - b) / k
        }
    }

fun toA(valueText: String, unit: ConcUnit, kText: String, bText: String): Double? =
    when (unit) {
        ConcUnit.ABSORBANCE_A -> valueText.toDoubleOrNull()
        ConcUnit.MG_L -> {
            val mgL = valueText.toDoubleOrNull() ?: return null
            val k = kText.toDoubleOrNull() ?: return null
            val b = bText.toDoubleOrNull() ?: 0.0
            if (k <= 0.0) null else mgL * k + b
        }
    }

/** 计算“分解率/产率”，C0=初始/期望浓度，Ci=当前浓度（均 mg/L） */
fun calcPerformance(c0: Double?, ci: Double?): Double? {
    if (c0 == null || ci == null || c0 <= 0.0) return null
    val r = (c0 - ci) / c0
    return when {
        r.isNaN() || r.isInfinite() -> null
        r >= 0.0 -> r
        else -> 1 - r
    }
}