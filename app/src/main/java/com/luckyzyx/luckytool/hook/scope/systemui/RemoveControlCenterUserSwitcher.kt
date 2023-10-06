package com.luckyzyx.luckytool.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object RemoveControlCenterUserSwitcher : YukiBaseHooker() {
    override fun onHook() {
        //Search Log showUserSwitcher
        "com.oplusos.systemui.qs.OplusQSFooterImpl".toClass().apply {
            method {
                name = "showUserSwitcher"
                emptyParam()
                returnType = BooleanType
            }.hook {
                replaceToFalse()
            }
        }
    }
}