package com.example.soar.EntryPage.SignUp

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.soar.R
import com.example.soar.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var navController: NavController

    // 단계 ID 배열 – 순서는 진행 순서
    private val steps = listOf(
        R.id.step1Fragment,
        R.id.step2Fragment,
        R.id.step3Fragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host)

        // Fragment 전환될 때마다 호출
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val index = steps.indexOf(destination.id).coerceAtLeast(0)
            animateProgress(index)
        }
    }

    private fun animateProgress(stepIndex: Int) {
        // 0-based index라서 +1
        val targetPercent = (stepIndex + 1f) / steps.size
        val bgWidth = binding.progressBg.width

        // 계산된 px 로 애니메이션
        val targetPx = (bgWidth * targetPercent).toInt()
        val startPx = binding.progressActive.layoutParams.width

        ValueAnimator.ofInt(startPx, targetPx).apply {
            duration = 300           // 원하는 속도
            addUpdateListener { animator ->
                binding.progressActive.layoutParams =
                    binding.progressActive.layoutParams.apply {
                        width = animator.animatedValue as Int
                    }
            }
            start()
        }
    }
}