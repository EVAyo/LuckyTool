package com.luckyzyx.luckytool.hook.scope.gallery

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs

object HookFunctionManager : YukiBaseHooker() {
    override fun onHook() {
        //姜文电影滤镜
        val jangWen = prefs(ModulePrefs).getBoolean("enable_gallery_jiangwen_filter", false)

        //Source FunctionSwitchManager
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(MapClass.name)
                    }
                    methods {
                        add {
                            paramTypes(StringClass.name)
                            returnType(BooleanType.name)
                            usingStrings("FunctionSwitchManager", "getGroupName", "spKey")
                        }
                        add {
                            paramCount(1..5)
                            returnType(UnitType.name)
                        }
                    }
                    usingStrings("FunctionSwitchManager")
                }
            }.apply {
                checkDataList("HookFunctionManager")
                val member = first()
                member.name.toClass().apply {
                    method {
                        param(StringClass)
                        returnType(BooleanType)
                    }.hook {
                        after {
                            when (args().first().string()) {
                                //姜文电影滤镜
                                "pref_jiangwen_filter_enable" -> if (jangWen) resultTrue()
                            }
                        }
                    }
                }
            }
        }
    }
}