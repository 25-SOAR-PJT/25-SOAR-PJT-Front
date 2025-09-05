package com.example.soar.EntryPage.SignUp

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.soar.R
import com.example.soar.databinding.ActivitySignUpBinding
import androidx.activity.viewModels
import androidx.core.view.doOnLayout      // ← 추가
import com.example.soar.CurationSequencePage.CurationSequenceActivity
import com.example.soar.MainActivity
import com.example.soar.Utill.TermAgreeActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val steps = listOf(
        R.id.step1Fragment,
        R.id.step2Fragment,
        R.id.step3Fragment,
        R.id.step4Fragment
    )


    private val step1ViewModel: Step1ViewModel by viewModels()

    /**
     * ✨ 2. TermAgreeActivity를 실행하고 결과를 받기 위한 ActivityResultLauncher를 선언합니다.
     * PersonalBizActivity의 로직을 재사용합니다.
     */
    private val termAgreementLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // TermAgreeActivity가 어떤 결과로든 닫히면, 메인 화면으로 이동합니다.
        // 사용자가 여기서 동의하면 API 호출은 TermAgreeActivity 내부에서 처리됩니다.
        navigateToMainAndFinish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.ref_white)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── 색상 ───────────────────────────────
        binding.progressBg.setBackgroundColor(getColor(R.color.ref_coolgray_200))
        binding.progressActive.setBackgroundColor(getColor(R.color.ref_coolgray_500))
        binding.appbar.textTitle.text = " "

        // ── NavController ─────────────────────
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ── (1) 레이아웃 완료 후 초기화 ─────────
        binding.progressBg.doOnLayout {
            // progressActive 너비 0으로
            binding.progressActive.layoutParams =
                binding.progressActive.layoutParams.apply { width = 0 }
            binding.progressActive.requestLayout()

            // 현재 목적지에 맞춰 한 번 이동(= 25 %)
            val idx = steps.indexOf(navController.currentDestination?.id).coerceAtLeast(0)
            moveProgress(idx)
        }

        // ── (2) 단계가 바뀔 때마다 애니메이션 ──
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val idx = steps.indexOf(destination.id).coerceAtLeast(0)
            moveProgress(idx)
        }
    }

    private fun moveProgress(stepIndex: Int) {
        val ratio = (stepIndex + 1f) / steps.size          // 0.25, 0.5, 0.75, 1.0
        val bgWidth = binding.progressBg.width             // 이제 0 아님!
        val endPx = (bgWidth * ratio).toInt()
        val startPx = binding.progressActive.layoutParams.width

        ValueAnimator.ofInt(startPx, endPx).apply {
            duration = 250
            addUpdateListener {
                binding.progressActive.layoutParams =
                    binding.progressActive.layoutParams.apply { width = it.animatedValue as Int }
            }
            start()
        }
    }

    /**
     * ✨ 3. Step4Fragment에서 회원가입/로그인 최종 성공 시 이 함수를 호출합니다.
     */
    fun onSignUpSuccess() {
        // Step1ViewModel에서 '민감정보 처리 동의' (인덱스 3) 항목의 체크 상태를 확인합니다.
        val agreedToSensitiveTerm = step1ViewModel.items.value?.get(3)?.checked ?: false

        if (agreedToSensitiveTerm) {
            // 이미 동의했다면, 바로 메인 화면으로 이동합니다.
            navigateToCurationAndFinish()
        } else {
            // 동의하지 않았다면, 약관 동의 화면을 먼저 띄웁니다.
            startTermActivityForAgreement()
        }
    }

    /**
     * ✨ 4. 약관 동의 액티비티를 시작하는 함수입니다.
     */
    private fun startTermActivityForAgreement() {
        val intent = Intent(this, TermAgreeActivity::class.java).apply {
            // [선택] 민감정보 처리 동의의 ID는 3입니다.
            putExtra("POLICY_ID", 3)
        }
        termAgreementLauncher.launch(intent)
    }

    @Deprecated("This method has been replaced by onSignUpSuccess() and navigateToMain()", ReplaceWith("onSignUpSuccess()"))
    fun completeSignUp() {
        // 이 함수는 더 이상 사용되지 않습니다.
        setResult(RESULT_OK)
        finish()
    }

    fun navigateToMainAndFinish() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // 기존의 모든 액티비티를 스택에서 제거하고 새로운 태스크로 시작
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // SignUpActivity 종료
    }

    fun navigateToCurationAndFinish() {
        val intent = Intent(this, CurationSequenceActivity::class.java).apply {
            // 기존의 모든 액티비티를 스택에서 제거하고 새로운 태스크로 시작
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // SignUpActivity 종료
    }
}