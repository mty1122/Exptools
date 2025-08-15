package com.mty.exptools.domain.photo

data class LightSource(val label: String, val wavelength: Int) {

    companion object {
        val XENON_L = LightSource("氙灯-左", 400)
        val XENON_R = LightSource("氙灯-右", 420)
    }

}
