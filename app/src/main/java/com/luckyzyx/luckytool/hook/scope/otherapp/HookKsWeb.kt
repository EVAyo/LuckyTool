package com.luckyzyx.luckytool.hook.scope.otherapp

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.SharedPreferencesClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs

object HookKsWeb : YukiBaseHooker() {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("ksweb_remove_check_license", false)
        if (!isPro) return
        //Source EXTEND TO PRO VERSION / CHECK SERIAL KEY / KSWEB PRO / KSWEB STANDARD
        DexkitUtils.searchDexClass("HookKsWeb", appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(IntType.name)
                        addForType(BooleanType.name)
                        addForType(SharedPreferencesClass.name)
                    }
                    methods {
                        add { paramCount(0);returnType(IntType.name) }
                        add { paramCount(0);returnType(BooleanType.name) }
                        add { paramTypes(IntType.name);returnType(UnitType.name) }
                        add { paramTypes(ContextClass.name);returnType(UnitType.name) }
                    }
                    usingStrings(
                        "EXTEND TO PRO VERSION",
                        "CHECK SERIAL KEY",
                        "KSWEB PRO",
                        "KSWEB STANDARD"
                    )
                }
            }
        }?.firstOrNull()?.className?.toClass()?.apply {
            method { emptyParam();returnType = BooleanType }.hookAll {
                before {
                    field { type = BooleanType }.get(instance).setTrue()
                    field { type = IntType }.get(instance).set(2)
                }
            }
        }
    }
}