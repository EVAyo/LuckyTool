package com.luckyzyx.luckytool.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

class UnlockTaskLocks : YukiBaseHooker() {
    override fun onHook() {
        //Source OplusLockManager
        findClass("com.oplus.quickstep.applock.OplusLockManager").hook {
            injectMember {
                constructor {
                    paramCount = 1
                }
                afterHook {
                    field {
                        name = "mLockAppLimit"
                    }.get(instance).set(999)
                }
            }
        }
    }
}
class UnlockTaskLocksV11 : YukiBaseHooker() {
    override fun onHook() {
        //Source ColorLockManager
        findClass("com.coloros.quickstep.applock.ColorLockManager").hook {
            injectMember {
                constructor {
                    paramCount = 1
                }
                afterHook {
                    field {
                        name = "mLockAppLimit"
                    }.get(instance).set(999)
                }
            }
        }
    }
}