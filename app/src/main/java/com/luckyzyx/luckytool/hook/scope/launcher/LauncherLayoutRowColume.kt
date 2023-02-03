package com.luckyzyx.luckytool.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.IntArrayType
import com.luckyzyx.luckytool.utils.tools.ModulePrefs

object LauncherLayoutRowColume : YukiBaseHooker() {
    override fun onHook() {
        //Source UiConfig
        findClass("com.android.launcher.UiConfig").hook {
            injectMember {
                method {
                    name = "isSupportLayout"
                }
                replaceToTrue()
            }
        }
        val maxRows = prefs(ModulePrefs).getInt("launcher_layout_max_rows", 6)
        val maxColumns = prefs(ModulePrefs).getInt("launcher_layout_max_columns", 4)
        //Source ToggleBarLayoutAdapter
        findClass("com.android.launcher.togglebar.adapter.ToggleBarLayoutAdapter").hook {
            injectMember {
                method {
                    name = "initToggleBarLayoutConfigs"
                }
                beforeHook {
                    field {
                        name = "MIN_MAX_COLUMN"
                        type = IntArrayType
                    }.get().cast<IntArray>()?.set(1, maxColumns)
                    field {
                        name = "MIN_MAX_ROW"
                        type = IntArrayType
                    }.get().cast<IntArray>()?.set(1, maxRows)
                }
            }
        }
    }
}