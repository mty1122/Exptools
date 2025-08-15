package com.mty.exptools.domain.photo

data class PhotoTargetMaterial(val name: String, val waveLength: String) {

    companion object {
        val TC = PhotoTargetMaterial("TC", "357")
    }

}
