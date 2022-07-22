package com.lhr.learn.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description: fragment 容器activity
 */
fun Activity.startFragment(fragment: Class<out Fragment>){
    val intent = Intent(this, ReplaceFragmentActivity::class.java)
    intent.putExtra(ReplaceFragmentActivity.TARGET_FRAGMENT, fragment.name)
    this.startActivity(intent)
}

class ReplaceFragmentActivity: FragmentActivity() {
    companion object{
        const val TARGET_FRAGMENT = "TARGET_FRAGMENT"
    }

    private var targetFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetString = intent?.getStringExtra(TARGET_FRAGMENT) ?: ""
        if (TextUtils.isEmpty(targetString)){
            this.finish()
            return
        }
        val targetClass = Class.forName(targetString)
        targetFragment = targetClass.newInstance() as Fragment
        replaceFragment(targetFragment!!, android.R.id.content)
    }

    private fun replaceFragment(fragment: Fragment, id: Int){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(id, fragment)
        ft.commitAllowingStateLoss()
    }
}