package com.lhr.learn

import android.os.Bundle
import android.widget.Button
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.ui.startFragment
import com.lhr.learn.applications.AppListFragment
import com.lhr.learn.arpcheck.ArpCheckFragment
import com.lhr.learn.bitmap.BitmapCropFragment
import com.lhr.learn.classcheck.ClassCheckFragment
import com.lhr.learn.databinding.ActivityLearnBinding
import com.lhr.learn.nativelib.NativeLibFragment
import com.lhr.learn.procfile.FilesystemsFragment
import com.lhr.learn.record.RecordFragment

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "Android环境")
class LearnActivity : BaseActivity<ActivityLearnBinding>() {
    private val tag = "AS_${this::class.java.simpleName}"
    private val dataList = listOf(
        ListData("App List", this::gotoAppList),
        ListData("Bitmap Crop", this::gotoImageCrop),
        ListData("Class Check", this::gotoClassCheck),
        ListData("Native Files", this::gotoNativeFileCheck),
        ListData("Proc Files", this::gotoProcFilesystems),
        ListData("Arp Check", this::gotoArpCheck),
        ListData("Record Voice", this::gotoRecord),
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

    private fun gotoProcFilesystems(){
        FilesystemsFragment.start(this, "/proc")
    }

    private fun gotoNativeFileCheck(){
        this.startFragment<NativeLibFragment>()
    }

    private fun gotoArpCheck(){
        this.startFragment<ArpCheckFragment>()
    }

    private fun gotoRecord(){
        this.startFragment<RecordFragment>()
    }

    data class ListData(val name: String, val action: () -> Unit = {})
}