package com.luckyzyx.luckytool.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.ArraySet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withIO
import com.google.android.material.materialswitch.MaterialSwitch
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.databinding.FragmentApplistFunctionLayoutBinding
import com.luckyzyx.luckytool.databinding.LayoutAppinfoSwitchItemBinding
import com.luckyzyx.luckytool.utils.AppInfo
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.PackageUtils
import com.luckyzyx.luckytool.utils.getBoolean
import com.luckyzyx.luckytool.utils.getStringSet
import com.luckyzyx.luckytool.utils.putBoolean
import com.luckyzyx.luckytool.utils.putStringSet
import com.luckyzyx.luckytool.utils.setupMenuProvider

class ZoomWindowFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentApplistFunctionLayoutBinding
    private var appListAllDatas = ArrayList<AppInfo>()
    private var zoomWindowAdapter: ZoomWindowAdapter? = null
    private var isShowSystemApp = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setupMenuProvider(this)
        isShowSystemApp =
            requireActivity().getBoolean(ModulePrefs, "show_system_app_zoom_window", false)
        binding = FragmentApplistFunctionLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.enableSwitch.apply {
            text = context.getString(R.string.enable_zoom_window)
            isChecked = context.getBoolean(ModulePrefs, "enable_zoom_window", false)
            setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    context.putBoolean(ModulePrefs, "enable_zoom_window", isChecked)
                    requireActivity().dataChannel("android").put("enable_zoom_window", isChecked)
                }
            }
        }

        binding.searchViewLayout.apply {
            hint = "Name / PackageName"
            isHintEnabled = true
            isHintAnimationEnabled = true
        }
        binding.searchView.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    zoomWindowAdapter?.getFilter?.filter(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                loadData()
            }
        }

        if (appListAllDatas.isEmpty()) loadData()
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        scopeLife {
            binding.swipeRefreshLayout.isRefreshing = true
            binding.searchViewLayout.isEnabled = false
            binding.searchView.text = null
            val enableData =
                requireActivity().getStringSet(ModulePrefs, "zoom_window_support_list", ArraySet())
            withIO {
                appListAllDatas.clear()
                val packageManager = requireActivity().packageManager
                val appinfos = PackageUtils(packageManager).getInstalledApplications(0)
                for (i in appinfos) {
                    if (i.flags and ApplicationInfo.FLAG_SYSTEM == 1 && !isShowSystemApp) continue
                    appListAllDatas.add(
                        AppInfo(
                            i.loadIcon(packageManager),
                            i.loadLabel(packageManager),
                            i.packageName,
                        )
                    )
                }
            }
            zoomWindowAdapter = ZoomWindowAdapter(requireActivity(), appListAllDatas, enableData)
            binding.recyclerView.apply {
                adapter = zoomWindowAdapter
                layoutManager = LinearLayoutManager(context)
            }
            binding.swipeRefreshLayout.isRefreshing = false
            binding.searchViewLayout.isEnabled = true
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_list_menu, menu)
        menu.findItem(R.id.show_system_app).isChecked = isShowSystemApp
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.show_system_app) {
            menuItem.isChecked = !menuItem.isChecked
            isShowSystemApp = menuItem.isChecked
            requireActivity().putBoolean(
                ModulePrefs, "show_system_app_zoom_window", isShowSystemApp
            )
            loadData()
        }
        return true
    }
}

class ZoomWindowAdapter(
    val context: Context,
    datas: ArrayList<AppInfo>,
    enableData: Set<String>?
) :
    RecyclerView.Adapter<ZoomWindowAdapter.ViewHolder>() {

    private var allDatas = ArrayList<AppInfo>()
    private var filterDatas = ArrayList<AppInfo>()
    private var enabledMulti = ArrayList<String>()
    private var sortData = ArrayList<AppInfo>()
    private var hasPermissions = true

    init {
        if (datas.size <= 1) hasPermissions = false
        allDatas = datas
        enableData?.forEach { enabledMulti.add(it) }
        sortDatas()
        filterDatas = datas
    }

    private fun sortDatas() {
        allDatas.forEach { its ->
            if (enabledMulti.contains(its.packName)) sortData.add(0, its)
        }
        enabledMulti.clear()
        sortData.forEach {
            enabledMulti.add(it.packName)
        }
        saveEnableList()
        sortData.forEach {
            allDatas.remove(it)
            allDatas.add(0, it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutAppinfoSwitchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appIcon.setImageDrawable(filterDatas[position].appIcon)
        holder.appName.text = filterDatas[position].appName
        holder.packName.text = filterDatas[position].packName
        holder.appInfoView.setOnClickListener(null)
        holder.switchview.setOnCheckedChangeListener(null)

        holder.switchview.isChecked = enabledMulti.contains(filterDatas[position].packName)
        holder.appInfoView.setOnClickListener {
            holder.switchview.isChecked = !holder.switchview.isChecked
        }
        holder.switchview.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enabledMulti.add(filterDatas[position].packName)
            } else {
                enabledMulti.remove(filterDatas[position].packName)
            }
            saveEnableList()
        }
    }

    override fun getItemCount(): Int {
        return filterDatas.size
    }

    val getFilter
        get() = object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                filterDatas = if (constraint.isBlank()) {
                    allDatas
                } else {
                    val filterlist = ArrayList<AppInfo>()
                    allDatas.forEach {
                        if (
                            it.appName.toString().lowercase()
                                .contains(constraint.toString().lowercase()) ||
                            it.packName.lowercase().contains(constraint.toString().lowercase())
                        ) {
                            filterlist.add(it)
                        }
                    }
                    filterlist
                }
                val filterResults = FilterResults()
                filterResults.values = filterDatas
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filterDatas = results?.values as ArrayList<AppInfo>
                refreshDatas()
            }
        }

    private fun saveEnableList() {
        if (!hasPermissions) return
        context.putStringSet(ModulePrefs, "zoom_window_support_list", enabledMulti.toSet())
        context.dataChannel("android").put("zoom_window_support_list", enabledMulti.toSet())
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshDatas() {
        notifyDataSetChanged()
    }

    class ViewHolder(binding: LayoutAppinfoSwitchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val appInfoView: ConstraintLayout = binding.root
        val appIcon: ImageView = binding.appIcon
        val appName: TextView = binding.appName
        val packName: TextView = binding.packName
        val switchview: MaterialSwitch = binding.switchview
    }
}