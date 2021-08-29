package com.example.utils

import android.app.Activity
import android.content.Intent

/**
 * @author lhr
 * @date 2021/8/23
 * @des
 */
fun Activity.start(activityClass: Class<out Activity>){
    this.startActivity(Intent(this,activityClass))
}