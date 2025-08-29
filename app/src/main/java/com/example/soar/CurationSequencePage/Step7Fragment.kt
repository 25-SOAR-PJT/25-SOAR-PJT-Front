package com.example.soar.CurationSequencePage

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.soar.R
import com.example.soar.databinding.StepCsLoadingSuggestionBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Step7Fragment : Fragment(R.layout.step_cs_loading_suggestion) {

    private var _binding: StepCsLoadingSuggestionBinding? = null
    private val b get() = _binding!!

    private val activityViewModel: CurationSequenceViewModel by activityViewModels()

    // 시작/종료 제어를 위해 모아두기
    private val runningAnimators = mutableListOf<Animator>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StepCsLoadingSuggestionBinding.bind(view)

        setupProgressHeader()

        activityViewModel.userName.observe(viewLifecycleOwner) { name ->
            b.tvTitle.text = getString(R.string.cs_loading_tv, name)
        }

        b.root.doOnPreDraw { startPulseAnimations() }

        // ✅ 수정: 고정된 지연 시간을 제거하고 ViewModel의 로딩 상태를 관찰
        setupObservers()
    }

    private fun setupProgressHeader() {
        b.progressBg.tvProgress.text = "잠시만 기다려주세요"
        b.progressBg.tvProgressDescription.visibility = View.GONE
    }

    private fun setupObservers() {
        // ✨추가: 로딩 상태 LiveData를 관찰
        viewLifecycleOwner.lifecycleScope.launch {

            delay(2200)
            activityViewModel.isLoadingPolicies.observe(viewLifecycleOwner) { isLoading ->
                // 로딩이 완료되면 다음 프래그먼트로 이동

                if (!isLoading) {
                    // 프래그먼트가 아직 액티비티에 연결되어 있는지 확인 후 이동
                    if (isAdded) {

                        findNavController().navigate(R.id.action_step7_to_step8)
                    }
                }
            }
        }
    }

    private fun startPulseAnimations() {
        fun pulse(
            target: View,
            fromScale: Float,
            toScale: Float,
            fromAlpha: Float,
            toAlpha: Float,
            duration: Long,
            startDelay: Long,
            restart: Boolean = true
        ) {
            target.scaleX = fromScale
            target.scaleY = fromScale
            target.alpha = fromAlpha

            val repeatMode = if (restart) ValueAnimator.RESTART else ValueAnimator.REVERSE

            val sx = ObjectAnimator.ofFloat(target, View.SCALE_X, fromScale, toScale).apply {
                this.duration = duration
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                this.repeatMode = repeatMode
                interpolator = FastOutSlowInInterpolator()
            }
            val sy = ObjectAnimator.ofFloat(target, View.SCALE_Y, fromScale, toScale).apply {
                this.duration = duration
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                this.repeatMode = repeatMode
                interpolator = FastOutSlowInInterpolator()
            }
            val a = ObjectAnimator.ofFloat(target, View.ALPHA, fromAlpha, toAlpha).apply {
                this.duration = duration
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                this.repeatMode = repeatMode
                interpolator = LinearInterpolator()
            }

            AnimatorSet().apply {
                playTogether(sx, sy, a)
                start()
                runningAnimators += listOf(this, sx, sy, a)
            }
        }

        pulse(b.ellipse1871, 0.95f, 1.15f, 0.35f, 0.85f, 1000L, 0L, restart = false)
        pulse(b.ellipse1870, 0.98f, 1.12f, 0.40f, 0.90f, 800L, 200L, restart = false)
        pulse(b.ellipse1869, 1.00f, 1.08f, 0.60f, 1.00f, 1600L, 400L, restart = false)

        listOf(b.ellipse1871, b.ellipse1870, b.ellipse1869).forEachIndexed { i, v ->
            ObjectAnimator.ofFloat(v, View.ROTATION, 0f, 360f).apply {
                duration = 4000L; startDelay = (i * 120).toLong()
                repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator(); start()
                runningAnimators += this
            }
        }
    }

    override fun onDestroyView() {
        runningAnimators.forEach { runCatching { it.cancel() } }
        runningAnimators.clear()
        _binding = null
        super.onDestroyView()
    }
}