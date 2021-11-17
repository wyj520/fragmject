package com.example.fragment.project.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.fragment.library.base.adapter.BaseAdapter
import com.example.fragment.library.base.adapter.BaseViewPagerAdapter
import com.example.fragment.library.base.utils.BannerHelper
import com.example.fragment.library.common.constant.Keys
import com.example.fragment.library.common.fragment.RouterFragment
import com.example.fragment.library.common.utils.WanHelper
import com.example.fragment.module.user.fragment.UserFragment
import com.example.fragment.module.wan.fragment.*
import com.example.fragment.project.R
import com.example.fragment.project.adapter.HotKeyAdapter
import com.example.fragment.project.databinding.FragmentMainBinding
import com.example.fragment.project.model.MainViewModel
import com.google.android.material.tabs.TabLayout

class MainFragment : RouterFragment() {

    private lateinit var bannerHelper: BannerHelper
    private val hotKeyAdapter = HotKeyAdapter()

    private val tabTexts = arrayOf("首页", "导航", "问答", "项目", "我的")
    private val tabDrawable = intArrayOf(
        R.drawable.ic_bottom_bar_home,
        R.drawable.ic_bottom_bar_navigation,
        R.drawable.ic_bottom_bar_faq,
        R.drawable.ic_bottom_bar_system,
        R.drawable.ic_bottom_bar_project
    )
    private val fragments = arrayListOf(
        HomeFragment.newInstance(),
        LinkFragment.newInstance(),
        FAQFragment.newInstance(),
        ProjectListFragment.newInstance(),
        UserFragment.newInstance()
    )

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.wan.setOnClickListener { baseActivity.navigation(R.id.action_main_to_user) }
        binding.search.setOnClickListener { search() }
        hotKeyAdapter.setOnItemClickListener(object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(holder: BaseAdapter.ViewBindHolder, position: Int) {
                search()
            }
        })
        bannerHelper = BannerHelper(binding.hotKey, RecyclerView.VERTICAL)
        binding.hotKey.adapter = hotKeyAdapter
        viewModel.hotKeyResult.observe(viewLifecycleOwner) { result ->
            result.data?.apply {
                if (result.errorCode == "0") {
                    hotKeyAdapter.setNewData(this)
                    WanHelper.setHotKey(this)
                    bannerHelper.startTimerTask()
                }
                if (result.errorCode.isNotBlank() && result.errorMsg.isNotBlank()) {
                    baseActivity.showTips(result.errorMsg)
                }
            }
        }
        viewModel.getHotKey()

        binding.viewpager.offscreenPageLimit = 4
        binding.viewpager.adapter = BaseViewPagerAdapter(childFragmentManager, fragments)
        binding.tabBar.setupWithViewPager(binding.viewpager)
        binding.tabBar.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.apply {
                    val icon = findViewById<ImageView>(R.id.iv_tab_icon)
                    val text = findViewById<TextView>(R.id.tv_tab_name)
                    icon.setColorFilter(ContextCompat.getColor(icon.context, R.color.text_fff))
                    text.setTextColor(ContextCompat.getColor(text.context, R.color.text_fff))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.apply {
                    val icon = findViewById<ImageView>(R.id.iv_tab_icon)
                    val text = findViewById<TextView>(R.id.tv_tab_name)
                    icon.setColorFilter(ContextCompat.getColor(icon.context, R.color.gray_alpha))
                    text.setTextColor(ContextCompat.getColor(text.context, R.color.gray_alpha))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.tabBar.removeAllTabs()
        for (i in fragments.indices) {
            val layoutInflater = LayoutInflater.from(binding.root.context)
            val tabView: View = layoutInflater.inflate(R.layout.item_tab_main, null)
            val imgTab = tabView.findViewById<ImageView>(R.id.iv_tab_icon)
            val txtTab = tabView.findViewById<TextView>(R.id.tv_tab_name)
            imgTab.setImageDrawable(ContextCompat.getDrawable(imgTab.context, tabDrawable[i]))
            imgTab.setColorFilter(ContextCompat.getColor(imgTab.context, R.color.gray_alpha))
            txtTab.setTextColor(ContextCompat.getColor(txtTab.context, R.color.gray_alpha))
            txtTab.text = tabTexts[i]
            val tab = binding.tabBar.newTab()
            tab.customView = tabView
            binding.tabBar.addTab(tab)
        }
        binding.viewpager.currentItem = savedInstanceState?.getInt("MAIN_CURRENT_POSITION") ?: 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("MAIN_CURRENT_POSITION", binding.viewpager.currentItem)
    }

    override fun onResume() {
        super.onResume()
        bannerHelper.startTimerTask()
    }

    override fun onPause() {
        super.onPause()
        bannerHelper.stopTimerTask()
    }

    private fun search() {
        val title = hotKeyAdapter.getItem(bannerHelper.findLastVisibleItemPosition()).name
        val args = Bundle()
        args.putString(Keys.TITLE, title)
//        baseActivity.navigation(R.id.action_main_to_search, args)
    }

}