package com.example.soar.EntryPage.SignUp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.soar.R
import com.example.soar.Utill.ErrorMessageHelper
import com.example.soar.Utill.FocusErrorController
import com.example.soar.Utill.FocusErrorControllerGroup
import com.example.soar.databinding.StepBasicInfoBinding
import com.google.android.material.textfield.TextInputLayout

class Step2Fragment : Fragment(R.layout.step_basic_info) {

    private var _binding: StepBasicInfoBinding? = null
    private val b get() = _binding!!
    private val vm: Step2ViewModel by activityViewModels()




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = StepBasicInfoBinding.bind(view)

        b.tilName.setErrorTextAppearance(R.style.Font_Caption_Regular)
        b.tilResidentBirth.setErrorTextAppearance(R.style.Font_Caption_Regular)

        b.etResidentBirth.letterSpacing = 0f


        /* ── 입력 → VM ─────────────────────────────────── */
        b.etName.doAfterTextChanged           { vm.name.value      = it.toString() }
        b.etResidentBirth.doAfterTextChanged { editable ->
            // ① 글자가 있으면 간격을 주고, 없으면 0
            b.etResidentBirth.letterSpacing =
                if (editable.isNullOrEmpty()) 0f else 0.7f   // 0.3~0.4 사이에서 취향대로 조절

            // ② VM 값 반영
            vm.birth.value = editable.toString()

            // ③ 6자리 다 입력되면 성별칸으로 포커스 이동
            if (editable?.length == 6) b.etResidentSex.requestFocus()
        }
        b.etResidentSex.doAfterTextChanged    { vm.sexDigit.value  = it.toString() }

        /* ── touched flag ─────────────────────────────── */
        setTouchFlag(b.tilName,          vm.nameTouched )
        setTouchFlag(b.tilResidentBirth, vm.birthTouched)
        setTouchFlag(b.tilResidentSex,   vm.sexTouched )

        /* ── maxLength ───────────────────────────────── */
        b.etResidentBirth.filters = arrayOf(InputFilter.LengthFilter(6))
        b.etResidentSex.filters   = arrayOf(InputFilter.LengthFilter(1))

        /* ── IME DONE → 다음 ─────────────────────────── */
        b.etResidentSex.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE && vm.canProceed.value == true) {
                b.btnNext.performClick(); true
            } else false
        }

        /* ── 에러 헬퍼 ────────────────────────────────── */
        val helper = ErrorMessageHelper(requireContext(),
            R.drawable.ic_error, -3, R.color.ref_red_500)

        fun showErr(til: TextInputLayout, msg: String) {
            helper.showError(til, false, true, msg)
        }
        /* 이름만 TIL-error 사용 */
        fun showNameErr(msg:String) = helper.showError(b.tilName, false, true, msg)

        /* 주민번호 그룹 전용 TextView */
        fun showResidentErr(msg:String){
            b.tvResidentErr.text = msg
            b.tvResidentErr.visibility = View.VISIBLE
        }
        fun clearResidentErr(){ b.tvResidentErr.visibility = View.GONE }

        /* ── FocusErrorController ────────────────────── */
        FocusErrorController(
            b.etName, b.tilName,
            isValid         = { vm.isNameValid.value == true },
            getErrorMessage = { getString(R.string.error_name_format) },
            showError       = { msg -> showErr(b.tilName, msg) }
        )

        /* ── 주민번호 그룹 포커스 색상 ─────────────────── */
        FocusErrorControllerGroup(
            edits  = listOf(b.etResidentBirth, b.etResidentSex),
            tils   = listOf(b.tilResidentBirth, b.tilResidentSex),
            hyphen = b.ivHyphen,
            defaultColor = requireContext().getColor(R.color.textinput_stroke),
            focusedColor = requireContext().getColor(R.color.ref_blue_700),
            errorColor   = requireContext().getColor(R.color.ref_red_500),

            /* 검증 로직 */
            isValid = { vm.isBirthValid.value == true && vm.isSexValid.value == true },

            /* 메시지 선택 */
            getErrorMessage = {
                if (vm.isBirthValid.value != true)
                    getString(R.string.error_birth_format)
                else
                    getString(R.string.error_sex_digit)
            },

            showError  = ::showResidentErr,
            clearError = ::clearResidentErr
        )

        /* ── 버튼 enable ─────────────────────────────── */
        vm.canProceed.observe(viewLifecycleOwner) { b.btnNext.isEnabled = it }

        /* ── 다음 버튼 ───────────────────────────────── */
        b.btnNext.setOnClickListener {
            b.btnNext.isEnabled = false
            vm.verifyIdentity()
        }

        /* ── 서버 검증 결과 ───────────────────────────── */
        vm.verifyState.observe(viewLifecycleOwner) { st ->
            when (st) {
                VerifyState.Success -> findNavController().navigate(R.id.action_step2_to_step3)

                is VerifyState.Error -> {
                    b.btnNext.isEnabled = true
                    playShake(b.btnNext)
                    showResidentErr(st.msg)
                    vm.resetVerifyState()
                }
                else -> {}
            }
        }

    }

    /* ───────── util ───────── */


    private fun setTouchFlag(til: TextInputLayout, flag: MutableLiveData<Boolean>) {
        til.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) flag.value = true
        }
    }

    private fun playShake(target: View) =
        ObjectAnimator.ofFloat(target, "translationX",
            0f, 12f, -12f, 9f, -9f, 6f, -6f, 0f
        ).apply { duration = 400; interpolator = CycleInterpolator(1f) }.start()

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

/* ---------- LiveData 두 개 combine 헬퍼 ---------- */
fun <A, B> LiveData<A>.combine(
    other: LiveData<B>,
    block: (A, B) -> Unit
) = MediatorLiveData<Unit>().apply {
    var a: A? = null; var b: B? = null
    fun emit() { if (a != null && b != null) block(a!!, b!!) }
    addSource(this@combine) { a = it; emit() }
    addSource(other)        { b = it; emit() }
}