package com.example.soar.Utill // 실제 PolicyActivity의 패키지 경로

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.R
import com.example.soar.databinding.ActivityTermsBinding // 사용하는 바인딩 클래스

class TermActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent에서 전달받은 POLICY_ID 값을 꺼냄 (기본값 -1)
        val policyId = intent.getIntExtra("POLICY_ID", -1)

        // 2. UI에 표시할 데이터를 담을 변수 선언
        val title: String
        val mainDesc: String
        val descriptionList: List<String>
        val explanation: String? // 설명 텍스트는 없는 경우도 있으므로 Nullable

        // 3. policyId에 따라 각 변수에 적절한 문자열 리소스를 할당
        when (policyId) {
            0 -> { // [필수] 이용약관 동의
                title = getString(R.string.policy1_desc_title)
                mainDesc = getString(R.string.policy1_desc_main)
                descriptionList = listOf(getString(R.string.policy1_desc_main2))
                explanation = null // 이용약관에는 추가 설명이 없음
            }
            1 -> { // [필수] 개인정보 수집 및 이용 동의
                title = getString(R.string.policy2_desc_title)
                mainDesc = getString(R.string.policy2_desc_main)
                descriptionList = listOf(
                    getString(R.string.policy2_desc_2),
                    getString(R.string.policy2_desc_3),
                    getString(R.string.policy2_desc_4)
                )
                explanation = getString(R.string.policy2_explanation)
            }
            2 -> { // [선택] 마케팅 정보 수신 동의
                title = getString(R.string.policy3_desc_title)
                mainDesc = getString(R.string.policy3_desc_main)
                descriptionList = listOf(
                    getString(R.string.policy3_desc_2),
                    getString(R.string.policy3_desc_3),
                    getString(R.string.policy3_desc_4)
                )
                explanation = getString(R.string.policy3_explanation)
            }
            3 -> { // [선택] 민감정보 처리 동의
                title = getString(R.string.policy4_desc_title)
                mainDesc = getString(R.string.policy4_desc_main)
                descriptionList = listOf(
                    getString(R.string.policy4_desc_2),
                    getString(R.string.policy4_desc_3),
                    getString(R.string.policy4_desc_4)
                )
                explanation = getString(R.string.policy4_explanation)
            }
            4 -> { // [필수] 제3자 정보 제공 동의
                title = getString(R.string.policy5_desc_title)
                mainDesc = getString(R.string.policy5_desc_main)
                descriptionList = listOf(
                    getString(R.string.policy5_desc_2),
                    getString(R.string.policy5_desc_3),
                    getString(R.string.policy5_desc_4),
                    getString(R.string.policy5_desc_5)
                )
                explanation = getString(R.string.policy5_explanation)
            }
            else -> { // 예외 처리
                title = "약관 정보"
                mainDesc = "잘못된 접근입니다."
                descriptionList = emptyList()
                explanation = null
            }
        }

        // 4. 준비된 데이터로 UI 컴포넌트 설정
        setupAppBar()
        binding.descMain.text = mainDesc
        setupRecyclerView(descriptionList)

        // 설명(explanation) 텍스트가 있을 때만 보이도록 처리
        if (explanation.isNullOrEmpty()) {
            binding.policyExplanation.visibility = View.GONE
        } else {
            binding.policyExplanation.visibility = View.VISIBLE
            binding.policyExplanation.text = explanation
        }
    }

    private fun setupAppBar() {
        binding.appbar.textTitle.text = " "
        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupRecyclerView(descriptionList: List<String>) {
        val policyAdapter = TermDescAdapter(descriptionList)
        binding.rvPolicyDesc.apply {
            adapter = policyAdapter
            layoutManager = LinearLayoutManager(this@TermActivity)
        }
    }
}