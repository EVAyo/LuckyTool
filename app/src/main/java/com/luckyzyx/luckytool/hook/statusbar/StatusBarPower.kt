package com.luckyzyx.luckytool.hook.statusbar

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.tools.ModulePrefs

object StatusBarPower : YukiBaseHooker() {
    override fun onHook() {
        val removePercent = prefs(ModulePrefs).getBoolean("remove_statusbar_battery_percent", false)
        val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_power_user_typeface", false)
        val customFontSize = prefs(ModulePrefs).getInt("statusbar_power_font_size", 2)

        //Source StatBatteryMeterView
        findClass("com.oplusos.systemui.statusbar.widget.StatBatteryMeterView").hook {
            injectMember {
                method {
                    name = "onFinishInflate"
                }
                afterHook {
                    field { name = "batteryPercentText" }.get(instance).cast<TextView>()?.apply {
                        if (userTypeface) typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }
            injectMember {
                method {
                    name = "updatePercentText"
                }
                afterHook {
                    field { name = "batteryPercentText" }.get(instance).cast<TextView>()?.apply {
                        if (removePercent) text = text.toString().replace("%", "")
                        if (userTypeface && customFontSize != 0) setTextSize(
                            TypedValue.COMPLEX_UNIT_DIP,
                            customFontSize.toFloat() * 2
                        )
                    }
                }
            }
        }
    }
}