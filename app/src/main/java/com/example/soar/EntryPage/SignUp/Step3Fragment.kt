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
import com.example.soar.repository.AuthRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

class Step3Fragment : Fragment(R.layout.step_email_info) {

    private var _b: StepEmailInfoBinding? = null
    private val b get() = _b!!

    private val vm: Step3ViewModel by activityViewModels {
        Step3ViewModelFactory(
            repo  = AuthRepository(RetrofitClient.apiService),
            owner = requireActivity()          // ← 또는 this@Step3Fragment
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
        tillCode .setErrorTextAppearance(R.style.Font_Caption_Regular)

        inputField.isEnabled = false

        etEmail.doAfterTextChanged { vm.email.value = it.toString() }
        inputField.doAfterTextChanged {
            vm.code.value = it.toString()
            codeUnderbar.isSelected = it?.length != 4 && it?.isNotEmpty() == true
        }

        buttonNewcode.setOnClickListener { vm.requestEmail() }
        btnNext       .setOnClickListener { vm.verifyCode() }

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
                isEndIconCheckable = enable
                setEndIconTintList(
                    ContextCompat.getColorStateList(
                        context,
                        if (enable) R.color.ref_blue_500 else R.color.ref_coolgray_200
                    )
                )
                setEndIconOnClickListener(if (enable) { { vm.requestEmail() } } else null)
            }
        }
        vm.canResend  .observe(viewLifecycleOwner) { b.buttonNewcode.isEnabled = it }
        vm.canProceed .observe(viewLifecycleOwner) { b.btnNext.isEnabled      = it }

        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    /* ───────────────────────── UIState 렌더 ────────────────────── */
    private fun render(state: EmailState) = with(b) {
        when (state) {
            EmailState.Idle, EmailState.Loading -> Unit

            EmailState.MailSent -> {
                Snackbar.make(root, R.string.msg_mail_sent, Snackbar.LENGTH_SHORT).show()
                inputField.isEnabled = true
                inputField.requestFocus()
                timerText.visibility = View.VISIBLE
            }

            EmailState.Verified -> findNavController().navigate(R.id.action_step3_to_step4)

            is EmailState.Error -> {
                shake(btnNext)
                helper.showError(tillCode, false, true, state.msg)
                codeUnderbar.isSelected = true
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
        ObjectAnimator.ofFloat(target,"translationX",0f,12f,-12f,9f,-9f,6f,-6f,0f).apply{
            duration = 400; interpolator = CycleInterpolator(1f)
        }.start()

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}