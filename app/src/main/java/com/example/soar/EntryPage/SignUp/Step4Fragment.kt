package com.example.soar.EntryPage.SignUp

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
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
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.soar.Network.RetrofitClient
import com.example.soar.R
import com.example.soar.databinding.StepPwInfoBinding
import com.example.soar.repository.AuthRepository
import com.google.android.material.textfield.TextInputLayout
import com.example.soar.Utill.ErrorMessageHelper
import com.example.soar.Utill.FocusErrorController
import com.example.soar.Utill.PasswordToggleHelper

class Step4Fragment : Fragment(R.layout.step_pw_info) {

    private var _binding: StepPwInfoBinding? = null
    private val b get() = _binding!!

    private val vmStep2: Step2ViewModel   by activityViewModels()
    private val vmStep3: Step3ViewModel   by activityViewModels()
    private val vmPolicy: Step1ViewModel by activityViewModels()

    private val vm: Step4ViewModel by activityViewModels {
        Step4ViewModelFactory(
            repo  = AuthRepository(RetrofitClient.apiService),
            owner = requireActivity()
        )
    }

    /* ─────────────────────────────── */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = StepPwInfoBinding.bind(view)

        /* 버튼 초기 상태 */
        b.btnNext.isEnabled = false

        /* EditText -> VM */
        b.etPassword.doAfterTextChanged { vm.pw.value      = it.toString() }
        b.etPwCheck  .doAfterTextChanged { vm.pwCheck.value = it.toString() }

        /* TIL 에러 텍스트 스타일 */
        b.tilPassword.setErrorTextAppearance(R.style.Font_Caption_Regular)
        b.tilPwCheck .setErrorTextAppearance(R.style.Font_Caption_Regular)

        /* 비밀번호 토글 */
        PasswordToggleHelper(b.tilPassword, requireContext(),
            R.drawable.ic_pw_toggle_active, R.drawable.ic_pw_toggle_inactive)
        PasswordToggleHelper(b.tilPwCheck , requireContext(),
            R.drawable.ic_pw_toggle_active, R.drawable.ic_pw_toggle_inactive)

        /* touched flag */
        setTouchListener(b.tilPassword, vm.pwTouched)
        setTouchListener(b.tilPwCheck , vm.pwCheckTouched)

        /* 실시간 스트로크 색상 */
        listOf(b.etPassword to b.tilPassword, b.etPwCheck to b.tilPwCheck).forEach { (et, til) ->
            et.doAfterTextChanged {
                val defaultClr = if (it.isNullOrBlank()) R.color.ref_gray_200 else R.color.ref_blue_700
                updateStrokeSelector(til, R.color.ref_blue_700, defaultClr)
            }
        }

        /* 에러 헬퍼 */
        val helper = ErrorMessageHelper(requireContext(),
            R.drawable.ic_error, -3, R.color.ref_red_500)

        FocusErrorController(
            b.etPassword, b.tilPassword,
            isValid = { vm.pwValid.value == true },
            getErrorMessage = { getString(R.string.error_pw_rule) },
            showError = { msg -> helper.showError(b.tilPassword, false, true, msg) }
        )
        FocusErrorController(
            b.etPwCheck, b.tilPwCheck,
            isValid = { vm.pwCheckValid.value == true },
            getErrorMessage = { getString(R.string.error_pw_mismatch) },
            showError = { msg -> helper.showError(b.tilPwCheck, false, true, msg) }
        )

        /* IME 인셋 처리 */
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, inset ->
            val ime = inset.getInsets(WindowInsetsCompat.Type.ime()).bottom
            b.btnNext.translationY = if (ime > 0) -ime.toFloat() else 0f
            inset
        }

        /* 버튼 enable */
        vm.canProceed.observe(viewLifecycleOwner) { b.btnNext.isEnabled = it }

        /* 가입 완료 */
        b.btnNext.setOnClickListener {
            vm.signUp(
                name     = vmStep2.name.value.orEmpty(),
                birth    = vmStep2.birth.value.orEmpty(),
                sexDigit = vmStep2.sexDigit.value.orEmpty(),
                email    = vmStep3.email.value.orEmpty(),
                otp      = vmStep3.code.value.orEmpty(),
                terms    = vmPolicy.items.value?.map { it.checked } ?: emptyList()
            )
        }

        /* 뒤로가기 */
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            vm.resetState()
            vmStep3.reset()
            isEnabled = false
            findNavController().popBackStack()
        }

        /* UI 상태머신 */
        vm.uiState.observe(viewLifecycleOwner) { st ->
            when (st) {
                UiState.Idle    -> showLoading(false)
                UiState.Loading -> showLoading(true)
                UiState.Success -> (requireActivity() as SignUpActivity).navigateToMainAndFinish()
                is UiState.Failure -> {
                    shake(b.btnNext)
                    helper.showError(b.tilPwCheck, false, true, st.msg)
                    vm.resetState()
                }
            }
        }
    }

    /* ───────────── helper ───────────── */

    private fun showLoading(show:Boolean){ b.btnNext.isEnabled = !show }

    private fun shake(target: View) =
        ObjectAnimator.ofFloat(target,"translationX",
            0f,16f,-16f,12f,-12f,6f,-6f,0f
        ).apply { duration=450; interpolator=CycleInterpolator(1f) }.start()

    private fun setTouchListener(til:TextInputLayout, flag:MutableLiveData<Boolean>){
        til.editText?.setOnFocusChangeListener {_,hasFocus-> if(!hasFocus) flag.value=true }
    }

    private fun updateStrokeSelector(
        til: TextInputLayout, focusedColorRes:Int, defaultColorRes:Int
    ){
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused), intArrayOf()
        )
        val colors = intArrayOf(
            ContextCompat.getColor(til.context, focusedColorRes),
            ContextCompat.getColor(til.context, defaultColorRes)
        )
        til.setBoxStrokeColorStateList(ColorStateList(states, colors))
    }

    override fun onDestroyView(){ super.onDestroyView(); _binding = null }
}