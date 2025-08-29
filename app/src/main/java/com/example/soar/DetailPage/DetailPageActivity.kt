package com.example.soar.DetailPage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.Network.RecentViewManager
import com.example.soar.R
import com.example.soar.databinding.ActivityDetailPageBinding
import com.example.soar.Network.detail.YouthPolicyDetail
import com.google.android.flexbox.FlexboxLayout

sealed class Item {
    data class Document(val title: String, val subtitle: String) : Item()
    data class Keyword(val text: String) : Item()
    data class Review (val username: String, val content: String) : Item()
}

// 더미데이터 (This will be replaced by live data)
object DummyData {
    val documentItems = listOf(
        Item.Document( "신분증 사본", "정부24에서 발급"),
        Item.Document( "개인정보 수집 동의서", "서울시청 양식"),
        Item.Document( "통장사본", "주민센터 발급"),
        Item.Document( "사업자등록증", "국세청 발급")
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
    private val viewModel: DetailViewModel by viewModels()

    // 더미데이터
    val docs = DummyData.documentItems
    val reviews = DummyData.reviewItems

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get policyId from Intent
        val policyId = intent.getStringExtra("policyId")
        if (policyId == null) {
            Toast.makeText(this, "정책 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✨ 여기에 최근 본 정책으로 기록하는 코드를 추가합니다.
        RecentViewManager.addPolicy(policyId)

        setupUI()
        observeViewModel()
        viewModel.loadPolicyDetail(policyId)
    }

    private fun setupUI() {
        // Appbar
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.detailPage_title)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Bookmark
        var isBookmarked = false
        binding.btnBookmark.setOnClickListener {
            if (isBookmarked) {
                binding.imgBookmark.setImageResource(R.drawable.icon_bookmark_checked)
            } else {
                binding.imgBookmark.setImageResource(R.drawable.icon_bookmark)
            }
            isBookmarked = !isBookmarked
        }

        // Tab bar
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
        binding.toDetail.setOnClickListener() {
            scrollToView(binding.sectionDetail)
            selectTab(1)
        }
        binding.toReview.setOnClickListener() {
            scrollToView(binding.sectionReview)
            selectTab(2)
        }

        // Expand/Fold
        var isExpanded = false
        binding.btnFold.setOnClickListener {
            if (!isExpanded) {
                binding.textDetail.maxLines = Int.MAX_VALUE
                binding.textDetail.ellipsize = null
                binding.btnFold.text = getString(R.string.fold)
                binding.gradient.visibility = View.GONE
            } else {
                binding.textDetail.maxLines = 15
                binding.textDetail.ellipsize = TextUtils.TruncateAt.END
                binding.btnFold.text = getString(R.string.detail_more)
                binding.gradient.visibility = View.VISIBLE
            }
            isExpanded = !isExpanded
        }

        // Dummy review data setup
        val top5 = reviews.take(5)
        val reviewRecyclerview = binding.reviewRecyclerview
        reviewRecyclerview.layoutManager = LinearLayoutManager(this)
        reviewRecyclerview.adapter = ReviewAdapter(
            top5,
            onOptionsClick = null,
            showOptions = false
        )
        val count = if (reviews.size > 999) "999+" else reviews.size.toString()
        binding.textReviewCount.text = count
        binding.textReviewCount2.text = count

        binding.btnMore.setOnClickListener{
            val intent = Intent(this, ReviewDetailActivity::class.java)
            startActivity(intent)
        }
        binding.textInput.setOnClickListener{
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("FOCUS_INPUT", true)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.policyDetail.observe(this) { detail ->
            updateUIWithPolicyDetail(detail)
        }

        viewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: Handle loading state (e.g., show/hide progress bar)
        }
    }

    private fun updateUIWithPolicyDetail(detail: YouthPolicyDetail) {
        // Update main info
        binding.textPolicyName.text = detail.policyName
        binding.textPolicyDeadline.text = detail.dateLabel

        // Update support content and benefit
        binding.textSupportTarget.text = detail.policyExplanation
        binding.textSupportBenefit.text = detail.policySupportContent

        // Update detail section
        val combinedDetails = buildString {
            append(detail.policyExplanation)
            append("\n\n")
            append(detail.policySupportContent)
            append("\n\n")
            append(detail.applyMethodContent)
            append("\n\n")
            append(detail.submitDocumentContent)
            append("\n\n")
            append(detail.etcMatterContent)
        }.trim()
        binding.textDetail.text = combinedDetails

        // [수정] 키워드, 대분류, 중분류 데이터를 추출하여 updateFlexbox 함수에 전달합니다.
        val keywords = detail.policyKeyword?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        updateFlexbox(detail.largeClassification, detail.mediumClassification, keywords)
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

    // [수정] 함수 파라미터를 추가하여 대분류, 중분류, 키워드 목록을 받도록 변경합니다.
    private fun updateFlexbox(largeClassification: String?, mediumClassification: String?, items: List<String>) {
        val flexLayout = binding.flexLayout
        flexLayout.removeAllViews()

        // 공통 레이아웃 속성 정의
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 36f, resources.displayMetrics
        ).toInt()

        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
        ).toInt()

        val horizontalPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
        ).toInt()

        // [추가] 1. 대분류(largeClassification) 태그 추가 (파란색 배경)
        if (!largeClassification.isNullOrEmpty()) {
            val largeClassTag = TextView(this).apply {
                text = largeClassification
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    heightPx
                ).apply {
                    setMargins(0, 0, margin, margin)
                }
                setPadding(horizontalPadding, 0, horizontalPadding, 0)
                setBackgroundResource(R.drawable.round_background2)
                backgroundTintList = ContextCompat.getColorStateList(this@DetailPageActivity, R.color.ref_blue_150)
                setTextColor(ContextCompat.getColor(this@DetailPageActivity, R.color.ref_blue_600))
                setTextAppearance(R.style.Font_Label_Semibold)
                gravity = Gravity.CENTER_VERTICAL
            }
            flexLayout.addView(largeClassTag)
        }

        // [추가] 2. 중분류(mediumClassification) 태그 추가 (회색 배경)
        if (!mediumClassification.isNullOrEmpty()) {
            val mediumClassTag = TextView(this).apply {
                text = mediumClassification
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    heightPx
                ).apply {
                    setMargins(0, 0, margin, margin)
                }
                setPadding(horizontalPadding, 0, horizontalPadding, 0)
                setBackgroundResource(R.drawable.round_background2)
                backgroundTintList = ContextCompat.getColorStateList(this@DetailPageActivity, R.color.ref_coolgray_100)
                setTextColor(ContextCompat.getColor(this@DetailPageActivity, R.color.ref_gray_800))
                setTextAppearance(R.style.Font_Label_Semibold)
                gravity = Gravity.CENTER_VERTICAL
            }
            flexLayout.addView(mediumClassTag)
        }

        // [추가] 3. 나머지 키워드(policyKeyword) 태그들 추가 (회색 배경)
        items.forEach { item ->
            val tagView = TextView(this).apply {
                text = item
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    heightPx
                ).apply {
                    setMargins(0, 0, margin, margin)
                }
                setPadding(horizontalPadding, 0, horizontalPadding, 0)
                setBackgroundResource(R.drawable.round_background2)
                backgroundTintList = ContextCompat.getColorStateList(this@DetailPageActivity, R.color.ref_coolgray_100)
                setTextColor(ContextCompat.getColor(this@DetailPageActivity, R.color.ref_gray_800))
                setTextAppearance(R.style.Font_Label_Semibold)
                gravity = Gravity.CENTER_VERTICAL
            }
            flexLayout.addView(tagView)
        }
    }
}