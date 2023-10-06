package com.luckyzyx.luckytool.hook.scope.notificationmanager

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveNotificationManagerLimit : YukiBaseHooker() {
    override fun onHook() {
        //Source ControllerChannelGroup$AppItemListener -> 通知渠道列表允许通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerChannelGroup\$AppItemListener".toClass()
            .apply {
                method { name = "isSwitchEnabled" }.hook {
                    replaceToTrue()
                }
            }
        //Source ControllerAllowNotificationChannel -> 通知渠道内允许通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationChannel".toClass()
            .apply {
                method { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
        //Source ControllerUnimportantChannel -> 通知渠道不重要通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerUnimportantChannel".toClass()
            .apply {
                method { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
        //Source ControllerAllowNotificationPkg -> 应用内允许通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationPkg".toClass()
            .apply {
                method { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
    }
}