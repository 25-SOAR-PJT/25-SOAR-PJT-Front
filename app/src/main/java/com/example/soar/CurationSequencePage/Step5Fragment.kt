package com.example.soar.CurationSequencePage

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.StepCsKeywordBinding
import com.example.soar.util.showBlockingToast
import com.google.android.flexbox.FlexboxLayout

class Step5Fragment : Fragment(R.layout.step_cs_keyword) {

    private var _binding: StepCsKeywordBinding? = null
    private val b get() = _binding!!

    private val viewModel: Step5ViewModel by viewModels()
    private val activityViewModel: CurationSequenceViewModel by activityViewModels() // 추가


    // fieldId와 FlexboxLayout을 매핑
    private lateinit var flexboxMap: Map<Int, FlexboxLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsKeywordBinding.bind(view)

        initializeFlexboxMap()

        activityViewModel.allTags.observe(viewLifecycleOwner) { allTags ->
            if (allTags.isNullOrEmpty()) return@observe
            viewModel.loadInitialTags(allTags)
        }

        // [추가] 수정 모드일 경우, 저장된 값으로 ViewModel 상태를 초기화합니다.
        if (activityViewModel.isEditMode.value == true) {
            // Shared ViewModel에서 저장된 키워드 리스트를 가져와 ID Set으로 변환
            val previouslySelectedIds = activityViewModel.keywords.value
                ?.map { it.tagId }?.toSet() ?: emptySet()
            // 로컬 ViewModel의 상태를 업데이트
            viewModel.initializeSelection(previouslySelectedIds)
        }

        setupProgressHeader()
        setupListeners()
        setupObservers()
    }

    private fun setupProgressHeader() {
        // ViewBinding을 통해 include된 레이아웃의 View에 접근
        b.progressBg.tvProgress.text = "5/6"
        b.progressBg.tvProgressDescription.text= "이제 정확한 추천을 받을 수 있겠는데요?"
    }

    private fun initializeFlexboxMap() {
        flexboxMap = mapOf(
            1 to b.flexLayout1, 2 to b.flexLayout2,
            3 to b.flexLayout3, 4 to b.flexLayout4
        )
    }

    private fun setupListeners() {
        b.btnNext.setOnClickListener {
            // [수정] .find() 대신 .filter()를 사용하여 선택된 모든 태그를 리스트로 가져옵니다.
            val selectedUiModels = viewModel.tagsUiModel.value?.filter { it.isSelected }

            if (!selectedUiModels.isNullOrEmpty()) {
                // [수정] List<TagUiModel>을 List<TagResponse>로 변환(map)합니다.
                val selectedTags = selectedUiModels.map { uiModel ->
                    TagResponse(uiModel.tagId, uiModel.tagName, uiModel.fieldId)
                }
                // [수정] 변환된 '리스트'를 전달합니다.
                activityViewModel.setKeywords(selectedTags)
            } else {
                // 아무것도 선택하지 않았을 경우 빈 리스트를 전달합니다.
                activityViewModel.setKeywords(emptyList())
            }


            if (activityViewModel.isEditMode.value == true) {
                findNavController().popBackStack(R.id.step6Fragment, false)
            } else {
                findNavController().navigate(R.id.action_step5_to_step6)
            }
        }
        b.btnPrevious.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        // ViewModel의 UI 모델이 변경될 때마다 전체 UI를 다시 그림
        viewModel.tagsUiModel.observe(viewLifecycleOwner) { allTags ->
            updateAllFlexboxUI(allTags)
        }


        // [추가] Toast 메시지 이벤트를 관찰
        viewModel.showToast.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                showBlockingToast(message, hideCancel = true)
            }
        }
    }

    private fun updateAllFlexboxUI(tags: List<TagUiModel>) {
        // 태그를 fieldId 별로 그룹화
        val groupedTags = tags.groupBy { it.fieldId }

        // 각 FlexboxLayout을 업데이트
        flexboxMap.forEach { (fieldId, flexboxLayout) ->
            val tagsForField = groupedTags[fieldId] ?: emptyList()
            updateFlexboxForCategory(flexboxLayout, tagsForField)
        }
    }

    /**
     * 특정 카테고리의 FlexboxLayout UI를 갱신하는 함수
     */
    private fun updateFlexboxForCategory(flexboxLayout: FlexboxLayout, tags: List<TagUiModel>) {
        flexboxLayout.removeAllViews()

        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
        ).toInt()

        tags.forEach { tag ->
            // item_keyword.xml 레이아웃 사용 (제공해주신 레이아웃 기준)
            val keywordView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_keyword, flexboxLayout, false)

            val textView = keywordView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName

            if (tag.isSelected) {
                setSelectedUI(keywordView)
            } else {
                setDefaultUI(keywordView)
            }

            keywordView.setOnClickListener {
                viewModel.toggleTagSelection(tag.tagId)
            }

            val params = (keywordView.layoutParams as FlexboxLayout.LayoutParams).apply {
                setMargins(0, 0, margin, margin)
            }
            keywordView.layoutParams = params

            flexboxLayout.addView(keywordView)
        }
    }

    private fun setDefaultUI(tagView: View) {
        tagView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_coolgray_100)
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        val btnClose = tagView.findViewById<ImageView>(R.id.btn_close)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_gray_800))
        btnClose.visibility = View.GONE
    }

    private fun setSelectedUI(tagView: View) {
        tagView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_blue_150)
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        val btnClose = tagView.findViewById<ImageView>(R.id.btn_close)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_blue_600))
        btnClose.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}