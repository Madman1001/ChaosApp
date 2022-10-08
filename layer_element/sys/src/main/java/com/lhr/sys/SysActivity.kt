package com.lhr.sys

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseAdapter
import com.lhr.sys.databinding.SysActivityBinding
import com.lhr.sys.reflection.HiddenApiBypass
import com.lhr.sys.service.ServiceHookBean
import java.lang.reflect.Proxy

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "系统服务代理")
class SysActivity : BaseActivity<SysActivityBinding>() {
    private val tag = "AS_${this::class.java.simpleName}"

    companion object {
        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                HiddenApiBypass.addHiddenApiExemptions("")
            }
            SERVICE_HOOK_LIST.forEach {
                if (!ServiceHookHelper.hookService(it)) {
                    HookFailList.add(it)
                    Log.e(HOOK_TAG, "service ${it.serviceName} hook fail")
                } else {
                    Log.e(HOOK_TAG, "service ${it.serviceName} hook success")
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.listRv.run {
            layoutManager = GridLayoutManager(this.context, 3, GridLayoutManager.VERTICAL, false)
            adapter = object : BaseAdapter<ServiceHookBean>() {
                override fun bind(holder: ViewHolder, position: Int, data: ServiceHookBean) {
                    holder.itemView.findViewById<Button>(R.id.itemBtn).run {
                        text = data.serviceName

                        val service = this@SysActivity.getSystemService(data.serviceName)
                        if (service == null) {
                            setBackgroundColor(Color.parseColor("#FF0000"))
                        } else {
                            var isProxy = false
                            service::class.java.let {
                                for (ii in it.interfaces) {
                                    if (ii == Proxy::class.java) {
                                        isProxy = true
                                        break
                                    }
                                }
                            }
                            Log.e(this@SysActivity.tag, "is proxy ${isProxy}")
                            if (isProxy) {
                                setBackgroundColor(Color.parseColor("#00FFFF"))
                            } else {
                                setBackgroundColor(Color.parseColor("#FF0000"))
                            }
                        }
                    }
                }

                override var layout: Int = R.layout.item_hook_list
            }.apply {
                this@apply.addData(SERVICE_HOOK_LIST)
            }
        }
    }
}