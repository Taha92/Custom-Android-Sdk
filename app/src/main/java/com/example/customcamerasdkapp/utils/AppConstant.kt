package com.example.customcamerasdkapp.utils

import android.Manifest

class AppConstant {
    companion object {
        const val TAG = "CameraXLog"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val MAX_FONT_SIZE = 96F
        const val REQUEST_CODE_PERMISSIONS = 20
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO)
    }
}