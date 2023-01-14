package com.luckyzyx.luckytool.hook.scope.systemui

import android.net.TrafficStats
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.GridLayout.LayoutParams
import android.widget.TextView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.data.dp
import com.luckyzyx.luckytool.utils.tools.XposedPrefs
import java.text.DecimalFormat

object NetworkSpeed : YukiBaseHooker() {

    override fun onHook() {
        //Search postUpdateNetworkSpeedDelay
        VariousClass(
            "com.oplusos.systemui.statusbar.controller.NetworkSpeedController",
            "com.oplus.systemui.statusbar.phone.netspeed.OplusNetworkSpeedControllExImpl"
        ).hook {
            injectMember {
                method {
                    name = "postUpdateNetworkSpeedDelay"
                    paramCount = 1
                }
                beforeHook {
                    if (prefs(XposedPrefs).getBoolean("set_network_speed", false) && (args(0).cast<Long>() == 4000L)) {
                        args(0).set(1000L)
                    }
                }
            }
        }
        val isDoubleRow = prefs(XposedPrefs).getBoolean("enable_double_row_network_speed", false)
        val getDoubleSize = prefs(XposedPrefs).getInt("set_network_speed_font_size", 7)
        val getDoublePadding = prefs(XposedPrefs).getInt("set_network_speed_padding_bottom", 2)
        if (!isDoubleRow) return
        //Source NetworkSpeedView
        VariousClass(
            "com.oplusos.systemui.statusbar.widget.NetworkSpeedView",
            "com.oplus.systemui.statusbar.phone.netspeed.widget.NetworkSpeedView"
        ).hook {
            injectMember {
                method { name = "onFinishInflate" }
                afterHook {
                    val mView = instance<FrameLayout>()
                    field { name = "mSpeedNumber" }.get(instance).cast<TextView>()?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, getDoubleSize.toFloat())
                        layoutParams.width = LayoutParams.WRAP_CONTENT
                    }
                    field { name = "mSpeedUnit" }.get(instance).cast<TextView>()?.apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, getDoubleSize.toFloat())
                        layoutParams.width = LayoutParams.WRAP_CONTENT
                    }
                    mView.setPadding(0, 0, 0, getDoublePadding.dp)
                }
            }
            injectMember {
                method {
                    name = "updateNetworkSpeed"
                    paramCount = 2
                }
                beforeHook {
                    instance<FrameLayout>().layoutParams.takeIf { it != null }?.width = LayoutParams.WRAP_CONTENT
                    val mSpeedNumber = field { name = "mSpeedNumber" }.get(instance).cast<TextView>()
                    val mSpeedUnit = field { name = "mSpeedUnit" }.get(instance).cast<TextView>()
                    mSpeedNumber?.text = getTotalUpSpeed()
                    mSpeedUnit?.text = getTotalDownloadSpeed()
                    instance<FrameLayout>().requestLayout()
                    resultNull()
                }
            }
        }
    }

    /** 上次总的上行流量 */
    private var mLastTotalUp: Long = 0L

    /** 上次总的下行流量 */
    private var mLastTotalDown: Long = 0L

    /** 上次总的上行时间戳 */
    private var lastTimeStampTotalUp: Long = 0L

    /** 上次总的下行时间戳 */
    private var lastTimeStampTotalDown: Long = 0L

    //获取总的上行速度
    private fun getTotalUpSpeed(): String {
        //换算后的上行速度
        val totalUpSpeed: Float
        val currentTotalTxBytes = TrafficStats.getTotalTxBytes()

        /** 当前系统时间戳 */
        val nowTimeStampTotalUp = System.currentTimeMillis()

        /** 当前总的上行流量 */
        val mCurrentTotalUp = currentTotalTxBytes - mLastTotalUp

        /** 当前总的间隔时间 */
        val mCurrentIntervals = nowTimeStampTotalUp - lastTimeStampTotalUp

        //计算上传速度
        val bytes = ((mCurrentTotalUp * 1000.0) / (mCurrentIntervals * 1.0)).toFloat()
        if (bytes.isInfinite() || bytes.isNaN()) return "0B/s"
        val unit: String
        if (bytes >= (1024 * 1024)) {
            totalUpSpeed = DecimalFormat("0.0").format(bytes / (1024 * 1024)).toFloat()
            unit = "MB/s"
        } else if (bytes >= 1024) {
            totalUpSpeed = DecimalFormat("0.0").format(bytes / 1024).toFloat()
            unit = "KB/s"
        } else {
            totalUpSpeed = DecimalFormat("0.0").format(bytes).toFloat()
            unit = "B/s"
        }
        //保存当前的流量总和和上次的时间戳
        mLastTotalUp = currentTotalTxBytes
        lastTimeStampTotalUp = nowTimeStampTotalUp
        return "" + totalUpSpeed.toString() + unit
    }

    //获取总的下行速度
    private fun getTotalDownloadSpeed(): String {
        //换算后的下行速度
        val totalDownSpeed: Float
        val currentTotalRxBytes = TrafficStats.getTotalRxBytes()

        /** 当前系统时间戳 */
        val nowTimeStampTotalDown = System.currentTimeMillis()

        /** 当前总的下行流量 */
        val mCurrentTotalDown = currentTotalRxBytes - mLastTotalDown

        /** 当前总的间隔时间 */
        val mCurrentIntervals = nowTimeStampTotalDown - lastTimeStampTotalDown

        //计算下行速度
        val bytes = ((mCurrentTotalDown * 1000.0) / (mCurrentIntervals * 1.0)).toFloat()
        if (bytes.isInfinite() || bytes.isNaN()) return "0B/s"
        val unit: String
        if (bytes >= (1024 * 1024)) {
            totalDownSpeed = DecimalFormat("0.0").format(bytes / (1024 * 1024)).toFloat()
            unit = "MB/s"
        } else if (bytes >= 1024) {
            totalDownSpeed = DecimalFormat("0.0").format(bytes / 1024).toFloat()
            unit = "KB/s"
        } else {
            totalDownSpeed = DecimalFormat("0.0").format(bytes).toFloat()
            unit = "B/s"
        }
        //保存当前的流量总和和上次的时间戳
        mLastTotalDown = currentTotalRxBytes
        lastTimeStampTotalDown = nowTimeStampTotalDown

        return "" + totalDownSpeed.toString() + unit
    }
}