package com.luckyzyx.luckytool.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object StatusBarIconVerticalCenter : YukiBaseHooker() {
    override fun onHook() {
        //Source PhoneStatusBarViewExImpl updateContentsPadding
        VariousClass(
            "com.oplusos.systemui.ext.BasePhoneStatusBarViewExt",
            "com.oplus.systemui.statusbar.phone.PhoneStatusBarViewExImpl"
        ).hook {
            injectMember {
                method { name = "getHoleTop" }
                replaceTo(0)
            }
            injectMember {
                method { name = "getHoleBottom" }
                replaceTo(0)
            }
        }
    }
}