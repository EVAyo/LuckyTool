package com.luckyzyx.luckytool.hook.apps.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringType

class EnableAdrenoGpuController : YukiBaseHooker() {
    override fun onHook() {
        //Source GpuSettingHelper
        //Search isSupportGpuControlPanel
        searchClass {
            //714,715,716
            from("fc", "gc", "ec").absolute()
            field {
                type = ListClass
            }.count(5)
            method {
                param(StringType)
            }.count(5)
            method {
                param(StringType)
                returnType = BooleanType
            }.count(4)
            method {
                param(ContextClass, StringType)
            }.count(2)
            method {
                emptyParam()
                returnType = BooleanType
            }.count(3)
        }.get()?.hook {
            injectMember {
                method {
                    emptyParam()
                    returnType(BooleanType).index().last()
                }
                replaceToTrue()
            }
        } ?: loggerD(msg = "$packageName\nError -> EnableAdrenoGpuController")
    }
}