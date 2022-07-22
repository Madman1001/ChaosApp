package com.lhr.learn

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lhr.centre.annotation.CElement
import com.lhr.learn.applications.AppListFragment
import com.lhr.learn.base.startFragment
import com.lhr.learn.bitmap.BitmapCropFragment

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
        setContentView(R.layout.activity_learn)
    }

    fun gotoImageCrop(view: View){
        this.startFragment(BitmapCropFragment::class.java)
    }

    fun gotoAppList(view: View){
        this.startFragment(AppListFragment::class.java)
    }
}