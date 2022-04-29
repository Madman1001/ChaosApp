package com.example.crash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class CrashActivity : AppCompatActivity() {
    companion object{
        const val CRASH_DETAIL_KEY = "CRASH_DETAIL_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crash_layout)
        this.findViewById<TextView>(R.id.crash_log_detail).text =
            intent.getStringExtra(CRASH_DETAIL_KEY) ?: ""
    }
}