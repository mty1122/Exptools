package com.mty.exptools.util

import android.widget.Toast
import com.mty.exptools.ExptoolsApp

fun toast(msg: String) {
    Toast.makeText(ExptoolsApp.context, msg, Toast.LENGTH_SHORT).show()
}