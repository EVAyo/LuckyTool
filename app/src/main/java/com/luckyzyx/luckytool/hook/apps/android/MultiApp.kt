package com.luckyzyx.luckytool.hook.apps.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.luckyzyx.luckytool.utils.data.XposedPrefs

class MultiApp : YukiBaseHooker() {
    override fun onHook() {
        findClass("com.oplus.multiapp.OplusMultiAppConfig").hook {
            injectMember {
                method {
                    name = "getAllowedPkgList"
                    returnType = ListClass
                }
                beforeHook {
                    if (prefs(XposedPrefs).getBoolean("multi_app_enable", false)){
                        field {
                            name = "mAllowedPkgList"
                            type = ListClass
                        }.get(instance).set(prefs(XposedPrefs).getStringSet("enabledMulti", HashSet()).toList())
                    }
                }
            }
        }
    }
}