package com.luckyzyx.luckytool.hook.apps.battery

import android.app.NotificationManager
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringType

class RemoveAppHighBatteryConsumptionWarning : YukiBaseHooker() {
    override fun onHook() {
        // Source NotifyUtil
        // Search power_consumption_optimization_title / pco_notification_text / String \n String
        searchClass {
            from("com.oplus.a.g", "c4", "a4", "i5").absolute()
            constructor {
                paramCount = 1
            }.count(1)
            field {
                type = NotificationManager::class.java
            }.count(1)
            field {
                type = HandlerClass
            }.count(1)
            method {
                param(StringType, BooleanType)
                paramCount = 2
            }.count(4)
        }.get()?.hook {
            injectMember {
                method {
                    param(StringType, BooleanType).index(0)
                }
                intercept()
            }
            injectMember {
                method {
                    param(StringType, BooleanType).index(1)
                }
                intercept()
            }
            injectMember {
                method {
                    param(StringType, BooleanType).index(2)
                }
                intercept()
            }
            injectMember {
                method {
                    param(StringType, BooleanType).index(3)
                }
                intercept()
            }
        } ?: loggerD(msg = "$packageName\nError -> RemoveAppHighBatteryConsumptionWarning")
    }
}