package com.example.access

import com.lhr.centre.router.IPlugin

class AccessTable : IPlugin{
    override fun getTitle(): String {
        return "无障碍"
    }

    override fun getLevel(): Int {
        return 0
    }

    override fun getExtra(): String {
        return ""
    }
}