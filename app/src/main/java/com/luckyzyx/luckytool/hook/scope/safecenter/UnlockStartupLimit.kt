package com.luckyzyx.luckytool.hook.scope.safecenter

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType

object UnlockStartupLimit : YukiBaseHooker() {
    override fun onHook() {
        //Source StratupManager
        //Search -> update max allow count -5 -> method,-1 -> field
        VariousClass(
            "com.oplus.safecenter.startupapp.a"
        ).hook {
            injectMember {
                method {
                    name = "b"
                    param(ContextClass)
                    paramCount = 1
                }
                afterHook {
                    field {
                        name = "d"
                        type = IntType
                    }.get().set(10000)
                }
            }
        }
    }
}

object UnlockStartupLimitV11 : YukiBaseHooker() {
    override fun onHook() {
        //Source StratupManager
        //Search -> auto_start_max_allow_count -2 -> method,+2 -> field
        VariousClass(
            "com.coloros.safecenter.startupapp.b"
        ).hook {
            injectMember {
                method {
                    name = "c"
                    param(ContextClass)
                    paramCount = 1
                }
                afterHook {
                    field {
                        name = "b"
                        type = IntType
                    }.get().set(10000)
                }
            }
        }
    }
}