package com.luckyzyx.luckytool.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveLauncherHighTempreatureProtection : YukiBaseHooker() {
    override fun onHook() {
        //Source HighTemperatureProtectionManager
        "com.android.launcher.hightemperatureprotection.HighTemperatureProtectionManager".toClass()
            .apply {
                method { name = "isHighTempProtectedEnable" }.hook {
                    replaceToFalse()
                }
                method {
                    name = "isInterceptItemClickFromHighTempreatureProtection"
                    paramCount = 2
                }.hook {
                    replaceToFalse()
                }
            }
    }
}