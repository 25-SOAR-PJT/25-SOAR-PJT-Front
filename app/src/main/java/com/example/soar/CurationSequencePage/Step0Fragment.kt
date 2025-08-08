package com.example.soar.CurationSequencePage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // [수정] activityViewModels로 변경
import androidx.navigation.fragment.findNavController
import com.example.soar.R
import com.example.soar.databinding.StepCsStartBinding

class Step0Fragment : Fragment(R.layout.step_cs_start) {

    private var _binding: StepCsStartBinding? = null
    private val b get() = _binding!!

    // [수정] Activity의 ViewModel을 공유하여 사용
    private val activityViewModel: CurationSequenceViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsStartBinding.bind(view)

        // [수정] ViewModel의 userName을 관찰하여 TextView 업데이트
        activityViewModel.userName.observe(viewLifecycleOwner) { name ->
            b.tvTitle.text = getString(R.string.cs_entry, name)
        }

        b.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_step0_to_step1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}