@file:Suppress("unused")

package com.luckyzyx.luckytool.utils

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.TypedValue
import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.classOf
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.luckyzyx.luckytool.BuildConfig.*
import com.luckyzyx.luckytool.R
import com.simple.spiderman.SpiderMan
import java.io.*


/**
 * try/catch函数
 * @param default 异常返回值
 * @param result 正常回调值
 * @return [T] 发生异常时返回设定值否则返回正常值
 */
inline fun <T> safeOf(default: T, result: () -> T) = try {
    result()
} catch (_: Throwable) {
    default
}

/**
 * 获取ColorOS版本
 * @return [String]
 */
val getColorOSVersion
    get() = safeOf(default = "null") {
        classOf(name = "com.oplus.os.OplusBuild").let {
            it.field { name = "VERSIONS" }.ignoredError().get().array<String>()
                .takeIf { e -> e.isNotEmpty() }
                ?.get(it.method { name = "getOplusOSVERSION" }.ignoredError().get().int() - 1)
        }
    }

/**
 * 获取APP版本/版本号/Commit
 * 写入SP xml文件内
 * @return [String]
 */
fun getAppVersion(context: Context, packName: String, isCommit: Boolean = false): String = safeOf(default = "null") {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(packName, 0)
    val versionName = safeOf(default = "null") { packageInfo.versionName }
    val versionCode = safeOf(default = "null") { packageInfo.longVersionCode }
    if (isCommit) {
        val versionCommit = safeOf(default = "null") {
            packageManager.getApplicationInfo(packName, PackageManager.GET_META_DATA).metaData.getString("versionCommit")
        }
        context.putString(XposedPrefs, packName, versionCommit)
        return "$versionName($versionCode)[$versionCommit]"
    }
    return "$versionName($versionCode)"
}

/**
 * 获取构建版本名/版本号
 * @return [String]
 */
val getBuildVersion get() = "${VERSION_NAME}(${VERSION_CODE})"

/**
 * 查询包名是否存在
 */
fun checkPackName(packageName: String): Boolean {
    val packageManager = AndroidAppHelper.currentApplication().packageManager
    val applist = packageManager.getInstalledPackages(0)
    for (i in applist) {
        if (i.packageName.equals(packageName, ignoreCase = true)) return true
    }
    return false
}

/**
 * 获取已安装APP列表
 */
fun getInstalledApp(context: Context? = null, hasSystem: Boolean = false, size: Int? = null): ArrayList<String> {
    val packageManager = if (context == null) {
        AndroidAppHelper.currentApplication().packageManager
    }else{
        context.packageManager
    }
    val packageInfos = packageManager.getInstalledPackages(0)
    val applist = ArrayList<String>()
    for (info in packageInfos){
        if((info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0){
            applist.add(info.packageName)
        }else if (hasSystem){
            applist.add(info.packageName)
        }
        if (size != null && applist.size == size) break
    }
    return applist
}

/**
 * 检查隐藏活动是否存在
 */
fun checkResolveActivity(context: Context, intent: Intent): Boolean {
    return context.packageManager.resolveActivity(intent, 0) != null
}

/**
 * Toast快捷方法
 * @param name 字符串
 * @param long 显示时长
 * @return [Toast]
 */
internal fun Context.toast(name: String, long: Boolean? = false): Any = if (long == true) {
    Toast.makeText(this, name, Toast.LENGTH_LONG).show()
} else {
    Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
}

/**
 * 获取支持的刷新率
 * @return [List]
 */
fun getFpsMode(context: Context): Array<String> {
    val command =
        "dumpsys display | grep -A 1 'mSupportedModesByDisplay' | tail -1 | tr '}' '\\n' | cut -f2 -d '{' | while read row; do\n" +
        "  if [[ -n \$row ]]; then\n" +
        "    echo \$row | tr ',' '\\n' | while read col; do\n" +
        "      case \$col in\n" +
        "      'width='*)\n" +
        "        echo -n \$(echo \${col:6})\n" +
        "        ;;\n" +
        "      'height='*)\n" +
        "        echo -n x\$(echo \${col:7})\n" +
        "        ;;\n" +
        "      'fps='*)\n" +
        "        echo ' '\$(echo \${col:4} | cut -f1 -d '.')Hz\n" +
        "        ;;\n" +
        "      esac\n" +
        "    done\n" +
        "    echo -e '@'\n" +
        "  fi\n" +
        "done"
    val result = ShellUtils.execCommand(command, true, true).successMsg ?: return arrayOf("获取错误,请勿点击")
    return result.substring(0,result.length - 1).split("@").toMutableList().apply {
        add(context.getString(R.string.Restore_default_refresh_rate))
    }.toTypedArray()
}

/**
 * 跳转工程模式
 */
fun jumpEngineermode() {
    if (checkPackName("com.oppo.engineermode")) {
        ShellUtils.execCommand("am start -n com.oppo.engineermode/.aftersale.AfterSalePage", true)
    }else if (checkPackName("com.oplus.engineermode")) {
        ShellUtils.execCommand("am start -n com.oplus.engineermode/.aftersale.AfterSalePage", true)
    }
}
/**
 * 跳转充电测试
 */
fun jumpBatteryInfo() {
    if (checkPackName("com.oppo.engineermode")) {
        ShellUtils.execCommand("am start -n com.oppo.engineermode/.charge.modeltest.BatteryInfoShow", true)
    }else if (checkPackName("com.oplus.engineermode")) {
        ShellUtils.execCommand("am start -n com.oplus.engineermode/.charge.modeltest.BatteryInfoShow", true)
    }
}
/**
 * 跳转进程管理
 */
fun jumpRunningApp(context: Context){
    val isoppoRunning = Intent().setClassName(
        "com.android.settings",
        "com.coloros.settings.feature.process.RunningApplicationActivity"
    )
    val isoplusRunning = Intent().setClassName(
        "com.android.settings",
        "com.oplus.settings.feature.process.RunningApplicationActivity"
    )
    if (checkResolveActivity(context,isoppoRunning)) {
        ShellUtils.execCommand("am start -n com.android.settings/com.coloros.settings.feature.process.RunningApplicationActivity", true)
    } else if (checkResolveActivity(context,isoplusRunning)) {
        ShellUtils.execCommand("am start -n com.android.settings/com.oplus.settings.feature.process.RunningApplicationActivity", true)
    }
}

/**
 * 显示/隐藏APP图标
 */
fun setDesktopIcon(context: Context,value : Boolean){
    context.packageManager.setComponentEnabledSetting(ComponentName(APPLICATION_ID, "${APPLICATION_ID}.Hide"),
        if (value) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
}

fun dp2px(context: Context,dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

fun sp2px(context: Context,sp: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)

fun copyFile(source: File, target: File) {
    var fis: InputStream? = null
    var fos: OutputStream? = null
    try {
        fis = BufferedInputStream(FileInputStream(source))
        fos = BufferedOutputStream(FileOutputStream(target))
        val buf = ByteArray(4096)
        var i: Int
        while (fis.read(buf).also { i = it } != -1) {
            fos.write(buf, 0, i)
        }
    } catch (e: Exception) {
        SpiderMan.show(e)
    } finally {
        fis?.close()
        fos?.close()
    }
}