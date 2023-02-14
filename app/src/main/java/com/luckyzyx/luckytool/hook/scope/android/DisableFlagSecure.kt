package com.luckyzyx.luckytool.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.luckyzyx.luckytool.utils.tools.ModulePrefs
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.function.BiPredicate

object DisableFlagSecure : YukiBaseHooker() {
    private var deoptimizeMethod: Method? = null
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("disable_flag_secure", false)
        if (!isEnable) return
        //Source A11+ -> WindowState A11- -> WindowManagerService
        findClass("com.android.server.wm.WindowState").hook {
            injectMember {
                method {
                    name = "isSecureLocked"
                }
                replaceToFalse()
            }
        }
        try {
            val m: Method? =
                XposedBridge::class.java.getDeclaredMethod("deoptimizeMethod", Member::class.java)
            deoptimizeMethod = m
        } catch (t: Throwable) {
            loggerD(msg = t.toString())
        }

        try {
            val windowStateAnimator = "com.android.server.wm.WindowStateAnimator".toClassOrNull()
            deoptimizeMethod(windowStateAnimator, "createSurfaceLocked")
            val displayManagerService =
                "com.android.server.display.DisplayManagerService".toClassOrNull()
            deoptimizeMethod(displayManagerService, "setUserPreferredModeForDisplayLocked")
            deoptimizeMethod(displayManagerService, "setUserPreferredDisplayModeInternal")
            val listener =
                "com.android.server.wm.InsetsPolicy\$InsetsPolicyAnimationControlListener".toClassOrNull()
            if (listener != null) for (m in listener.declaredConstructors) {
                deoptimizeMethod?.invoke(null, m)
            }
            val insetsPolicy = "com.android.server.wm.InsetsPolicy".toClassOrNull()
            deoptimizeMethod(insetsPolicy, "startAnimation")
            deoptimizeMethod(insetsPolicy, "controlAnimationUnchecked")
            for (i in 0..19) {
                val c =
                    "com.android.server.wm.DisplayContent$\$ExternalSyntheticLambda$i".toClassOrNull()
                if (c != null && BiPredicate::class.java.isAssignableFrom(c)) {
                    deoptimizeMethod(c, "test")
                }
            }
        } catch (e: java.lang.Exception) {
            loggerD(msg = e.toString())
        }
    }

    private fun deoptimizeMethod(cls: Class<*>?, method: String) {
        try {
            cls?.declaredMethods?.forEach {
                if (deoptimizeMethod != null && it.name.equals(method)) {
                    deoptimizeMethod?.invoke(null, it)
                }
            }
        } catch (e: java.lang.Exception) {
            loggerD(msg = e.toString())
        }
    }
}