package com.example.access.utils

import android.util.Log
import com.example.access.bean.PermissionActionBean
import com.example.access.bean.PermissionIntentBean
import com.example.access.bean.PermissionRuleBean
import com.example.access.bean.RomRuleBean
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Boolean

/**
 * @author lhr
 * @date 2021/7/9
 * @des
 */
object JsonUtils {
    /**
     * 分割标签
     */
    private const val RULE_GREP_SIGN_KEY = "$"

    /**
     * ROM 标签
     */
    private const val ROM_RULE_VERSION_KEY = "version"
    private const val ROM_RULE_PERMISSION_KEY = "permission"

    /**
     * permission标签
     */
    private const val PERMISSION_RULE_TYPE_KEY = "type"
    private const val PERMISSION_RULE_PRIORITY_KEY = "priority"
    private const val PERMISSION_RULE_CHECKABLE_KEY = "checkable"
    private const val PERMISSION_RULE_INTENT_KEY = "intent"
    private const val PERMISSION_RULE_ACTION_KEY = "action"

    /**
     * intent标签
     */
    private const val INTENT_RULE_ACTION_KEY = "action"
    private const val INTENT_RULE_PACKAGE_KEY = "package"
    private const val INTENT_RULE_ACTIVITY_KEY = "activity"
    private const val INTENT_RULE_NEW_EXTRA_KEY = "new_extra"
    private const val INTENT_RULE_NEW_DATA_KEY = "new_data"

    /**
     * action标签
     */
    private const val ACTION_RULE_NEED_WAIT_TIME_KEY = "need_wait_time"
    private const val ACTION_RULE_NEED_WAIT_WINDOW_KEY = "need_wait_window"
    private const val ACTION_RULE_SCROLL_NODE_KEY = "scroll_node"
    private const val ACTION_RULE_CHECK_NODE_KEY = "check_node"
    private const val ACTION_RULE_LOCATE_NODE_KEY = "locate_node"
    private const val ACTION_RULE_OPERATION_NODE_KEY = "operation_node"
    private const val ACTION_RULE_CLICK_NODE_KEY = "click_node"
    private const val ACTION_RULE_NOT_NEED_PERFORM_BACK_KEY = "not_need_perform_back"

    private const val ACTION_RULE_SUB_CLASS_NAME_KEY = "class_name"
    private const val ACTION_RULE_SUB_CORRECT_STATUS_KEY = "correct_status"
    private const val ACTION_RULE_SUB_FIND_TEXTS_KEY = "find_texts"
    private const val ACTION_RULE_SUB_BEHAVIOR_KEY = "behavior"

    fun readJson(data: String): RomRuleBean? {
        return try {
            createRomRuleBean(JSONObject(data))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    @Throws(JSONException::class)
    private fun createRomRuleBean(jSONObject: JSONObject): RomRuleBean? {
        var bean:RomRuleBean? = null
        if (jSONObject.has(ROM_RULE_VERSION_KEY)) {
            bean = RomRuleBean(jSONObject.getInt(ROM_RULE_VERSION_KEY))
        }
        bean?.let {
            if (jSONObject.has(ROM_RULE_PERMISSION_KEY)) {
                val jSONArray: JSONArray? = jSONObject.getJSONArray(ROM_RULE_PERMISSION_KEY)
                if (jSONArray != null) {
                    for (i in 0 until jSONArray.length()) {
                        val permissions = createPermissionRuleBean(jSONArray.getJSONObject(i))
                        if (permissions != null){
                            it.permissionRuleBeans.add(permissions)
                        }
                    }
                }
            }
        }
        return bean
    }

    @Throws(JSONException::class)
    private fun createPermissionRuleBean(jSONObject: JSONObject?): PermissionRuleBean? {
        if (jSONObject == null || !jSONObject.has(PERMISSION_RULE_TYPE_KEY)) {
            return null
        }
        val permission = PermissionRuleBean(jSONObject.getInt(PERMISSION_RULE_TYPE_KEY))
        if (jSONObject.has(PERMISSION_RULE_PRIORITY_KEY)) {
            permission.priority = jSONObject.getInt(PERMISSION_RULE_PRIORITY_KEY)
        }

        if (jSONObject.has(PERMISSION_RULE_CHECKABLE_KEY)) {
            val enable = jSONObject.getInt(PERMISSION_RULE_CHECKABLE_KEY) == 1
            permission.checkable = enable
        }

        if (jSONObject.has(PERMISSION_RULE_INTENT_KEY)) {
            permission.ruleIntent = createPermissionIntentBean(jSONObject.getJSONObject(PERMISSION_RULE_INTENT_KEY))
        }

        if (jSONObject.has(PERMISSION_RULE_ACTION_KEY)) {
            val jSONArray = jSONObject.getJSONArray(PERMISSION_RULE_ACTION_KEY)
            for (index in 0 until jSONArray.length()) {
                val action = createActionBean(jSONArray.getJSONObject(index))
                if (action != null){
                    permission.actionList.add(action)
                }
            }
        }
        return permission
    }

    @Throws(JSONException::class)
    private fun createPermissionIntentBean(jSONObject: JSONObject?): PermissionIntentBean? {
        if (jSONObject == null || !jSONObject.has(INTENT_RULE_ACTION_KEY)) {
            return null
        }
        var intentBean:PermissionIntentBean? = null
        if (jSONObject.has(INTENT_RULE_ACTION_KEY)) {
            intentBean = PermissionIntentBean(jSONObject.getString(INTENT_RULE_ACTION_KEY))
        }
        if (jSONObject.has(INTENT_RULE_ACTIVITY_KEY)) {
            intentBean?.permissionActivity = (jSONObject.getString(INTENT_RULE_ACTIVITY_KEY))
        }
        if (jSONObject.has(INTENT_RULE_PACKAGE_KEY)) {
            intentBean?.permissionPackage = jSONObject.getString(INTENT_RULE_PACKAGE_KEY)
        }

        if (jSONObject.has(INTENT_RULE_NEW_EXTRA_KEY)) {
            var string = jSONObject.getString(INTENT_RULE_NEW_EXTRA_KEY)
            if (string.contains(RULE_GREP_SIGN_KEY)) {
            }
        }
        if (jSONObject.has(INTENT_RULE_NEW_DATA_KEY)) {
            var string2 = jSONObject.getString(INTENT_RULE_NEW_DATA_KEY)
            if (string2.contains(RULE_GREP_SIGN_KEY)) {
            }
        }
        return intentBean
    }

    @Throws(JSONException::class)
    private fun createActionBean(jSONObject: JSONObject?): PermissionActionBean? {
        if (jSONObject == null) {
            return null
        }

        val actionBean = PermissionActionBean()
        if (jSONObject.has(ACTION_RULE_NEED_WAIT_TIME_KEY)) {
            actionBean.needWaitTime = jSONObject.getInt(ACTION_RULE_NEED_WAIT_TIME_KEY)
        }

        if (jSONObject.has(ACTION_RULE_NEED_WAIT_WINDOW_KEY)) {
            actionBean.needWaitWindow = jSONObject.getBoolean(ACTION_RULE_NEED_WAIT_WINDOW_KEY)
        }

        if (jSONObject.has(ACTION_RULE_SCROLL_NODE_KEY)) {
            val scrollJson = jSONObject.getJSONObject(ACTION_RULE_SCROLL_NODE_KEY)
            if (scrollJson.has(ACTION_RULE_SUB_CLASS_NAME_KEY)) {
                actionBean.scrollNode = scrollJson.getString(ACTION_RULE_SUB_CLASS_NAME_KEY)
            }
        }

        if (jSONObject.has(ACTION_RULE_CHECK_NODE_KEY)) {
            val checkJson = jSONObject.getJSONObject(ACTION_RULE_CHECK_NODE_KEY)
            if (checkJson.has(ACTION_RULE_SUB_CLASS_NAME_KEY)) {
                actionBean.checkNode.nodeClassName = jSONObject.getString(ACTION_RULE_SUB_CLASS_NAME_KEY)
            }
            if (jSONObject.has(ACTION_RULE_SUB_CORRECT_STATUS_KEY)) {
                var status = jSONObject.getString(ACTION_RULE_SUB_CORRECT_STATUS_KEY)
                val defaultStatus = "true"
                if (!status.equals(defaultStatus, ignoreCase = true) && !status.equals("false", ignoreCase = true)) {
                    status = defaultStatus
                }
                actionBean.checkNode.nodeStatus = Boolean.parseBoolean(status)
            }
        }

        if (jSONObject.has(ACTION_RULE_LOCATE_NODE_KEY)) {
            val locateNode = jSONObject.getJSONObject(ACTION_RULE_LOCATE_NODE_KEY)
            if (locateNode.has(ACTION_RULE_SUB_FIND_TEXTS_KEY)) {
                val jSONArray = locateNode.getJSONArray(ACTION_RULE_SUB_FIND_TEXTS_KEY)
                for (index in 0 until jSONArray.length()) {
                    var findText = jSONArray.getString(index)
                    if (findText.contains(RULE_GREP_SIGN_KEY)) {
                        //todo 设置为应用名称
                        findText = "My Application"
                    }
                    actionBean.findTexts.add(findText)
                }
            }
        }

        if (jSONObject.has(ACTION_RULE_OPERATION_NODE_KEY)) {
            val operationNode = jSONObject.getJSONObject(ACTION_RULE_OPERATION_NODE_KEY)
            if(operationNode.has(ACTION_RULE_SUB_BEHAVIOR_KEY)) {
                actionBean.behavior = operationNode.getString(ACTION_RULE_SUB_BEHAVIOR_KEY)
            }
        }

        if (jSONObject.has(ACTION_RULE_CLICK_NODE_KEY)) {
            val operationNode = jSONObject.getJSONObject(ACTION_RULE_CLICK_NODE_KEY)
            if(operationNode.has(ACTION_RULE_SUB_CLASS_NAME_KEY)) {
                actionBean.clickNode = operationNode.getString(ACTION_RULE_SUB_CLASS_NAME_KEY)
            }
        }

        if (jSONObject.has(ACTION_RULE_NOT_NEED_PERFORM_BACK_KEY)) {
            actionBean.notNeedBack = jSONObject.getBoolean(ACTION_RULE_NOT_NEED_PERFORM_BACK_KEY)
        }

        return actionBean
    }
}