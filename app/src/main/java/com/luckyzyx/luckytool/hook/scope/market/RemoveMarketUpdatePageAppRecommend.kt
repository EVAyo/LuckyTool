package com.luckyzyx.luckytool.hook.scope.market

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.luckypray.dexkit.query.matchers.MethodMatcher

object RemoveMarketUpdatePageAppRecommend : YukiBaseHooker() {
    override fun onHook() {
        //Source AppUpdateFragment
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                searchPackages("com.heytap.cdo.client.ui.upgrademgr")
                matcher {
                    addParamType(ListClass.name)
                    returnType(UnitType.name)
                    addInvoke(MethodMatcher().name("notifyDataSetChanged"))
                    addCall {
                        addParamType(ListClass.name)
                        returnType(UnitType.name)
                        usingNumbers(114.0F)
                    }
                }
            }.apply {
                checkDataList("RemoveMarketUpdatePageAppRecommend")
                val member = first()
                member.className.toClass().apply {
                    method { name = member.methodName;param(ListClass) }.hook {
                        before {
                            val list = args().first().list<Any>().toMutableList()
                            list.clear()
                            args().first().set(ArrayList(list))
                        }
                    }
                }
            }
        }
    }
}