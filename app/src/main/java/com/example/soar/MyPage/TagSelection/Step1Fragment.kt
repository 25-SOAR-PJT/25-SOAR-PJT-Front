package com.example.soar.MyPage.TagSelection

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.StepCsLocationBinding

class Step1Fragment : Fragment(R.layout.step_cs_location) {

    private var _binding: StepCsLocationBinding? = null
    private val b get() = _binding!!
    private val activityViewModel: TagSelectionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsLocationBinding.bind(view)

        activityViewModel.location.observe(viewLifecycleOwner) { locationTag ->
            val isSelected = locationTag != null
            b.clDropdown.isSelected = isSelected

            if (isSelected) {
                b.tvSelection.text = locationTag!!.tagName
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
        setupProgressHeader()
    }

    private fun setupListeners() {
        b.clDropdown.setOnClickListener {
            findNavController().navigate(R.id.action_step1_to_locationSelectionFragment)
        }

        // ✨ 수정: '수정하기' 버튼은 태그 저장 API만 호출
        b.btnNext.setOnClickListener {
            activityViewModel.saveUserTags()
        }

        setFragmentResultListener("location_request") { _, bundle ->
            val selectedId = bundle.getInt("selected_id")
            val selectedName = bundle.getString("selected_name")
            if (selectedName != null) {
                val selectedTag = TagResponse(selectedId, selectedName, 9)
                activityViewModel.setLocation(selectedTag)
            }
        }
    }

    private fun setupProgressHeader() {
        b.progressBg.tvProgress.visibility = View.GONE
        b.progressBg.tvProgressDescription.visibility = View.GONE
        b.btnPrevious.visibility = View.GONE
        b.btnNext.text = "수정하기"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null }
}