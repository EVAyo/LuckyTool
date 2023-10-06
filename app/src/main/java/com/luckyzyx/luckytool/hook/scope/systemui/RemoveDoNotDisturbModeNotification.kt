package com.luckyzyx.luckytool.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveDoNotDisturbModeNotification : YukiBaseHooker() {
    override fun onHook() {
        //Source DndAlertHelper
        VariousClass(
            "com.oplusos.systemui.notification.helper.DndAlertHelper",
            "com.coloros.systemui.notification.helper.DndAlertHelper",
            "com.oplus.systemui.statusbar.notification.helper.DndAlertHelper" //C14
        ).toClass().apply {
            method { name = "sendNotificationWithEndtime";paramCount = 1 }.hook {
                intercept()
            }
        }
    }
}