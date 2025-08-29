package com.example.soar.MyPage.TagSelection

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // ImageView import 추가
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.StepCsExtraBinding

class Step4Fragment : Fragment(R.layout.step_cs_extra) {

    private var _binding: StepCsExtraBinding? = null
    private val b get() = _binding!!

    private val viewModel: Step4ViewModel by viewModels()
    private val activityViewModel: TagSelectionViewModel by activityViewModels() // 추가


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsExtraBinding.bind(view)

        // 1. JSON에서 태그 데이터를 로드하여 ViewModel 초기화
        activityViewModel.allTags.observe(viewLifecycleOwner) { allTags ->
            if (allTags.isNullOrEmpty()) return@observe
            viewModel.loadInitialTags(allTags)
        }

        // [추가] 수정 모드일 경우, 저장된 값으로 ViewModel 상태를 초기화합니다.
        if (activityViewModel.isEditMode.value == true) {
            // Shared ViewModel에서 저장된 키워드 리스트를 가져와 ID Set으로 변환
            val previouslySelectedIds = activityViewModel.additionalConditions.value
                ?.map { it.tagId }?.toSet() ?: emptySet()
            // 로컬 ViewModel의 상태를 업데이트
            viewModel.initializeSelection(previouslySelectedIds)
        }

        setupProgressHeader()
        // ✨ 추가: 저장 성공 이벤트를 관찰
        activityViewModel.saveSuccessEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                // 성공 이벤트가 발생하면 액티비티 종료
                activity?.let {
                    it.finish()
                    it.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
        }
        setupListeners()
        setupObservers()
    }

    private fun setupProgressHeader() {
        // ViewBinding을 통해 include된 레이아웃의 View에 접근
        b.progressBg.tvProgress.visibility = View.GONE
        b.progressBg.tvProgressDescription.visibility = View.GONE
        b.btnPrevious.visibility = View.GONE
        b.btnNext.text = "수정하기"
    }

    private fun setupListeners() {
        b.btnNext.setOnClickListener {
            // [수정] .find() 대신 .filter()를 사용하여 선택된 모든 태그를 리스트로 가져옵니다.
            val selectedUiModels = viewModel.extraTagsUiModel.value?.filter { it.isSelected }

            if (!selectedUiModels.isNullOrEmpty()) {
                // [수정] List<TagUiModel>을 List<TagResponse>로 변환(map)합니다.
                val selectedTags = selectedUiModels.map { uiModel ->
                    TagResponse(uiModel.tagId, uiModel.tagName, uiModel.fieldId)
                }
                // [수정] 변환된 '리스트'를 전달합니다.
                activityViewModel.setAdditionalConditions(selectedTags)
            } else {
                // 아무것도 선택하지 않았을 경우 빈 리스트를 전달합니다.
                activityViewModel.setAdditionalConditions(emptyList())
            }

            activityViewModel.saveUserTags()
        }

    }

    private fun setupObservers() {
        // 2. ViewModel의 UI 모델이 변경될 때마다 FlexboxLayout 업데이트
        viewModel.extraTagsUiModel.observe(viewLifecycleOwner) { tags ->
            updateTagsUI(tags)
        }


    }

    /**
     * 키워드 리스트를 받아 FlexboxLayout의 UI를 갱신하는 함수
     */
    private fun updateTagsUI(tags: List<TagUiModel>) {
        b.flexboxLayoutKeywords.removeAllViews()

        val marginInDp = 8
        val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()

        tags.forEach { tag ->
            // item_keyword.xml 레이아웃을 inflate
            val keywordView = layoutInflater.inflate(R.layout.item_keyword, b.flexboxLayoutKeywords, false)
            val textView = keywordView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName

            // 선택 상태에 따라 UI 적용
            if (tag.isSelected) {
                setSelectedUI(keywordView)
            } else {
                setDefaultUI(keywordView)
            }

            // 클릭 시 ViewModel의 선택 상태를 변경
            keywordView.setOnClickListener {
                viewModel.toggleTagSelection(tag.tagId)
            }

            // 마진 설정
            val params = keywordView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, marginInPx, marginInPx)
            keywordView.layoutParams = params

            b.flexboxLayoutKeywords.addView(keywordView)
        }
    }

    // 기본 상태 UI 설정 (KeywordActivity 로직 적용)
    private fun setDefaultUI(tagView: View) {
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        val btnClose = tagView.findViewById<ImageView>(R.id.btn_close) // ImageView 참조

        tagView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_coolgray_100)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_gray_800))
        btnClose.visibility = View.GONE // 'X' 아이콘 숨기기
    }

    // 선택 상태 UI 설정 (KeywordActivity 로직 적용)
    private fun setSelectedUI(tagView: View) {
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        val btnClose = tagView.findViewById<ImageView>(R.id.btn_close) // ImageView 참조

        tagView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_blue_150)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_blue_600))
        btnClose.visibility = View.VISIBLE // 'X' 아이콘 보이기
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}