package com.luckyzyx.luckytool.hook.scope.otherapp

import android.app.Activity
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.ModulePrefs

object HookAlphaBackupPro : YukiBaseHooker() {
    override fun onHook() {
        //移除许可证检测
        val isPro = prefs(ModulePrefs).getBoolean("remove_check_license", false)
        if (!isPro) return
        findClass("com.ruet_cse_1503050.ragib.appbackup.pro.activities.HomeActivity").hook {
            injectMember {
                method {
                    name = "onCreate"
                }
                beforeHook {
                    instance<Activity>().intent.putExtra("licenseState", "valid_licence")
                }
            }
        }
    }
}