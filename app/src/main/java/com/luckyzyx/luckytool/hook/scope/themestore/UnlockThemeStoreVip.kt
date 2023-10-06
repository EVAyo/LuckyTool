package com.luckyzyx.luckytool.hook.scope.themestore

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object UnlockThemeStoreVip : YukiBaseHooker() {
    override fun onHook() {
        //Source VipUserDto
        "com.oppo.cdo.card.theme.dto.vip.VipUserDto".toClass().apply {
            method { name = "getVipStatus" }.hook {
                replaceTo(1)
            }
            method { name = "getVipDays" }.hook {
                replaceTo(999)
            }
        }
        //Source PublishProductItemDto
        "com.oppo.cdo.theme.domain.dto.response.PublishProductItemDto".toClass().apply {
            method { name = "getPrice" }.hook {
                replaceTo(0.0)
            }
            method { name = "getIsVipAvailable" }.hook {
                replaceTo(1)
            }
        }
    }
}