package com.example.soar.DetailPage

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.soar.R
import com.example.soar.databinding.ActivityDetailPageBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

// 임시 데이터 클래스
sealed class Item {
    data class Document(val title: String, val subtitle: String) : Item()
    data class Keyword(val text: String) : Item()
}


class DetailPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 더미데이터
        val documentItems = listOf(
            Item.Document(title = "신분증 사본", subtitle = "정부24에서 발급"),
            Item.Document(title = "개인정보 수집 동의서", subtitle = "서울시청 양식"),
            Item.Document(title = "통장사본", subtitle = "주민센터 발급"),
            Item.Document(title = "사업자등록증", subtitle = "국세청 발급")
        )

        val keywordItems = listOf(
            Item.Keyword(text = "청년정책"),
            Item.Keyword(text = "지원금"),
            Item.Keyword(text = "일자리일자리일자리일자리일자리일자리일자리일자리일자리일자리일자리"),
            Item.Keyword(text = "일자리"),
            Item.Keyword(text = "일자리"),
            Item.Keyword(text = "일자리"),
            Item.Keyword(text = "일자리")
        )

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.detailPage_title)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // 북마크
        var isBookmarked = false
        binding.btnBookmark.setOnClickListener {
            if (isBookmarked) {
                binding.imgBookmark.setImageResource(R.drawable.icon_bookmark_checked)
            } else {
                binding.imgBookmark.setImageResource(R.drawable.icon_bookmark)
            }
            isBookmarked = !isBookmarked
        }

        // 키워드
        val keywordRecyclerview = binding.keywordRecyclerview
        val keywordlayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        keywordRecyclerview.layoutManager = keywordlayoutManager
        keywordRecyclerview.adapter = KeywordAdapter(keywordItems)


        // 탭 바 (페이지 스크롤)
        val scrollView = binding.scrollView
        fun scrollToView(target: View) {
            scrollView.post {
                scrollView.smoothScrollTo(0, target.top)
            }
        }
        binding.toContent.setOnClickListener() {
            //scrollToView(binding.sectionContent)
            selectTab(0)
        }
        binding.toDocument.setOnClickListener() {
            scrollToView(binding.sectionDocument)
            selectTab(1)

        }
        binding.toDetail.setOnClickListener() {
            scrollToView(binding.sectionDetail)
            selectTab(2)

        }
        binding.toReview.setOnClickListener() {
            scrollToView(binding.sectionReview)
            selectTab(3)
        }

        // 문서
        val documentRecyclerview = binding.documentRecyclerview
        val documentLayoutManager = GridLayoutManager(this, 2) // 2개의 열
        val spaceDp = 4f // 원하는 여백 (dp)
        val spacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, spaceDp, resources.displayMetrics
        ).toInt()
        documentRecyclerview.layoutManager  = documentLayoutManager
        documentRecyclerview.addItemDecoration(SpacesItemDecoration(spacingPx))
        documentRecyclerview.adapter = DocumentAdapter(documentItems)


        // 상세정보 더보기
        var isExpanded = false
        binding.btnFold.setOnClickListener {
            if (isExpanded) {
                // 전체 펼치기
                binding.textDetail.maxLines = Int.MAX_VALUE
                binding.textDetail.ellipsize = null
                binding.btnFold.text = getString(R.string.fold)
                binding.gradient.visibility = View.GONE
            } else {
                // 다시 접기
                binding.textDetail.maxLines = 15
                binding.textDetail.ellipsize = TextUtils.TruncateAt.END
                binding.btnFold.text = getString(R.string.detail_more)
                binding.gradient.visibility = View.VISIBLE
            }
            isExpanded = !isExpanded
        }

        // 댓글 더보기
        binding.btnMore.setOnClickListener {
//            val intent = Intent(this, ReviewDetailActivity::class.java)
//            startActivity(intent)
        }
    }

    fun selectTab(index: Int) {
        val colors = listOf(binding.text1, binding.text2, binding.text3, binding.text4)
        val lines = listOf(binding.line1, binding.line2, binding.line3, binding.line4)

        for (i in 0..3) {
            colors[i].setTextColor(
                getColor(if (i == index) R.color.ref_blue_500 else R.color.ref_coolgray_700)
            )
            lines[i].visibility = if (i == index) View.VISIBLE else View.GONE
        }
    }


}

class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.top = space
        outRect.bottom = space
    }
}