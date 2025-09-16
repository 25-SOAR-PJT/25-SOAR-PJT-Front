package com.example.soar.DetailPage

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.Network.RecentViewManager
import com.example.soar.Network.RetrofitClient.apiService
import com.example.soar.Network.TokenManager
import com.example.soar.R
import com.example.soar.databinding.ActivityDetailPageBinding
import com.example.soar.Network.detail.YouthPolicyDetail
import com.example.soar.util.showBlockingToast
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

    // ✨ 북마크 상태 변경 여부를 추적하기 위한 변수
    private var bookmarkStatusChanged = false
    private var initialBookmarkStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get policyId from Intent
        policyId = intent.getStringExtra("policyId")
        if (policyId == null) {
            showBlockingToast("정책 정보를 불러올 수 없습니다.", hideCancel = true)
            finish()
            return
        }
        policyId = intent.getStringExtra("policyId")
        viewModel.loadBookmarkStatus(policyId!!)

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

    // ✨ 뒤로가기 버튼 클릭 시 변경된 북마크 상태를 이전 화면에 전달
    override fun onBackPressed() {
        setBookmarkResult()
        super.onBackPressed()
    }


    private fun setupUI(policyId: String) {
        // Appbar
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.detailPage_title)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            setBookmarkResult()
            finish()

        }

        binding.btnBookmark.setOnClickListener {
            // 1. 로그인 상태 확인
            if (TokenManager.getAccessToken().isNullOrEmpty()) {
                showBlockingToast("로그인 후 이용할 수 있습니다.", hideCancel = true)
                return@setOnClickListener
            }
            // 2. ViewModel의 토글 함수 호출
            viewModel.toggleBookmark(policyId)
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
            showBlockingToast(errorMsg, hideCancel = true)
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
            event.getContentIfNotHandled()
                ?.let { showBlockingToast(it, hideCancel = true) }
        }

        // --- ✨ 북마크 관련 LiveData 관찰 ---
        viewModel.isBookmarked.observe(this) { isBookmarked ->
            // LiveData의 상태에 따라 아이콘 변경
            if (isBookmarked) {
                binding.imgBookmark.setImageResource(R.drawable.icon_bookmark_checked)
            } else {
                binding.imgBookmark.setImageResource(R.drawable.icon_bookmark)
            }
            // 초기 상태와 현재 상태가 다르면, 변경되었다고 기록
            if (isBookmarked != initialBookmarkStatus) {
                bookmarkStatusChanged = true
            }
        }

        viewModel.bookmarkEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                showBlockingToast(message, hideCancel = true)
            }
        }


    }

    private fun updateUIWithPolicyDetail(detail: YouthPolicyDetail) {
        // Update main info
        binding.textPolicyName.text = detail.policyName
        binding.textPolicyDeadline.text = detail.dateLabel

        // Update support content and benefit
        binding.textSupportTarget.text = detail.policyExplanation
        binding.textSupportBenefit.text = detail.policySupportContent

        // [✨ 수정] 신청 기간(text_support_deadline)에 대한 조건부 로직 수정
        val start = detail.businessPeriodStart
        val end = detail.businessPeriodEnd
        val etc = detail.businessPeriodEtc

        val deadlineText = when {
            // 1. 시작일과 종료일이 모두 있을 경우 (공백이 아닌 경우만)
            !start.isNullOrBlank() && !end.isNullOrBlank() -> {
                if (!etc.isNullOrBlank()) {
                    "${start.formatDate()} ~ ${end.formatDate()} ($etc)" // 날짜 형식 변환
                } else {
                    "${start.formatDate()} ~ ${end.formatDate()}" // 날짜 형식 변환
                }
            }
            // 2. 시작일만 있을 경우 (공백이 아닌 경우만)
            !start.isNullOrBlank() -> {
                if (!etc.isNullOrBlank()) {
                    "${start.formatDate()}부터 ($etc)" // 날짜 형식 변환
                } else {
                    "${start.formatDate()}부터" // 날짜 형식 변환
                }
            }
            // 3. 종료일만 있을 경우 (공백이 아닌 경우만)
            !end.isNullOrBlank() -> {
                if (!etc.isNullOrBlank()) {
                    "${end.formatDate()}까지 ($etc)" // 날짜 형식 변환
                } else {
                    "${end.formatDate()}까지" // 날짜 형식 변환
                }
            }
            // 4. 기타 정보만 있을 경우 (공백이 아닌 경우만)
            !etc.isNullOrBlank() -> {
                etc
            }
            // 5. 모든 정보가 없거나 공백일 경우
            else -> {
                "-" // 기본값
            }
        }
        binding.textSupportDeadline.text = deadlineText

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
        val keywords =
            detail.policyKeyword?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: emptyList()
        updateFlexbox(detail.largeClassification, detail.mediumClassification, keywords)


        // 1. 로그인 상태 확인
        if (!TokenManager.getAccessToken().isNullOrEmpty()) {
            // 2. URL 우선순위 결정
            val targetUrl = when {
                !detail.applyUrl.isNullOrBlank() -> detail.applyUrl
                !detail.referenceUrl1.isNullOrBlank() -> detail.referenceUrl1
                !detail.referenceUrl2.isNullOrBlank() -> detail.referenceUrl2
                else -> {
                    // 3. 위 URL이 모두 없을 경우, 정책 이름으로 구글 검색 URL 생성
                    val query = URLEncoder.encode(detail.policyName, "UTF-8")
                    "https://www.google.com/search?q=$query"
                }
            }

            // 4. 버튼을 보이게 하고 클릭 리스너 설정
            binding.btnSticky.visibility = View.VISIBLE
            binding.btnSticky.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    showBlockingToast("URL을 열 수 있는 앱이 없습니다.", hideCancel = true)
                }
                // suspend 함수는 coroutine scope 안에서 실행
                lifecycleScope.launch {
                    try {
                        callAttendanceCheck()
                    } catch (e: Exception) {
                        Log.e("FCM", "failed", e)
                    }
                }
            }
        } else {
            // 비로그인 상태이면 버튼을 숨김
            binding.btnSticky.visibility = View.GONE
        }
    }
    suspend fun callAttendanceCheck() {
        apiService.attendanceCheck()
    }

    private fun updateUIWithStepDetail(stepDetail: com.example.soar.Network.detail.PolicyStepDetail) {
        // XML의 ID와 API 응답 데이터를 매핑합니다.
        // applyStep, documentStep, noticeStep의 각 TextView에 ID를 부여해야 합니다.
        // 예: applyStepDescription, documentStepDescription, noticeStepDescription

        // 신청 접수
        // 신청 접수
        binding.applyStepDescription.text = buildString {
            append(stepDetail.applyStep ?: "-")
        }.trim().toHtmlSpanned()


        // 서류 검토
        binding.documentStepDescription.text = buildString {
            append(stepDetail.documentStep ?: "-")
        }.trim().toHtmlSpanned()

        // 발표 및 안내
        binding.noticeStepDescription.text = buildString {
            append(stepDetail.noticeStep ?: "-")
        }.trim().toHtmlSpanned()


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
    private fun updateFlexbox(
        largeClassification: String?,
        mediumClassification: String?,
        items: List<String>
    ) {
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
                backgroundTintList =
                    ContextCompat.getColorStateList(this@DetailPageActivity, R.color.ref_blue_150)
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
                backgroundTintList = ContextCompat.getColorStateList(
                    this@DetailPageActivity,
                    R.color.ref_coolgray_100
                )
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
                backgroundTintList = ContextCompat.getColorStateList(
                    this@DetailPageActivity,
                    R.color.ref_coolgray_100
                )
                setTextColor(ContextCompat.getColor(this@DetailPageActivity, R.color.ref_gray_800))
                setTextAppearance(R.style.Font_Label_Semibold)
                gravity = Gravity.CENTER_VERTICAL
            }
            flexLayout.addView(tagView)
        }
    }

    private fun String?.toHtmlSpanned(): Spanned {
        // null일 경우 빈 객체를 반환하여 NullPointerException 방지
        if (this.isNullOrEmpty()) {
            return Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY)
        }
        // 안드로이드 N (API 24) 이상과 미만을 구분하여 처리
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(this)
        }


    }

    private fun String.formatDate(): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val date = LocalDate.parse(this, inputFormatter)
            date.format(outputFormatter)
        } catch (e: DateTimeParseException) {
            this // 파싱에 실패하면 원본 문자열 반환
        }
    }

    /** ✨ 변경된 북마크 상태를 결과로 설정하는 함수 */
    private fun setBookmarkResult() {
        if (bookmarkStatusChanged) {
            val resultIntent = Intent().apply {
                putExtra("policyId", policyId)
                putExtra("newBookmarkStatus", viewModel.isBookmarked.value)
            }
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
    }
}