package com.example.soar.CurationSequencePage

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soar.R
import com.example.soar.databinding.StepCsLocationBinding

class Step1Fragment : Fragment(R.layout.step_cs_location) {

    private var _binding: StepCsLocationBinding? = null
    private val b get() = _binding!!
    private val viewModel: Step1ViewModel by viewModels()
    private val activityViewModel: CurationSequenceViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsLocationBinding.bind(view)

        // 수정 모드 진입 시, Shared ViewModel의 값으로 로컬 ViewModel을 초기화
        if (activityViewModel.isEditMode.value == true) {
            viewModel.initializeSelection(activityViewModel.location.value)
        }

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        b.clDropdown.setOnClickListener {
            findNavController().navigate(R.id.action_step1_to_locationSelectionFragment)
        }

        b.btnNext.setOnClickListener {
            val selectedLocation = viewModel.location.value
            // [수정] Step3와 동일한 패턴으로 Shared ViewModel 업데이트
            activityViewModel.setLocation(selectedLocation)

            if (activityViewModel.isEditMode.value == true) {
                findNavController().popBackStack(R.id.step6Fragment, false)
            } else {
                findNavController().navigate(R.id.action_step1_to_step2)
            }
        }

        b.btnPrevious.setOnClickListener {
            findNavController().popBackStack()
        }

        // [수정] ID와 이름을 받아 TagResponse 객체를 만들어 ViewModel에 전달
        setFragmentResultListener("location_request") { _, bundle ->
            val selectedId = bundle.getInt("selected_id")
            val selectedName = bundle.getString("selected_name")
            if (selectedName != null) {
                // fieldId: 10은 지역을 의미, fieldName은 임시값
                val selectedTag = TagResponse(selectedId, selectedName, 10, "거주지역")
                viewModel.setLocation(selectedTag)
            }
        }
    }

    private fun setupObservers() {
        viewModel.location.observe(viewLifecycleOwner) { locationTag ->
            // [수정] isSelected 상태만 변경해주면 XML Selector가 나머지를 처리합니다.
            val isSelected = locationTag != null
            b.clDropdown.isSelected = isSelected

            if (isSelected) {
                b.tvSelection.text = locationTag!!.tagName
                // 선택 상태의 텍스트 색상은 Fragment에서 직접 변경
                b.tvSelection.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ref_blue_500
                    )
                )
            } else {
                b.tvSelection.text = getString(R.string.selection)
                b.tvSelection.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ref_coolgray_700
                    )
                )
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null }
}