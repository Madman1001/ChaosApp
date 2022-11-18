package com.lhr.learn.classcheck

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.lhr.learn.utils.ClassScanUtil
/**
 * @CreateDate: 2022/11/17
 * @Author: mac
 * @Description:
 */
fun getClassesViewModel(app: Application):ClassesViewModel =
    ViewModelProvider.AndroidViewModelFactory.getInstance(app).create(ClassesViewModel::class.java)
class ClassesViewModel(app: Application): AndroidViewModel(app) {
    private val app get() = getApplication<Application>()

    private var wordTree = WordTree()

    private var allClassList = listOf<String>()

    fun getClasses(limit: Int = -1): List<String>{
        if (allClassList.isNotEmpty()){
            if (limit >= 0 && allClassList.size > limit){
                return allClassList.subList(0, limit)
            } else {
                return allClassList
            }
        }
        allClassList = ClassScanUtil.getAllClass(app)
        makeClassesWordTree(allClassList)
        if (allClassList.isNotEmpty()){
            return getClasses(limit)
        }
        return allClassList
    }

    private fun makeClassesWordTree(classes: List<String>){
        wordTree.reset()
        for (name in classes) {
            wordTree.addWords(name.split("."))
        }
    }

    fun matchPromptWords(clazz: String, limit: Int): Collection<String> {
        return wordTree.matchWords(clazz, limit)
    }
}
