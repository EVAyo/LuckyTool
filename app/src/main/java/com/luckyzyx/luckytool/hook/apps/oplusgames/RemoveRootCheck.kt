package com.luckyzyx.luckytool.hook.apps.oplusgames

import android.os.Bundle
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringType

class RemoveRootCheck : YukiBaseHooker() {
    override fun onHook() {
        //Source COSASDKManager
        //Search -> dynamic_feature_cool_ex / getSupportCoolEx bundle -> Method
        //isSafe:null; -> isSafe:0
        searchClass {
            from("com.oplus.x", "com.oplus.f", "com.oplus.a0", "yp", "hr", "br", "ir").absolute()
            field { type = StringType }.count(5..6)
            field { type = BooleanType }.count(2..4)
            field { type = IntType }.count(1..2)
            method {
                emptyParam()
                name = "clear"
            }.count(1)
            method {
                emptyParam()
                returnType = BundleClass
            }.count(1)
        }.get()?.hook {
            injectMember {
                method {
                    emptyParam()
                    returnType = BundleClass
                }
                afterHook {
                    result<Bundle>()?.putInt("isSafe", 0)
                }
            }
        } ?: loggerD(msg = "$packageName\nError -> RemoveRootCheck")
    }
}
