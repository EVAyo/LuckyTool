package com.luckyzyx.luckytool.hook.scope.systemui

import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.luckyzyx.luckytool.utils.data.A11
import com.luckyzyx.luckytool.utils.data.A12
import com.luckyzyx.luckytool.utils.data.SDK
import com.luckyzyx.luckytool.utils.data.getColorOSVersion

object RemoveStatusBarClockRedOne : YukiBaseHooker() {
    override fun onHook() {
        if (SDK == A11 && getColorOSVersion == "V12"){
            findClass("com.android.systemui.statusbar.policy.Clock").hook {
                injectMember {
                    method {
                        name = "setShouldShowOpStyle"
                        param(BooleanType)
                    }
                    beforeHook {
                        args(0).setFalse()
                    }
                }
            }
        }
        if (SDK >= A12){
            findClass("com.oplusos.systemui.ext.BaseClockExt").hook {
                injectMember {
                    method {
                        name = "setTextWithRedOneStyle"
                        paramCount = 2
                    }
                    beforeHook {
                        args(0).cast<TextView>()?.text = args(1).cast<CharSequence>().toString()
                        resultFalse()
                    }
                }
            }
        }
    }
}