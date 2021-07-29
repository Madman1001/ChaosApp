package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des 动作数据类
 */
class ActionBean private constructor(builder: ActionBuild) {
    val name: String
    val needWaitTime: Int
    val findTexts: Array<String>
    val scrollNode: String
    val actionNode: String
    val checkNode: String
    val checkStatus:Boolean
    val behavior: String
    val needBack: Boolean

    init {
        this.name = builder.name
        this.needWaitTime = builder.needWaitTime
        this.findTexts = builder.findTexts.toTypedArray()
        this.checkNode = builder.checkNode
        this.checkStatus = builder.checkStatus
        this.scrollNode = builder.scrollNode
        this.actionNode = builder.clickNode
        this.behavior = builder.behavior
        this.needBack = builder.needBack
    }

    override fun toString(): String {
        return "ActionBean(name=$name, needWaitTime=$needWaitTime, findTexts=$findTexts, scrollNode='$scrollNode', clickNode='$actionNode', checkNode='$checkNode', checkStatus=$checkStatus, behavior='$behavior', needBack=$needBack)"
    }


    class ActionBuild {
        var findTexts: ArrayList<String> = ArrayList()
        var name: String = ""
        var needWaitTime: Int = 0
        var scrollNode: String = ""
        var clickNode: String = ""
        var checkNode: String = ""
        var checkStatus = true
        var behavior: String = ""
        var needBack: Boolean = false

        fun build() : ActionBean {
            return ActionBean(this)
        }
    }
}