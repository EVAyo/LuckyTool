package com.luckyzyx.luckytool.hook.scope.systemui

import android.content.Context
import android.text.TextUtils
import android.util.LayoutDirection
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.luckyzyx.luckytool.hook.utils.sysui.LunarHelperUtils
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.getScreenOrientation
import java.util.Locale
import kotlin.math.abs

@Suppress("LocalVariableName", "DiscouragedApi")
object ControlCenterDateStyle : YukiBaseHooker() {
    override fun onHook() {
        var removeComma = prefs(ModulePrefs).getBoolean("remove_control_center_date_comma", false)
        dataChannel.wait<Boolean>("remove_control_center_date_comma") { removeComma = it }
        var showLunar =
            prefs(ModulePrefs).getBoolean("statusbar_control_center_date_show_lunar", false)
        dataChannel.wait<Boolean>("statusbar_control_center_date_show_lunar") { showLunar = it }
        var fixWidth =
            prefs(ModulePrefs).getBoolean("statusbar_control_center_date_fix_width", false)
        dataChannel.wait<Boolean>("statusbar_control_center_date_fix_width") { fixWidth = it }

        var fixLunar =
            prefs(ModulePrefs).getString("statusbar_control_center_date_fix_lunar_horizontal", "0")
        dataChannel.wait<String>("statusbar_control_center_date_fix_lunar_horizontal") {
            fixLunar = it
        }

        //Source WeatherInfoParseHelper -> cn_comma
        VariousClass(
            "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper", //C13
            "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper" //C14
        ).toClass().apply {
            method {
                name = "getChineseDateInfo"
                paramCount = 2
            }.hook {
                after {
                    if (removeComma) result = result<String>()?.replace("，", " ")
                    if (showLunar) {
                        val context = args().last().cast<Context>() ?: return@after
                        val lunarInstance = LunarHelperUtils(appClassLoader).buildInstance(context)
                        val lunarDate = LunarHelperUtils(appClassLoader).getDateToString(
                            lunarInstance, System.currentTimeMillis()
                        ).let {
                            if ((it.isNullOrBlank()) || (it.length < 8)) ""
                            else " " + it.substring(4, it.length)
                        }
                        result = result<String>() + lunarDate
                    }
                }
            }
        }

        if (SDK < A13) return
        var translationX = 0
        //Source OplusQSFooterImpl
        VariousClass(
            "com.oplusos.systemui.qs.OplusQSFooterImpl", //C13
            "com.oplus.systemui.qs.OplusQSFooterImpl" //C14
        ).toClass().apply {
            if (hasMethod { name = "updateQsDateView" }.not()) return@apply
            method { name = "updateQsDateView" }.hook {
                after {
                    val mTmpConstraintSet =
                        field { name = "mTmpConstraintSet" }.get(instance).any()
                            ?: return@after
                    val mClockView = field { name = "mClockView" }.get(instance).cast<TextView>()
                        ?: return@after
                    val mQsDateView = field { name = "mQsDateView" }.get(instance).cast<TextView>()
                        ?: return@after

                    if (fixWidth) mTmpConstraintSet.current().method {
                        name = "constrainWidth"
                    }.call(mQsDateView.id, ConstraintLayout.LayoutParams.WRAP_CONTENT)

                    if (showLunar && (fixLunar != "0")) {
                        val res = instance<ViewGroup>().resources
                        //162dp
                        val qs_footer_date_width = res.getDimensionPixelSize(
                            res.getIdentifier(
                                "qs_footer_date_width", "dimen",
                                ControlCenterDateStyle.packageName
                            )
                        )
                        //10dp
                        val qs_footer_date_margin_start = res.getDimensionPixelSize(
                            res.getIdentifier(
                                "qs_footer_date_margin_start", "dimen",
                                ControlCenterDateStyle.packageName
                            )
                        )
                        //51dp
                        val qs_footer_date_expand_translation_y = res.getDimensionPixelSize(
                            res.getIdentifier(
                                "qs_footer_date_expand_translation_y", "dimen",
                                ControlCenterDateStyle.packageName
                            )
                        )
                        val isRtl =
                            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL
                        val width = mClockView.width + qs_footer_date_margin_start
                        if (abs(translationX) < abs(width)) translationX =
                            if (!isRtl) (-width) else width
                        val translationY = qs_footer_date_expand_translation_y / 2

                        getScreenOrientation(res) {
                            if (it) return@getScreenOrientation
                            if (translationX == 0 || translationY == 0) return@getScreenOrientation

                            when (fixLunar) {
                                "1" -> mTmpConstraintSet.current().method {
                                    name = "constrainWidth"
                                }.call(mQsDateView.id, qs_footer_date_width * 2)

                                "2" -> {
                                    mTmpConstraintSet.current().method {
                                        name = "setTranslationX"
                                    }.call(mQsDateView.id, translationX.toFloat())
                                    mTmpConstraintSet.current().method {
                                        name = "setTranslationY"
                                    }.call(mQsDateView.id, translationY.toFloat())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}