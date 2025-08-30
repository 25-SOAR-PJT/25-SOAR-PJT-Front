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
}


class DetailPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPageBinding
    private val viewModel: DetailViewModel by viewModels()

    private val commentViewModel: CommentViewModel by viewModels() // CommentViewModel 추가
    private lateinit var reviewAdapter: ReviewAdapter // 어댑터 타입 변경

    private var policyId: String? = null // policyId를 멤버 변수로 저장


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get policyId from Intent
        policyId = intent.getStringExtra("policyId")
        if (policyId == null) {
            Toast.makeText(this, "정책 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✨ 여기에 최근 본 정책으로 기록하는 코드를 추가합니다.
        RecentViewManager.addPolicy(policyId!!)

        //setupUI()
        setupUI(policyId!!)
        observeViewModel()
        viewModel.loadPolicyDetail(policyId!!)
        commentViewModel.loadComments(policyId!!)
    }
    override fun onResume() {
        super.onResume()
        // 화면에 다시 나타날 때마다 댓글을 새로고침
        policyId?.let {
            commentViewModel.loadComments(it)
        }
    }


    private fun setupUI(policyId: String) {
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

        reviewAdapter = ReviewAdapter { /* 미리보기에서는 옵션 버튼 동작 안 함 */ }
        binding.reviewRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.reviewRecyclerview.adapter = reviewAdapter


        binding.btnMore.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("policyId", policyId) // ReviewDetailActivity에 policyId 전달
            startActivity(intent)
        }
        binding.textInput.setOnClickListener {
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("policyId", policyId) // ReviewDetailActivity에 policyId 전달
            intent.putExtra("FOCUS_INPUT", true)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.policyDetail.observe(this) { detail ->
            updateUIWithPolicyDetail(detail)
        }

        viewModel.policyStepDetail.observe(this) { stepDetail ->
            updateUIWithStepDetail(stepDetail)
        }


        viewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: Handle loading state (e.g., show/hide progress bar)
        }

        // CommentViewModel 옵저버 추가
        commentViewModel.comments.observe(this) { comments ->
            reviewAdapter.submitList(comments.take(5)) // 상위 5개만 표시
            val count = if (comments.size > 999) "999+" else comments.size.toString()
            binding.textReviewCount.text = count
            binding.textReviewCount2.text = count
        }
        commentViewModel.toastEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
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

    private fun updateUIWithStepDetail(stepDetail: com.example.soar.Network.detail.PolicyStepDetail) {
        // XML의 ID와 API 응답 데이터를 매핑합니다.
        // applyStep, documentStep, noticeStep의 각 TextView에 ID를 부여해야 합니다.
        // 예: applyStepDescription, documentStepDescription, noticeStepDescription

        // 신청 접수
        binding.applyStepDescription.text = stepDetail.applyStep ?: "정보 없음"

        // 서류 검토
        binding.documentStepDescription.text = stepDetail.documentStep ?: "정보 없음"

        // 발표 및 안내
        binding.noticeStepDescription.text = stepDetail.noticeStep ?: "정보 없음"

        // 필요 서류 (별도 섹션이지만, step API에 포함되어 있다면 여기서 업데이트)
        // binding.textSubmittedDocuments.text = stepDetail.submittedDocuments ?: "정보 없음"
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