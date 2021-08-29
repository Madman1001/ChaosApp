package com.example.utils

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class UtilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util)

        this.findViewById<View>(R.id.drag_button).setOnTouchListener(DragTouchListener(true))

    }
}