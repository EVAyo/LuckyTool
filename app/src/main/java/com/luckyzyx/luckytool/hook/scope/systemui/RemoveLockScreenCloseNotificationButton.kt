package com.luckyzyx.luckytool.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveLockScreenCloseNotificationButton : YukiBaseHooker() {
    override fun onHook() {
        //Source NotificationPanelViewExt
        "com.oplusos.systemui.notification.extend.NotificationPanelViewExt".toClass().apply {
            method {
                name = "setNotificationCloseButton"
                paramCount = 1
            }.hook { intercept() }
        }
    }
}