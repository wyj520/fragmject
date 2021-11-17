package com.example.fragment.module.wan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fragment.library.base.adapter.BaseViewPagerAdapter
import com.example.fragment.library.common.fragment.RouterFragment
import com.example.fragment.module.wan.R
import com.example.fragment.module.wan.databinding.FragmentNavigationBinding

class NavigationFragment : RouterFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): NavigationFragment {
            return NavigationFragment()
        }
    }

    private val tabTexts = arrayOf("广场 • 近期分享", "导航")
    private val fragments = arrayListOf(
        SquareFragment.newInstance(),
        LinkFragment.newInstance()
    )

    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewpager.adapter = BaseViewPagerAdapter(childFragmentManager, fragments)
        binding.tabBar.setupWithViewPager(binding.viewpager)
        binding.tabBar.removeAllTabs()
        for (i in tabTexts.indices) {
            val layoutInflater = LayoutInflater.from(binding.root.context)
            val tabView: View = layoutInflater.inflate(R.layout.tab_item_top, null)
            tabView.findViewById<TextView>(R.id.tv_tab).text = tabTexts[i]
            val tab = binding.tabBar.newTab()
            tab.customView = tabView
            binding.tabBar.addTab(tab)
        }
        binding.viewpager.currentItem =
            savedInstanceState?.getInt("NAVIGATION_CURRENT_POSITION") ?: 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("NAVIGATION_CURRENT_POSITION", binding.viewpager.currentItem)
    }

}