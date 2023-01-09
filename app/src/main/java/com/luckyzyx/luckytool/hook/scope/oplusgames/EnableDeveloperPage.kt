package com.luckyzyx.luckytool.hook.scope.oplusgames

import android.app.Activity
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object EnableDeveloperPage : YukiBaseHooker() {
    override fun onHook() {
        //Source GameDevelopOptionsActivity
        findClass("business.compact.activity.GameDevelopOptionsActivity").hook {
            injectMember {
                method {
                    name = "onCreate"
                    paramCount = 1
                }
                beforeHook {
                    instance<Activity>().intent.putExtra("gameDevelopOptions",instanceClass.simpleName)
                }
            }
        }
    }
}