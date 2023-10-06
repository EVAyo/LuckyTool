package com.luckyzyx.luckytool.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object EnableStatusBarClockFormat : YukiBaseHooker() {
    override fun onHook() {
        //Source RmStatusbarClockPreferenceController
        "com.oplus.settings.feature.notification.controller.RmStatusbarClockPreferenceController".toClass()
            .apply {
                method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(0)
                }
            }
    }
}