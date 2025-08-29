package com.example.soar.EntryPage.SignUp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.CycleInterpolator
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.soar.Network.RetrofitClient
import com.example.soar.R
import com.example.soar.Utill.ErrorMessageHelper
import com.example.soar.Utill.FocusErrorController
import com.example.soar.databinding.StepEmailInfoBinding
import com.example.soar.Network.user.AuthRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit


class Step3Fragment : Fragment(R.layout.step_email_info) {

    private var _b: StepEmailInfoBinding? = null
    private val b get() = _b!!

    // 💡 1. 코드 관련 API 에러 발생 여부를 저장하는 플래그
    private var isCodeApiError = false
    private val vm: Step3ViewModel by activityViewModels {
        Step3ViewModelFactory(
            repo = AuthRepository(RetrofitClient.apiService),
            owner = requireActivity() // ← 또는 this@Step3Fragment
        )
    }
    private val vmStep2: Step2ViewModel by activityViewModels()
    private lateinit var helper: ErrorMessageHelper

    override fun onViewCreated(v: View, s: Bundle?) {
        _b = StepEmailInfoBinding.bind(v)
        helper = ErrorMessageHelper(requireContext(), R.drawable.ic_error, -3, R.color.ref_red_500)

        initInputs()
        observeVm()
        setWindowInsetAdjust()
        handleBackPress()
    }

    /* ───────────────────────── 초기화 ───────────────────────── */
    private fun initInputs() = with(b) {
        tillEmail.setErrorTextAppearance(R.style.Font_Caption_Regular)
        tillCode.setErrorTextAppearance(R.style.Font_Caption_Regular)

        inputField.isEnabled = false
        codeUnderbar.isEnabled = false

        etEmail.doAfterTextChanged { vm.email.value = it.toString() }

        // 💡 2. inputField의 텍스트 변경 리스너 수정
        inputField.doAfterTextChanged { text ->
            // API 에러 상태에서 사용자가 입력을 시작하면,
            if (isCodeApiError) {
                isCodeApiError = false          // 에러 플래그를 해제하고
                codeUnderbar.isSelected = false // 에러 상태(빨간색)를 일반 상태로 되돌림
                codeUnderbar.isActivated = true // 💡 추가: 즉시 포커스 상태(파란색)로 변경
            }
            vm.code.value = text.toString()
        }

        // 💡 3. inputField에 포커스 변경 리스너 추가
        inputField.setOnFocusChangeListener { _, hasFocus ->

            // API 에러 상태가 아닐 때만 포커스 색상(파란색/회색)을 제어
            if (!isCodeApiError) {
                codeUnderbar.isActivated = hasFocus
            }
        }

        buttonNewcode.setOnClickListener { vm.requestEmail() }
        btnNext.setOnClickListener { vm.verifyCode() }

        /* 포커스/에러 컨트롤러 재사용 */
        FocusErrorController(
            etEmail, tillEmail,
            isValid = { vm.emailValid.value == true },
            getErrorMessage = { getString(R.string.error_email_form) },
            showError = { msg -> helper.showError(tillEmail, false, true, msg) }
        )
    }

    /* ───────────────────────── 관찰 ───────────────────────── */
    private fun observeVm() {

        /* 타이머 */
        lifecycleScope.launchWhenStarted {
            vm.millis.flowWithLifecycle(lifecycle).collectLatest { ms ->
                val mm = TimeUnit.MILLISECONDS.toMinutes(ms)
                val ss = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                b.timerText.text = "%02d:%02d".format(mm, ss)
            }
        }

        vm.canRequestMail.observe(viewLifecycleOwner) { enable ->
            b.tillEmail.apply {
                // 클릭 리스너: enable=true 때만 등록
                setEndIconOnClickListener(if (enable) { _ -> vm.requestEmail() } else null)
                isEndIconCheckable = enable
                setEndIconTintList(
                    ContextCompat.getColorStateList(
                        context,
                        if (enable) R.color.ref_blue_500 else R.color.ref_coolgray_200
                    )
                )
            }
        }

        vm.canResend.observe(viewLifecycleOwner) { b.buttonNewcode.isEnabled = it }
        vm.canProceed.observe(viewLifecycleOwner) { b.btnNext.isEnabled = it }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }


    /* ───────────────────────── UIState 렌더 ────────────────────── */
    private fun render(state: EmailState) = with(b) {
        tillEmail.error = null
        tillCode.error = null
        // 💡 render 호출 시 isSelected는 초기화하되, isActivated는 포커스 상태에 따르므로 여기서 건드리지 않습니다.
        codeUnderbar.isSelected = false

        when (state) {
            EmailState.Idle, EmailState.Loading -> Unit

            EmailState.MailSent -> {
                isCodeApiError = false
                Snackbar.make(root, R.string.msg_mail_sent, Snackbar.LENGTH_SHORT).show()
                inputField.isEnabled = true
                codeUnderbar.isEnabled = true // 💡 추가: 언더바도 함께 활성화
                inputField.requestFocus()
                timerText.visibility = View.VISIBLE
            }

            EmailState.Verified -> findNavController().navigate(R.id.action_step3_to_step4)

            is EmailState.Error -> {
                when (state.source) {
                    ErrorSource.EMAIL -> {
                        shake(btnNext)
                        helper.showError(tillEmail, false, true, state.msg)
                    }

                    ErrorSource.CODE -> {
                        isCodeApiError = true // 💡 API 에러 발생 플래그 설정
                        shake(btnNext)
                        helper.showError(tillCode, false, true, state.msg)
                        codeUnderbar.isSelected = true // 💡 에러 상태(빨간색)로 변경
                        codeUnderbar.isActivated = false // 포커스 상태는 비활성화
                    }
                }
            }
        }
    }

    /* ───────────────────────── 기타 헬퍼 ──────────────────────── */
    private fun setWindowInsetAdjust() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, inset ->
            val ime = inset.getInsets(WindowInsetsCompat.Type.ime()).bottom
            b.btnNext.translationY = if (ime > 0) -ime.toFloat() else 0f
            inset
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            vm.reset(); isEnabled = false
            vmStep2.resetVerifyState()
            findNavController().popBackStack()
        }
    }

    private fun shake(target: View) =
        ObjectAnimator.ofFloat(target, "translationX", 0f, 12f, -12f, 9f, -9f, 6f, -6f, 0f).apply {
            duration = 400; interpolator = CycleInterpolator(1f)
        }.start()

    override fun onDestroyView() {
        super.onDestroyView(); _b = null
    }
}