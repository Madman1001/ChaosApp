package com.lhr.learn.classcheck

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.lhr.learn.utils.ClassScanUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @CreateDate: 2022/11/17
 * @Author: mac
 * @Description:
 */
fun getClassesViewModel(app: Application):ClassesViewModel =
    ViewModelProvider.AndroidViewModelFactory.getInstance(app).create(ClassesViewModel::class.java)
class ClassesViewModel(app: Application): AndroidViewModel(app) {
    private val app get() = getApplication<Application>()

    private val wordTreeRoot = CharNode(' ', false)

    private var allClassList = listOf<String>()

    fun getAllClasses(): List<String>{
        if (allClassList.isNotEmpty()){
            return allClassList
        }
        allClassList = ClassScanUtil.getAllClass(app)
        makeClassesWordTree(allClassList)
        return allClassList
    }

    fun matchClasses(clazz: String): List<String>{
        if (TextUtils.isEmpty(clazz)) return allClassList
        val result = ArrayList<String>()
        var curNode = wordTreeRoot
        val sb = StringBuilder()
        for (c in clazz) {
            curNode = curNode.charMap[c] ?: return result
            sb.append(c)
        }
        sb.deleteCharAt(sb.lastIndex)
        dfsMatchTree(curNode, sb, result)
        return result
    }

    private fun dfsMatchTree(node: CharNode, sb: StringBuilder, result: ArrayList<String>){
        sb.append(node.char)
        if (node.isWord){
            result.add(sb.toString())
        }
        for (child in node.charMap.values) {
            dfsMatchTree(child, sb, result)
        }
        sb.deleteCharAt(sb.lastIndex)
    }

    private fun makeClassesWordTree(words: List<String>){
        for (word in words) {
            var curNode = wordTreeRoot
            for (c in word) {
                var nextNode = curNode.charMap[c]
                if (nextNode == null){
                    nextNode = CharNode(c, false)
                    curNode.charMap[c] = nextNode
                }
                curNode = nextNode
            }
            curNode.isWord = true
        }
    }
}

class CharNode(val char: Char, var isWord: Boolean){
    val charMap by lazy { HashMap<Char,CharNode>() }
}