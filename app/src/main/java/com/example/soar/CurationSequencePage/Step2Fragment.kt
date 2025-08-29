package com.example.soar.CurationSequencePage

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.StepCsJobBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Step2Fragment : Fragment(R.layout.step_cs_job) {

    private var _binding: StepCsJobBinding? = null
    private val b get() = _binding!!

    private val viewModel: Step2ViewModel by viewModels()
    private val activityViewModel: CurationSequenceViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsJobBinding.bind(view)

        activityViewModel.allTags.observe(viewLifecycleOwner) { allTags ->
            if (allTags.isNullOrEmpty()) return@observe
            viewModel.loadInitialTags(allTags)
        }


        // [추가] 수정 모드일 경우, 저장된 값으로 ViewModel 상태를 초기화합니다.
        if (activityViewModel.isEditMode.value == true) {
            // Shared ViewModel에서 저장된 직업 태그의 ID를 가져옴
            val previouslySelectedId = activityViewModel.job.value?.tagId
            // 로컬 ViewModel의 상태를 업데이트
            viewModel.initializeSelection(previouslySelectedId)
        }

        setupProgressHeader()
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        b.btnNext.setOnClickListener {
            val selectedTag = viewModel.jobTagsUiModel.value?.find { it.isSelected }

            // [수정] if/else 구문으로 명시적으로 상태를 업데이트합니다.
            if (selectedTag != null) {
                // 선택한 것이 있을 경우
                val tagResponse = TagResponse(selectedTag.tagId, selectedTag.tagName, selectedTag.fieldId)
                activityViewModel.setJob(tagResponse)
            } else {
                // 선택한 것이 없을 경우, null로 상태를 업데이트
                activityViewModel.setJob(null)
            }

            // 선택된 태그 ID는 viewModel.selectedTagId.value 로 접근 가능
            if (activityViewModel.isEditMode.value == true) {
                findNavController().popBackStack(R.id.step6Fragment, false)
            } else {
                findNavController().navigate(R.id.action_step2_to_step3)
            }
        }

        b.btnPrevious.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.jobTagsUiModel.observe(viewLifecycleOwner) { tags ->
            updateTagsUI(tags)
        }

    }

    private fun setupProgressHeader() {
        // ViewBinding을 통해 include된 레이아웃의 View에 접근
        b.progressBg.tvProgress.text = "2/6"
        b.progressBg.tvProgressDescription.visibility = View.GONE // 설명 텍스트뷰를 숨김
    }

    private fun updateTagsUI(tags: List<TagUiModel>) {
        b.flexboxLayoutKeywords.removeAllViews()

        val marginInDp = 8
        val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()

        tags.forEach { tag ->
            val keywordView = layoutInflater.inflate(R.layout.item_keyword, b.flexboxLayoutKeywords, false)
            val textView = keywordView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName

            if (tag.isSelected) {
                setSelectedUI(keywordView)
            } else {
                setDefaultUI(keywordView)
            }

            keywordView.setOnClickListener {
                // [변경] toggleTagSelection -> selectTag 함수 호출
                viewModel.selectTag(tag.tagId)
            }

            val params = keywordView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, marginInPx, marginInPx)
            keywordView.layoutParams = params

            b.flexboxLayoutKeywords.addView(keywordView)
        }
    }

    private fun setDefaultUI(tagView: View) {
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        tagView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_coolgray_100)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_gray_800))
    }

    private fun setSelectedUI(tagView: View) {
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        tagView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_blue_150)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_blue_600))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}