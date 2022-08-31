package com.lhr.common.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description: fragment 容器activity
 */
inline fun <reified F: Fragment> Activity.startFragment(bundle: Bundle? = null){
    val intent = Intent(this, ReplaceFragmentActivity::class.java)
    intent.putExtra(ReplaceFragmentActivity.TARGET_FRAGMENT, F::class.java.name)
    if (bundle != null){
        intent.putExtras(bundle)
    }
    this.startActivity(intent)
}

class ReplaceFragmentActivity: BaseNoDbActivity() {
    companion object{
        const val TARGET_FRAGMENT = "TARGET_FRAGMENT"
    }

    private var targetFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val targetString = intent?.getStringExtra(TARGET_FRAGMENT) ?: ""
        if (TextUtils.isEmpty(targetString)){
            this@ReplaceFragmentActivity.finish()
            return
        }
        kotlin.runCatching {
            val targetClass = Class.forName(targetString)
            targetFragment = targetClass.newInstance() as Fragment
            targetFragment?.arguments = intent.extras
            replaceFragment(targetFragment!!, android.R.id.content)
        }.onFailure {
            this@ReplaceFragmentActivity.finish()
            Toast.makeText(this.applicationContext, "launch fragment ${targetFragment} fail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment, id: Int){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(id, fragment)
        ft.commitAllowingStateLoss()
    }
}