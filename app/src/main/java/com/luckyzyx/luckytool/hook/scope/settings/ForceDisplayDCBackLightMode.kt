package com.luckyzyx.luckytool.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object ForceDisplayDCBackLightMode : YukiBaseHooker() {
    override fun onHook() {
//        <string name="settings_dc_backlight_mode_title_new">低亮无频闪护眼</string>
//        <com.oplus.settings.widget.preference.SettingsPreferenceCategory android:key="key_dc_backlight_category">
//        <com.oplus.settings.widget.preference.SettingSwitchPreference android:persistent="false" android:title="@string/settings_dc_backlight_mode_title_new" android:key="key_dc_backlight_mode" android:summary="@string/dc_backlight_mode_summary"/>
//        </com.oplus.settings.widget.preference.SettingsPreferenceCategory>

        //Source DcBackLightModePreferenceController
        findClass("com.oplus.settings.feature.display.controller.DcBackLightModePreferenceController").hook {
            injectMember {
                method { name = "getAvailabilityStatus" }
                replaceTo(0)
            }
        }
    }
}