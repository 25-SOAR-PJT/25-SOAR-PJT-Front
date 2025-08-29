package com.example.soar.EntryPage.SignUp

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.soar.R
import com.example.soar.databinding.ActivitySignUpBinding

import androidx.core.view.doOnLayout      // ← 추가
import com.example.soar.MainActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val steps = listOf(
        R.id.step1Fragment,
        R.id.step2Fragment,
        R.id.step3Fragment,
        R.id.step4Fragment
    )

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

    fun completeSignUp() {
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
}