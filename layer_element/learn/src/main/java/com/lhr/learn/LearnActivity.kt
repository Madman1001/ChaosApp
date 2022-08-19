package com.lhr.learn

import android.os.Bundle
import android.widget.Button
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseAdapter
import com.lhr.learn.applications.AppListFragment
import com.lhr.learn.bitmap.BitmapCropFragment
import com.lhr.learn.classcheck.ClassCheckFragment
import com.lhr.learn.databinding.ActivityLearnBinding

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "Android基础")
class LearnActivity : BaseActivity<ActivityLearnBinding>() {
    private val tag = "AS_${this::class.java.simpleName}"
    private val dataList = listOf(
        ListData("App List", this::gotoAppList),
        ListData("Bitmap Crop", this::gotoImageCrop),
        ListData("Class Check", this::gotoClassCheck),
    )

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.listRv.run {
            this.adapter = object : BaseAdapter<ListData>() {
                override fun bind(holder: ViewHolder, position: Int, data: ListData) {
                    holder.itemView.findViewById<Button>(R.id.itemBtn).run {
                        text = data.name
                        setOnClickListener {
                            data.action.invoke()
                        }
                    }
                }

                override var layout: Int = R.layout.item_learn_list
            }.apply {
                this.addData(dataList)
            }
        }
    }

    private fun gotoClassCheck() {
        this.startFragment<ClassCheckFragment>()
    }

    private fun gotoImageCrop() {
        this.startFragment<BitmapCropFragment>()
    }

    private fun gotoAppList() {
        this.startFragment<AppListFragment>()
    }

    data class ListData(val name: String, val action: () -> Unit = {})
}