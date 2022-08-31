package com.lhr.learn.procfile

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.common.ext.*
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.ui.BaseFragment
import com.lhr.common.ui.startFragment
import com.lhr.learn.R
import com.lhr.learn.databinding.FragmentFilesystemsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class FilesystemsFragment : BaseFragment<FragmentFilesystemsBinding>() {
    private var basePath = File("")

    private var currentPath: File = basePath
        set(value) {
            field = value
            liveTitle.value = field.absolutePath
        }

    val liveTitle = MutableLiveData<String>(basePath.absolutePath)

    private val filesDataAdapter = object : BaseAdapter<File>() {
        override fun bind(holder: ViewHolder, position: Int, data: File) {
            holder.itemView.findViewById<TextView>(R.id.itemTv).text = data.name
            holder.itemView.findViewById<ImageView>(R.id.itemIv).setImageResource(
                if (data.isDirectory) {
                    R.drawable.ic_folder
                } else {
                    R.drawable.ic_file
                }
            )
            if (data.isFile) {
                val fileSize = data.length()
                holder.itemView.findViewById<TextView>(R.id.itemSizeTv).text =
                    when {
                        fileSize >= GB -> {
                            "${String.format("%.2f", fileSize.byteToGb())}G"
                        }
                        fileSize >= MB -> {
                            "${String.format("%.1f", fileSize.byteToMb())}MB"
                        }
                        fileSize >= KB -> {
                            "${fileSize.byteToKb().toInt()}KB"
                        }
                        else -> {
                            "${fileSize}B"
                        }
                    }
            } else {
                holder.itemView.findViewById<TextView>(R.id.itemSizeTv).text = ""
            }
            holder.itemView.setOnClickListener {
                openFile(data)
            }
        }

        override var layout: Int = R.layout.item_files_list
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (currentPath.path != basePath.path) {
                        val file = currentPath.parentFile
                        if (file != null) {
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

    override fun initView(savedInstanceState: Bundle?) {
        val filePath = arguments?.getString(KEY_FILE_PATH) ?: "/proc"
        basePath = File(filePath)
        currentPath = basePath
        mBinding.fragment = this
        mBinding.filesRv.run {
            adapter = filesDataAdapter
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = obtainFileList(currentPath)
            filesDataAdapter.replaceData(list)
        }
    }

    private fun openFile(file: File) {
        if (file.exists()) {
            if (file.isFile) {
                TxtFileDetailFragment.start(attachActivity, file.absolutePath)
            } else if (file.isDirectory) {
                currentPath = file
                lifecycleScope.launch(Dispatchers.IO) {
                    val list = obtainFileList(currentPath)
                    filesDataAdapter.replaceData(list)
                }
            } else {
                Toast.makeText(attachActivity, "无法打开文件或文件夹", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtainFileList(file: File): List<File> {
        val result = mutableListOf<File>()
        if (file.exists() && file.isDirectory) {
            for (f in file.listFiles() ?: emptyArray()) {
                result.add(f)
            }
        }
        return result
    }

    companion object {
        private const val KEY_FILE_PATH = "KEY_FILE_PATH"

        fun start(activity: Activity, path: String) {
            activity.startFragment<FilesystemsFragment>(Bundle().apply {
                putString(
                    KEY_FILE_PATH,
                    path
                )
            })
        }
    }
}