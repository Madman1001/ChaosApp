package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MainService : Service() {

    private class MyBind : Binder(){

    }

    override fun onBind(intent: Intent): IBinder {
        return MyBind()
    }

    override fun onCreate() {

    }
}