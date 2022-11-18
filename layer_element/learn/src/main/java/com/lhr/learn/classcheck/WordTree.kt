package com.lhr.learn.classcheck

import android.text.TextUtils

/**
 * @CreateDate: 2022/11/18
 * @Author: mac
 * @Description: 字典树
 */
class WordTree {
    class CharNode(val char: Char, var isWord: Boolean){
        val charMap by lazy { HashMap<Char,CharNode>() }
    }
    private val root = CharNode('.', false)

    fun reset(){
        root.charMap.clear()
    }

    fun addWords(words: Collection<String>){
        for (word in words) {
            var curNode = root
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

    fun matchWords(word: String, limit: Int): Collection<String>{
        if (TextUtils.isEmpty(word)) return emptyList()
        val result = ArrayList<String>()
        var curNode = root
        val sb = StringBuilder()
        var c: Char = curNode.char
        if (word.length > 1){
            for (i in 0..word.length - 2){
                c = word[i]
                curNode = curNode.charMap[c] ?: return emptyList()
                sb.append(c)
            }
        } else {
            c = word[0]
        }

        dfsMatchTree(curNode.charMap[c] ?: return emptyList(), sb, result, limit)
        return result
    }

    private fun dfsMatchTree(node: CharNode, sb: StringBuilder, result: ArrayList<String>, limit: Int){
        if (result.size == limit) return
        sb.append(node.char)
        if (node.isWord){
            result.add(sb.toString())
        }
        for (child in node.charMap.values) {
            dfsMatchTree(child, sb, result, limit)
        }
        sb.deleteCharAt(sb.lastIndex)
    }
}