package com.example.soar.CurationSequencePage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.soar.R
import com.example.soar.databinding.StepCsSuggestionBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.ExplorePage.ExploreAdapter
import com.example.soar.Network.explore.YouthPolicy

class Step8Fragment : Fragment(R.layout.step_cs_suggestion) {

    private var _binding: StepCsSuggestionBinding? = null
    private val b get() = _binding!!
    private val activityViewModel: CurationSequenceViewModel by activityViewModels()

    private lateinit var bizAdapter: ExploreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsSuggestionBinding.bind(view)

        setupProgressHeader()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        // 어댑터 생성 시 isBookmarkEnabled를 false로 설정하여 북마크 기능을 비활성화
        bizAdapter = ExploreAdapter(object : ExploreAdapter.OnItemClickListener {
            override fun onPolicyItemClick(policy: YouthPolicy) {
                // 이 화면에서는 상세 페이지로 이동하지 않음
            }
            override fun onBookmarkClick(policy: YouthPolicy) {
                // 북마크 기능이 비활성화되므로 이 코드는 실행되지 않음
            }
        }, isBookmarkEnabled = false) // ✨ 수정된 부분 ✨

        b.bizList.apply {
            adapter = bizAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupListeners() {
        b.btnNext.setOnClickListener {
            (requireActivity() as? CurationSequenceActivity)?.navigateToMainAndOpenPersonalBiz()
        }
    }

    private fun setupObservers() {
        activityViewModel.suggestedPolicies.observe(viewLifecycleOwner) { policies ->
            bizAdapter.submitList(policies)

            val policyCount = policies?.size ?: 0

            b.tvTitle.text = getString(R.string.cs_suggestion_tv, policyCount)

            if (policyCount == 0) {
                b.tvSubtitle.text = getString(R.string.cs_suggestion_description_empty)
            } else {
                val userName = activityViewModel.userName.value ?: "사용자"
                b.tvSubtitle.text = getString(R.string.cs_suggestion_description, userName)
            }
        }
    }

    private fun setupProgressHeader() {
        b.progressBg.tvProgress.visibility = View.GONE
        b.progressBg.tvProgressDescription.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}