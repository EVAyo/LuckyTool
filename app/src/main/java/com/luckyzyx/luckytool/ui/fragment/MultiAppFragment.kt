package com.luckyzyx.luckytool.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.ArraySet
import android.view.*
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.luckyzyx.luckytool.utils.jumpMultiApp
import com.luckyzyx.luckytool.utils.putBoolean
import com.luckyzyx.luckytool.utils.putStringSet
import rikka.core.util.ResourceUtils

class MultiAppFragment : Fragment() {

    private lateinit var binding: FragmentApplistFunctionLayoutBinding
    private var appListAllDatas = ArrayList<AppInfo>()
    private var multiAppAdapter: MultiAppAdapter? = null
    private var isShowSystemApp = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentApplistFunctionLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.enableSwitch.apply {
            text = getString(R.string.multi_app_enable)
            isChecked = context.getBoolean(ModulePrefs, "multi_app_enable", false)
            setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    context.putBoolean(ModulePrefs, "multi_app_enable", isChecked)
                    requireActivity().dataChannel("android").put("multi_app_enable", isChecked)
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
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    multiAppAdapter?.getFilter?.filter(s.toString())
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

    private fun loadData() {
        isShowSystemApp =
            requireActivity().getBoolean(ModulePrefs, "show_system_app_multi_app", false)
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchViewLayout.isEnabled = false
        binding.searchView.text = null
        appListAllDatas.clear()
        val enableData =
            requireActivity().getStringSet(ModulePrefs, "multi_app_custom_list", ArraySet())
        scopeLife {
            withIO {
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
            multiAppAdapter = MultiAppAdapter(requireActivity(), appListAllDatas, enableData)
            binding.recyclerView.apply {
                adapter = multiAppAdapter
                layoutManager = LinearLayoutManager(context)
            }
            binding.swipeRefreshLayout.isRefreshing = false
            binding.searchViewLayout.isEnabled = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_list_menu, menu)
        menu.findItem(R.id.show_system_app).isChecked = isShowSystemApp
        menu.add(0, 1, 0, getString(R.string.common_words_open)).apply {
            setIcon(R.drawable.baseline_open_in_new_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ResourceUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) jumpMultiApp(requireActivity())
        if (item.itemId == R.id.show_system_app) {
            item.isChecked = !item.isChecked
            isShowSystemApp = item.isChecked
            requireActivity().putBoolean(
                ModulePrefs, "show_system_app_multi_app", isShowSystemApp
            )
            loadData()
        }
        return super.onOptionsItemSelected(item)
    }
}

class MultiAppAdapter(
    val context: Context, datas: ArrayList<AppInfo>, enableData: Set<String>?
) : RecyclerView.Adapter<MultiAppAdapter.ViewHolder>() {

    private var allDatas = ArrayList<AppInfo>()
    private var filterDatas = ArrayList<AppInfo>()
    private var enabledMulti = ArrayList<String>()
    private var sortData = ArrayList<AppInfo>()

    init {
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
        val binding = LayoutAppinfoSwitchItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
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
                        if (it.appName.toString().lowercase().contains(
                                constraint.toString().lowercase()
                            ) || it.packName.lowercase().contains(constraint.toString().lowercase())
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
        context.putStringSet(ModulePrefs, "multi_app_custom_list", enabledMulti.toSet())
        context.dataChannel("android").put("multi_app_custom_list", enabledMulti.toSet())
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