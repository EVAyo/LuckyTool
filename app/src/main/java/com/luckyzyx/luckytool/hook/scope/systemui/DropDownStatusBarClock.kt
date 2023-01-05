package com.luckyzyx.luckytool.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

class DropDownStatusBarClock : YukiBaseHooker() {
    override fun onHook() {
        //Source Clock
        findClass("com.android.systemui.statusbar.policy.Clock").hook {
            injectMember {
                method {
                    name = "setShowSecondsAndUpdate"
                }
                beforeHook {
                    args(0).setTrue()
                }
            }
        }
    }
}