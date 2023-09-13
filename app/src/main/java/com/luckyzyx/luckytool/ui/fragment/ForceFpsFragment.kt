package com.luckyzyx.luckytool.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.drake.net.utils.scopeLife
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.luckyzyx.luckytool.IRefreshRateController
import com.luckyzyx.luckytool.databinding.FragmentFpsBinding
import com.luckyzyx.luckytool.ui.service.RefreshRateControllerService
import com.luckyzyx.luckytool.utils.DisplayMode
import com.luckyzyx.luckytool.utils.SettingsPrefs
import com.luckyzyx.luckytool.utils.bindRootService
import com.luckyzyx.luckytool.utils.getBoolean
import com.luckyzyx.luckytool.utils.getFpsMode1
import com.luckyzyx.luckytool.utils.getInt
import com.luckyzyx.luckytool.utils.putBoolean
import com.luckyzyx.luckytool.utils.putInt

class ForceFpsFragment : Fragment() {

    private lateinit var binding: FragmentFpsBinding
    private var controller: IRefreshRateController? = null

    private var allData = java.util.ArrayList<Any?>()
    private var idData = ArrayList<Int>()
    private var fpsData = ArrayList<String>()
    private var fpsAdapter: ArrayAdapter<String>? = null

    private var fpsMode = 1
    private var fpsAutostart = false
    private var currentFps = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFpsBinding.inflate(inflater)
        return binding.root
    }

    fun init(context: Context) {
        scopeLife {
            binding.swipeRefreshLayout.isRefreshing = true
            binding.swipeRefreshLayout.setOnRefreshListener { init(context) }
            clearAllData()
            fpsMode = context.getInt(SettingsPrefs, "fps_mode", 1)
            allData = if (fpsMode == 1) getFpsMode1()
            else controller?.supportModes as java.util.ArrayList<Any?>
            initFpsData(allData)
            currentFps = context.getInt(SettingsPrefs, "current_fps", -1)
            fpsAutostart = context.getBoolean(SettingsPrefs, "fps_autostart", false)
            fpsAdapter = ArrayAdapter(
                context, android.R.layout.simple_list_item_single_choice, fpsData
            )
            val isUnsupport = allData.isEmpty()
            val fpsSelfStart = binding.fpsSelfStart.apply {
                isChecked = fpsAutostart
                isEnabled = !isUnsupport && currentFps != -1
                setOnCheckedChangeListener { _, isChecked ->
                    context.updateAutoStatus(isChecked)
                }
            }
            binding.fpsNodataView.isVisible = isUnsupport
            binding.fpsList.apply {
                isVisible = !isUnsupport
                choiceMode = ListView.CHOICE_MODE_SINGLE
                if (!isUnsupport) adapter = fpsAdapter
                val curFpsId = idData.indexOf(currentFps)
                if (curFpsId != -1) setItemChecked(curFpsId, currentFps != -1)
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    fpsSelfStart.isEnabled = true
                    val id = idData[position]
                    context.updateRefreshRateMode(id)
                    if (fpsMode == 2) controller?.setRefreshRateMode(id)
                }
            }
            binding.fpsMode1.apply {
                isEnabled = true
                if (fpsMode == 1) toggle()
                setOnCheckedChangeListener { btn, _ ->
                    if (btn.isPressed.not()) return@setOnCheckedChangeListener
                    fpsSelfStart.isChecked = false
                    fpsSelfStart.isEnabled = false
                    context.changeFpsMode(1)
                }
            }
            binding.fpsMode2.apply {
                isEnabled = true
                if (fpsMode == 2) toggle()
                setOnCheckedChangeListener { btn, _ ->
                    if (btn.isPressed.not()) return@setOnCheckedChangeListener
                    fpsSelfStart.isChecked = false
                    fpsSelfStart.isEnabled = false
                    context.changeFpsMode(2)
                }
            }
            binding.fpsShow.apply {
                isEnabled = controller != null
                isChecked = controller?.refreshRateDisplay == true
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) controller?.refreshRateDisplay = isChecked
                }
            }
            binding.fpsRecover.apply {
                isEnabled = controller != null
                isVisible = fpsMode == 2
                setOnClickListener {
                    fpsSelfStart.isChecked = false
                    fpsSelfStart.isEnabled = false
                    context.changeFpsMode(-1)
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (controller == null) requireActivity().bindRootService(
            RefreshRateControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IRefreshRateController.Stub.asInterface(iBinder)
                init(requireActivity())
            })
        else init(requireActivity())
    }

    private fun Context.changeFpsMode(mode: Int) {
        if (mode != -1) updateFpsMode(mode)
        updateRefreshRateMode(-1)
        controller?.resetRefreshRateMode()
        init(this)
    }

    private fun clearAllData() {
        allData.clear()
        idData.clear()
        fpsData.clear()
    }

    private fun initFpsData(allData: java.util.ArrayList<Any?>) {
        allData.forEachIndexed { index, any ->
            val fps = any?.let { (it as DisplayMode) } ?: return@forEachIndexed
            idData.add(index)
            fpsData.add("${fps.id}   ${fps.width} x ${fps.height}   ${fps.refreshRate}")
        }
    }

    private fun Context.updateFpsMode(mode: Int) {
        putInt(SettingsPrefs, "fps_mode", mode)
        dataChannel("com.android.systemui").put("fps_mode", mode)
    }

    private fun Context.updateAutoStatus(isChecked: Boolean) {
        putBoolean(SettingsPrefs, "fps_autostart", isChecked)
        dataChannel("com.android.systemui").put("fps_autostart", isChecked)
    }

    private fun Context.updateRefreshRateMode(mode: Int) {
        putInt(SettingsPrefs, "current_fps", mode)
        dataChannel("com.android.systemui").put("current_fps", mode)
    }
}