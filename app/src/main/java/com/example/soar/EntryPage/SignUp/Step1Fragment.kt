package com.example.soar.EntryPage.SignUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.R
import com.example.soar.Utill.TermActivity
import com.example.soar.databinding.StepPolicyBinding

class Step1Fragment : Fragment(R.layout.step_policy), PolicyAdapter.OnPolicyClickListener {

    private var _binding: StepPolicyBinding? = null
    private val b get() = _binding!!
    private val vm: Step1ViewModel by viewModels({ requireActivity() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = StepPolicyBinding.bind(view)

        val adapter = PolicyAdapter(vm, this)
        b.rvPolicies.apply {
            layoutManager = LinearLayoutManager(requireContext()) // ★ 추가
            setHasFixedSize(true)
            this.adapter = adapter
            isNestedScrollingEnabled = false   // NestedScrollView 안에서 부드럽게
        }
        // 전체 동의
        b.itemAll.setOnClickListener { vm.toggleAll() }

        // Observe
        vm.items.observe(viewLifecycleOwner) { adapter.submitList(it) }
        vm.allChecked.observe(viewLifecycleOwner) { checked ->
            // 체크박스 이미지
            b.cbAll.setImageResource(
                if (checked) R.drawable.ic_checkbox_active
                else R.drawable.ic_checkbox_inactive
            )
            b.cbAll.isSelected = checked

            // CardView·텍스트에 selected 플래그 주입
            b.itemAll.isSelected = checked    // bg / stroke 셀렉터 적용
            b.tvAll.isSelected   = checked    // 글자색 셀렉터 적용
        }
        vm.canProceed.observe(viewLifecycleOwner) { b.btnNext.isEnabled = it }

        // 다음
        b.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step1_to_step2)
        }
    }

    // ✨ 3. 인터페이스의 onDetailClick 메소드 구현
    override fun onDetailClick(policyId: Int) {
        // PolicyActivity를 실행하고, 클릭된 아이템의 id를 'POLICY_ID'라는 키로 전달
        val intent = Intent(requireContext(), TermActivity::class.java).apply {
            putExtra("POLICY_ID", policyId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}