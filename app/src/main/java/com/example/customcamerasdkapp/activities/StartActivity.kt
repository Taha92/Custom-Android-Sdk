package com.example.customcamerasdkapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class StartActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = Intent("com.example.custom_camera.activities.MainActivity")
        startActivity(intent)
    }
}