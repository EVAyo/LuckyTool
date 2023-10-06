package com.luckyzyx.luckytool.hook.scope.android

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.luckyzyx.luckytool.hook.utils.PowerProfileUtils
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.safeOfNull
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

object FixBatteryHealthDataDisplay : YukiBaseHooker() {
    val isEnable = prefs(ModulePrefs).getBoolean("fix_battery_health_data_display", false)
    override fun onHook() {
        if (SDK < A13) return

        //Source BatteryServiceExtImpl -> GuardElfThermalControl
        "com.android.server.BatteryServiceExtImpl\$GuardElfThermalControl".toClass().apply {
            method { name = "getUIsohValue" }.hook {
                after {
                    if (!isEnable) return@after
                    val curValue = safeOfNull {
                        BufferedReader(FileReader("/sys/class/oplus_chg/battery/battery_fcc")).readLine()
                            .trim { it <= ' ' }.toFloatOrNull()
                    }
                    val extIns = field {
                        type = "com.android.server.BatteryServiceExtImpl".toClass().javaClass
                    }.get(instance).any()
                    val context = extIns?.current()?.field { type = ContextClass }?.cast<Context>()
                    val powerIns = PowerProfileUtils(appClassLoader).buildInstance(context)
                    val designValue = PowerProfileUtils(appClassLoader).getBatteryCapacity(powerIns)
                    result = if (curValue == null || designValue == null) -1
                    else (curValue / designValue * 100.0F).roundToInt()
                }
            }
        }
    }
}