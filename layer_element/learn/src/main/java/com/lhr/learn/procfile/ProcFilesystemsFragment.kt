package com.lhr.learn.procfile

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.R
import com.lhr.learn.databinding.FragmentProcFilesystemsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class ProcFilesystemsFragment: BaseFragment<FragmentProcFilesystemsBinding>() {
    private val basePath = File("/proc")

    private var currentPath = basePath

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (currentPath.path != basePath.path){
                    val file = currentPath.parentFile
                     if (file != null){
                         openFile(file)
                     } else {
                         attachActivity.finish()
                     }
                } else {
                    attachActivity.finish()
                }
            }
        })
    }

    private val filesDataAdapter = object : BaseAdapter<File>() {
        override fun bind(holder: ViewHolder, position: Int, data: File) {
            holder.itemView.findViewById<TextView>(R.id.itemTv).text = data.name
            holder.itemView.findViewById<ImageView>(R.id.itemIv).setImageResource(
                if (data.isDirectory){
                    R.drawable.ic_folder
                } else {
                    R.drawable.ic_file
                }
            )
            holder.itemView.setOnClickListener {
                openFile(data)
            }
        }

        override var layout: Int = R.layout.item_proc_files_list
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.filesRv.run {
            adapter = filesDataAdapter
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
         lifecycleScope.launch(Dispatchers.IO){
             val list = obtainFileList(currentPath)
             filesDataAdapter.replaceData(list)
         }
    }

    private fun openFile(file: File){
        if (file.exists()){
            if (file.isFile){
                ProcFileDetailFragment.start(attachActivity, file.absolutePath)
            } else if (file.isDirectory){
                currentPath = file
                lifecycleScope.launch(Dispatchers.IO){
                    val list = obtainFileList(currentPath)
                    filesDataAdapter.replaceData(list)
                }
            } else {
                Toast.makeText(attachActivity, "无法打开文件或文件夹", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtainFileList(file: File): List<File>{
        val result = mutableListOf<File>()
        if (file.exists() && file.isDirectory){
            for (f in file.listFiles() ?: emptyArray()) {
                result.add(f)
            }
        }
        return result
    }
}