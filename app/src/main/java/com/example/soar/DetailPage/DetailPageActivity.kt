package com.example.soar.DetailPage

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
    data class Review (val username: String, val content: String) : Item()
}
// 더미데이터
object DummyData {

    val documentItems = listOf(
        Item.Document( "신분증 사본", "정부24에서 발급"),
        Item.Document( "개인정보 수집 동의서", "서울시청 양식"),
        Item.Document( "통장사본", "주민센터 발급"),
        Item.Document( "사업자등록증", "국세청 발급")
    )

    val keywordItems = listOf(
        Item.Keyword( "청년정책"),
        Item.Keyword( "지원금"),
        Item.Keyword( "일자리일자리일자리일자리일자리일자리일자리일자리일자리일자리일자리"),
        Item.Keyword( "일자리"),
        Item.Keyword( "일자리"),
        Item.Keyword( "일자리"),
        Item.Keyword( "일자리")
    )

    val reviewItems = listOf(
        Item.Review("돈없는대학생19", "이걸로 많은 도움 받았어요! 추천합니다"),
        Item.Review("배부른고양이", "다들 꼭 지원하세요!! 두 달에 한 번씩 40만원씩 지급되는데..."),
        Item.Review("닉네임뭐하지", "지역마다 기간이 정해져있는 것 같아요. 내년 저희 지역 공고 올라오면..."),
        Item.Review("쏘야짱", "이런 제도가 있는 줄 몰랐는데 쏘야 덕분에 알게 되었네요..."),
        Item.Review("행복하자우리", "정말 유익한 정보였어요. 부모님도 같이 신청했어요."),
        Item.Review("아쉬운점도있음", "조건이 까다로운 편이에요.")
    )
}


class DetailPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPageBinding

    // 더미데이터
    val docs = DummyData.documentItems
    val keywords = DummyData.keywordItems
    val reviews = DummyData.reviewItems

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val spaceDp = 4f // 원하는 여백 (dp)
        val spacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, spaceDp, resources.displayMetrics
        ).toInt()
        keywordRecyclerview.layoutManager = keywordlayoutManager
        keywordRecyclerview.addItemDecoration(SpacesItemDecoration(spacingPx))
        keywordRecyclerview.adapter = KeywordAdapter(keywords)


        // 탭 바 (페이지 스크롤)
        val scrollView = binding.scrollView
        fun scrollToView(target: View) {
            scrollView.post {
                scrollView.smoothScrollTo(0, target.top)
            }
        }
        binding.toContent.setOnClickListener() {
            scrollToView(binding.sectionContent)
            selectTab(0)
        }
//        binding.toDocument.setOnClickListener() {
//            scrollToView(binding.sectionDocument)
//            selectTab(1)
//
//        }
        binding.toDetail.setOnClickListener() {
            scrollToView(binding.sectionDetail)
            selectTab(1)

        }
        binding.toReview.setOnClickListener() {
            scrollToView(binding.sectionReview)
            selectTab(2)
        }

//        // 문서
//        val documentRecyclerview = binding.documentRecyclerview
//        val documentLayoutManager = GridLayoutManager(this, 2) // 2개의 열
//        documentRecyclerview.layoutManager  = documentLayoutManager
//        documentRecyclerview.addItemDecoration(SpacesItemDecoration(spacingPx))
//        documentRecyclerview.adapter = DocumentAdapter(docs)


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

        // 댓글 표시
        val top5 = reviews.take(5) // 상위 5개만
        val reviewRecyclerview = binding.reviewRecyclerview
        reviewRecyclerview.layoutManager = LinearLayoutManager(this)
        reviewRecyclerview.adapter = ReviewAdapter(
            top5,
            onOptionsClick = null,
            showOptions = false
            )
        val count = if (reviews.size > 999) "999+" else reviews.size.toString()
        binding.textReviewCount.text = count

        // 댓글 입력 + 더보기
        binding.btnMore.setOnClickListener{
            val intent = Intent(this, ReviewDetailActivity::class.java)
            startActivity(intent)
        }
        binding.textInput.setOnClickListener{
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("FOCUS_INPUT", true) // 신호 전달
            startActivity(intent)
        }
    }

    fun selectTab(index: Int) {
        val colors = listOf(binding.text1, binding.text2, binding.text3)
        val lines = listOf(binding.line1, binding.line2, binding.line3)

        for (i in 0..2) {
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