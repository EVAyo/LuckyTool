package com.luckyzyx.luckytool.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object RemoveWiFiDataInout : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.statusbar.OplusStatusBarWifiView",
            "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarWifiViewExImpl"
        ).hook {
            injectMember {
                method { name = "initViewState" }
                afterHook {
                    field { name = "mWifiActivity" }.get(instance).cast<View>()?.isVisible = false
                }
            }
            injectMember {
                method { name = "updateState" }
                afterHook {
                    field { name = "mWifiActivity" }.get(instance).cast<View>()?.isVisible = false
                }
            }
        }
    }
}