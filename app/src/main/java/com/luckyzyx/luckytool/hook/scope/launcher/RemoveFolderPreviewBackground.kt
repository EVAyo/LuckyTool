package com.luckyzyx.luckytool.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.SDK

object RemoveFolderPreviewBackground : YukiBaseHooker() {
    override fun onHook() {
        //Source OplusPreviewBackground folder_icon_bg big_folder_bg
        "com.android.launcher3.folder.OplusPreviewBackground".toClass().apply {
            method { name = "setup" }.hookAll {
                after { field { name = "mBgDrawable" }.get(instance).setNull() }
            }
            method { name = "drawBackground" }.hook {
                intercept()
            }
        }
        if (SDK < A13) return
        //Source OplusFolderAnimationManager
        "com.android.launcher3.folder.OplusFolderAnimationManager".toClass().apply {
            method { name = "getFolderBackgroundAnimator" }.hook {
                intercept()
            }

        }
    }
}