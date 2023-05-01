package com.luckyzyx.luckytool.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.hook.scope.settings.DarkModeList
import com.luckyzyx.luckytool.hook.scope.settings.HookAppDetails
import com.luckyzyx.luckytool.hook.scope.settings.HookIris5Controller
import com.luckyzyx.luckytool.hook.scope.settings.RemoveDpiRestartRecovery
import com.luckyzyx.luckytool.hook.scope.settings.RemoveTopAccountDisplay
import com.luckyzyx.luckytool.utils.ModulePrefs

object HookSettings : YukiBaseHooker() {
    override fun onHook() {
        //暗色模式列表
        if (prefs(ModulePrefs).getBoolean("dark_mode_list_enable", false)) {
            loadHooker(DarkModeList)
        }
        //移除顶部账号显示
        if (prefs(ModulePrefs).getBoolean("remove_top_account_display", false)) {
            loadHooker(RemoveTopAccountDisplay)
        }
        //应用详情页
        loadHooker(HookAppDetails)
        //HookIris5Controller
        if (prefs(ModulePrefs).getBoolean("video_frame_insertion_support_2K120", false)) {
            loadHooker(HookIris5Controller)
        }
        //移除DPI重启恢复
        if (prefs(ModulePrefs).getBoolean("remove_dpi_restart_recovery", false)) {
            loadHooker(RemoveDpiRestartRecovery)
        }
        //settings put global stay_on_while_plugged_in 7
    }
}