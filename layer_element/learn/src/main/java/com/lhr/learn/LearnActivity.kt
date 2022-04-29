package com.lhr.learn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lhr.centre.annotation.CElement

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "Android基础")
class LearnActivity : AppCompatActivity(){
    private val tag = "AS_${this::class.java.simpleName}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}