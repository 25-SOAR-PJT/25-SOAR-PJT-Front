package com.example.soar.CurationSequencePage

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.soar.R
import com.example.soar.databinding.StepCsSummaryBinding
import com.google.android.flexbox.FlexboxLayout

class Step6Fragment : Fragment(R.layout.step_cs_summary) {

    private var _binding: StepCsSummaryBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: CurationSequenceViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsSummaryBinding.bind(view)

        setupProgressHeader()
        setupListeners()
        setupObservers()
    }

    private fun setupProgressHeader() {
        binding.progressBg.tvProgress.text = "6/6"
        binding.progressBg.tvProgressDescription.text = "마지막 단계예요!"
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            // TODO: 서버에 큐레이션 정보 전송 API 호출
            (requireActivity() as? CurationSequeceActivity)?.navigateToMainAndFinish()
        }
        binding.btnPrevious.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        activityViewModel.location.observe(viewLifecycleOwner) { location ->
            addChipToFlexbox(binding.flexboxLocation, location?.tagName, R.id.action_step6_to_step1)
        }
        activityViewModel.job.observe(viewLifecycleOwner) { job ->
            addChipToFlexbox(binding.flexboxJob, job?.tagName, R.id.action_step6_to_step2)
        }
        activityViewModel.education.observe(viewLifecycleOwner) { education ->
            addChipToFlexbox(binding.flexboxEducation, education?.tagName, R.id.action_step6_to_step3)
        }
        activityViewModel.additionalConditions.observe(viewLifecycleOwner) { conditions ->
            addChipsToFlexbox(binding.flexboxExtra, conditions, R.id.action_step6_to_step4)
        }
        activityViewModel.keywords.observe(viewLifecycleOwner) { keywords ->
            addChipsToFlexbox(binding.flexboxKeyword, keywords, R.id.action_step6_to_step5)
        }
    }


    /**
     * [수정] text가 null이거나 빈 문자열일 경우를 모두 처리하도록 isNullOrBlank() 사용
     */
    private fun addChipToFlexbox(flexbox: FlexboxLayout, text: String?, navigationActionId: Int) {
        val tags = if (text.isNullOrBlank()) {
            // text가 null이거나, 비어있거나, 공백만 있을 경우 emptyList()를 생성
            emptyList()
        } else {
            // 내용이 있는 경우에만 리스트 생성
            listOf(TagResponse(0, text, 0, ""))
        }
        addChipsToFlexbox(flexbox, tags, navigationActionId)
    }


    /**
     * [수정] List가 비어있을 경우 "해당사항 없음" 칩을 추가하는 로직으로 변경
     */
    private fun addChipsToFlexbox(flexbox: FlexboxLayout, tags: List<TagResponse>?, navigationActionId: Int) {
        flexbox.removeAllViews()
        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()

        val displayTags = if (tags.isNullOrEmpty()) {
            listOf(TagResponse(0, "해당사항 없음", 0, ""))
        } else {
            tags
        }

        displayTags.forEach { tag ->
            val chipView = LayoutInflater.from(context).inflate(R.layout.item_summary_chip, flexbox, false)
            val textView = chipView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName
            chipView.setOnClickListener {
                // [추가] 수정 모드를 true로 설정하고 화면 이동
                activityViewModel.setEditMode(true)
                findNavController().navigate(navigationActionId)
            }
            (chipView.layoutParams as? FlexboxLayout.LayoutParams)?.setMargins(0, 0, margin, margin)
            flexbox.addView(chipView)
        }
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.setEditMode(false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}