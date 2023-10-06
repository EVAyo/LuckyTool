package com.luckyzyx.luckytool.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object ForceDisplayBottomGoogleSettings : YukiBaseHooker() {
    override fun onHook() {
        //Source GooglePreferenceController
        "com.oplus.settings.feature.homepage.controller.GooglePreferenceController".toClass()
            .apply {
                method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(0)
                }
            }
    }
}