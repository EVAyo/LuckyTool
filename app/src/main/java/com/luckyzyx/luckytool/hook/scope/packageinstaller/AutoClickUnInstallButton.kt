package com.luckyzyx.luckytool.hook.scope.packageinstaller

import android.widget.Button
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object AutoClickUnInstallButton : YukiBaseHooker() {
    override fun onHook() {
        //Source UninstallerActivity
        "com.android.packageinstaller.UninstallerActivity".toClass().apply {
            method {
                name = "showUninstallConfirmation"
                paramCount = 1
            }.hook {
                after {
                    field { name = "mUnInstallButton" }.get(instance).cast<Button>()?.callOnClick()
                }
            }
        }
        //Source InstallAppProgress
        "com.android.packageinstaller.oplus.OPlusUninstallAppProgress".toClass().apply {
            method { name = "initView" }.hook {
                after {
                    field { name = "mOkButton" }.get(instance).cast<Button>()?.callOnClick()
                }
            }
        }
    }
}