package com.lhr.learn.classcheck

import android.text.TextUtils

/**
 * @author lhr
 * @date 19/11/2022
 * @des
 */
class ClassesTree {
    class WordNode(val word: String, var isEnd: Boolean){
        val wordMap by lazy { HashMap<String,WordNode>() }
    }
    private val root = WordNode(".", false)

    fun reset(){
        root.wordMap.clear()
    }

    fun addClasses(classes: Collection<String>){
        for (clazz in classes) {
            var curNode = root
            for (word in clazz.split(".")){
                var nextNode = curNode.wordMap[word]
                if (nextNode == null){
                    nextNode = WordNode(word, false)
                    curNode.wordMap[word] = nextNode
                }
                curNode = nextNode
            }
            curNode.isEnd = true
        }
    }

    fun matchClasses(word: String, limit: Int = Int.MAX_VALUE): Collection<String>{
        if (TextUtils.isEmpty(word)) return emptyList()
        val result = ArrayList<String>()
        var curNode = root
        val sb = StringBuilder()
        val list = word.split(".")
        var c = curNode.word
        if (word.length > 1){
            for (i in 0..list.size - 2){
                c = list[i]
                curNode = curNode.wordMap[c] ?: return emptyList()
                sb.append(c)
            }
        } else {
            c = list[0]
        }

        dfsMatchTree(curNode.wordMap[c] ?: return emptyList(), sb, result, limit)
        return result
    }

    private fun dfsMatchTree(node: WordNode, sb: StringBuilder, result: ArrayList<String>, limit: Int){
        if (result.size == limit) return
        sb.append(node.word)
        if (node.isEnd){
            result.add(sb.toString())
        }
        for (child in node.wordMap.values) {
            dfsMatchTree(child, sb, result, limit)
        }
        sb.deleteCharAt(sb.lastIndex)
    }
}