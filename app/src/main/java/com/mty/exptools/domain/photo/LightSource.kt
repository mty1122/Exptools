package com.mty.exptools.domain.photo

@JvmInline
value class LightSource(val value: String) {

    companion object {
        val XENON_L = LightSource("氙灯-左 400nm")
        val XENON_R = LightSource("氙灯-右 420nm")
    }

}
