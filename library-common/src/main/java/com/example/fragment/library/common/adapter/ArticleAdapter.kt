package com.example.fragment.library.common.adapter

import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.example.fragment.library.base.adapter.BaseAdapter
import com.example.fragment.library.base.http.HttpRequest
import com.example.fragment.library.base.http.HttpResponse
import com.example.fragment.library.base.http.post
import com.example.fragment.library.base.utils.*
import com.example.fragment.library.common.R
import com.example.fragment.library.common.activity.RouterActivity
import com.example.fragment.library.common.bean.ArticleBean
import com.example.fragment.library.common.constant.Keys
import com.example.fragment.library.common.constant.Router
import com.example.fragment.library.common.databinding.ArticleBannerItemBinding
import com.example.fragment.library.common.databinding.ArticleItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class ArticleAdapter() : BaseAdapter<ArticleBean>() {

    private var avatarList: List<Int> = listOf(
        R.drawable.avatar_1_raster,
        R.drawable.avatar_2_raster,
        R.drawable.avatar_3_raster,
        R.drawable.avatar_4_raster,
        R.drawable.avatar_5_raster,
        R.drawable.avatar_6_raster,
    )

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    override fun onCreateViewBinding(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding {
        return if (viewType == 0) ArticleBannerItemBinding::inflate else ArticleItemBinding::inflate
    }

    override fun onItemView(holder: ViewBindHolder, position: Int, item: ArticleBean) {
        if (item.viewType == 0) {
            val binding = holder.binding as ArticleBannerItemBinding
            item.banners?.let { data ->
                binding.banner.adapter = BannerAdapter(data)
                initIndicator(binding.indicator, data.size)
                BannerHelper(binding.banner, RecyclerView.HORIZONTAL).apply {
                    setOnItemScrollListener(object : OnItemScrollListener {
                        override fun onItemScroll(position: Int) {
                            makeSureIndicator(binding.indicator, position)
                        }
                    })
                    start()
                }
            }
        } else {
            val binding = holder.binding as ArticleItemBinding
            val activity: RouterActivity = contextToT(binding.root.context)
            binding.root.setOnClickListener {
                activity.navigation(Router.WEB, bundleOf(Keys.URL to Uri.encode(item.link)))
            }
            binding.avatar.loadCircleCrop(avatarList[position % 6])
            binding.avatar.setOnClickListener {
                activity.navigation(Router.SHARE_ARTICLE, bundleOf(Keys.UID to item.userId))
            }
            val shareUser = "${item.author}${item.shareUser}"
            binding.shareUser.text = shareUser.ifBlank { "匿名" }
            binding.time.text = item.niceDate
            if (!item.tags.isNullOrEmpty()) {
                binding.tag.text = item.tags[0].name
                binding.tag.setOnClickListener {
                    val uriString = "https://www.wanandroid.com${item.tags[0].url}"
                    val uri = Uri.parse(uriString)
                    var cid = uri.getQueryParameter(Keys.CID)
                    if (cid.isNullOrBlank()) {
                        val paths = uri.pathSegments
                        if (paths != null && paths.size >= 3) {
                            cid = paths[2]
                        }
                    }
                    activity.navigation(Router.SYSTEM, bundleOf(Keys.CID to cid))
                }
            } else {
                binding.tag.visibility = View.GONE
            }
            binding.title.text = fromHtml(item.title)
            if (item.desc.isNotBlank()) {
                binding.title.isSingleLine = true
                binding.desc.text = removeAllBank(fromHtml(item.desc), 2)
                binding.desc.visibility = View.VISIBLE
            } else {
                binding.title.isSingleLine = false
                binding.desc.visibility = View.GONE
            }
            if (item.envelopePic.isNotBlank()) {
                binding.image.visibility = View.VISIBLE
                binding.image.load(item.getHttpsEnvelopePic())
                binding.image.outlineProvider = RoundViewOutlineProvider(15f)
                binding.image.clipToOutline = true
            } else {
                binding.image.visibility = View.GONE
            }
            binding.chapterName.buildSpannableString {
                if (item.fresh) {
                    append("新  ") {
                        setColor(ContextCompat.getColor(binding.chapterName.context, R.color.blue))
                    }
                }
                if (item.top) {
                    append("置顶  ") {
                        setColor(
                            ContextCompat.getColor(
                                binding.chapterName.context,
                                R.color.orange
                            )
                        )
                    }
                }
                append(fromHtml(formatChapterName(item.superChapterName, item.chapterName)))
            }
            binding.chapterName.setOnClickListener {
                activity.navigation(Router.SYSTEM, bundleOf(Keys.CID to item.chapterId))
            }
            binding.collect.load(if (item.collect) R.drawable.ic_collect_checked else R.drawable.ic_collect_unchecked_stroke)
            binding.collect.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    if (item.collect) {
                        val url = "lg/uncollect_originId/{id}/json"
                        val request = HttpRequest(url).putPath("id", item.id)
                        val response = post<HttpResponse>(request)
                        if (response.errorCode == "0") {
                            binding.collect.load(R.drawable.ic_collect_unchecked_stroke)
                            item.collect = false
                        }
                    } else {
                        val request = HttpRequest("lg/collect/{id}/json").putPath("id", item.id)
                        val response = post<HttpResponse>(request)
                        if (response.errorCode == "0") {
                            binding.collect.load(R.drawable.ic_collect_checked)
                            item.collect = true
                        }
                    }
                }
            }
        }
    }

    private fun initIndicator(indicator: ViewGroup, itemCount: Int) {
        if (itemCount > 0) {
            indicator.removeAllViews()
            val layoutParams = LinearLayout.LayoutParams(
                dp2px(12f).toInt(),
                dp2px(3f).toInt()
            )
            layoutParams.marginStart = dp2px(2.5f).toInt()
            layoutParams.marginEnd = dp2px(2.5f).toInt()
            for (i in 0 until itemCount) {
                val point = View(indicator.context)
                point.setBackgroundResource(R.drawable.selector_indicator)
                indicator.addView(point, layoutParams)
            }
            indicator.getChildAt(0).isSelected = true
        }
    }

    private fun makeSureIndicator(indicator: ViewGroup, position: Int) {
        indicator.post {
            val itemCount = indicator.childCount
            if (position in 0 until itemCount) {
                for (i in 0 until itemCount) {
                    indicator.getChildAt(i).isSelected = false
                }
                indicator.getChildAt(position).isSelected = true
            }
        }
    }

    private fun fromHtml(str: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(str).toString()
        }
    }

    private fun formatChapterName(vararg names: String): String {
        val format = StringBuilder()
        for (name in names) {
            if (!TextUtils.isEmpty(name)) {
                if (format.isNotEmpty()) format.append("·")
                format.append(name)
            }
        }
        return format.toString()
    }

    private fun removeAllBank(str: String?, count: Int): String {
        var s = ""
        if (str != null) {
            val p = Pattern.compile("\\s{$count,}|\t|\r|\n")
            val m = p.matcher(str)
            s = m.replaceAll(" ")
        }
        return s
    }

}