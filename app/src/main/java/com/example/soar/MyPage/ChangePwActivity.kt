package com.example.soar.MyPage

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.soar.Network.RetrofitClient
import com.example.soar.Network.user.AuthRepository
import com.example.soar.R
import com.example.soar.Utill.ErrorMessageHelper
import com.example.soar.Utill.FocusErrorController
import com.example.soar.Utill.PasswordToggleHelper
import com.example.soar.databinding.ActivityChangePwBinding
import com.example.soar.util.showBlockingToast
import com.google.android.material.textfield.TextInputLayout

class ChangePwActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePwBinding

    // ViewModel 초기화 로직을 ViewModelProvider.Factory를 구현하는 람다로 수정
    private val vm: ChangePwViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChangePwViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChangePwViewModel(AuthRepository(RetrofitClient.apiService)) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText 입력값을 ViewModel의 LiveData에 바인딩
        setupTextWatchers()

        findViewById<TextView>(R.id.text_title).text = getString(R.string.change_pw)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnChangePw.isEnabled = false

        /* EditText -> VM */
        binding.etPassword.doAfterTextChanged { vm.newPw.value      = it.toString() }
        binding.etPwCheck  .doAfterTextChanged { vm.newPwCheck.value = it.toString() }

        binding.tilPassword.setErrorTextAppearance(R.style.Font_Caption_Regular)
        binding.tilPwCheck.setErrorTextAppearance(R.style.Font_Caption_Regular)

        PasswordToggleHelper(
            binding.tilPassword, this,
            R.drawable.ic_pw_toggle_active, R.drawable.ic_pw_toggle_inactive
        )
        PasswordToggleHelper(
            binding.tilPwCheck, this,
            R.drawable.ic_pw_toggle_active, R.drawable.ic_pw_toggle_inactive
        )

        setTouchListener(binding.tilPassword, vm.pwTouched)
        setTouchListener(binding.tilPwCheck, vm.pwCheckTouched)

        /* 실시간 스트로크 색상 */
        listOf(binding.etPassword to binding.tilPassword, binding.etPwCheck to binding.tilPwCheck).forEach { (et, til) ->
            et.doAfterTextChanged {
                val defaultClr = if (it.isNullOrBlank()) R.color.ref_gray_200 else R.color.ref_blue_700
                updateStrokeSelector(til, R.color.ref_blue_700, defaultClr)
            }
        }

        val helper = ErrorMessageHelper(
            this,
            R.drawable.ic_error, -3, R.color.ref_red_500
        )

        FocusErrorController(
            binding.etPassword, binding.tilPassword,
            isValid = { vm.pwValid.value == true },
            getErrorMessage = { getString(R.string.error_pw_rule) },
            showError = { msg -> helper.showError(binding.tilPassword, false, true, msg) }
        )
        FocusErrorController(
            binding.etPwCheck, binding.tilPwCheck,
            isValid = { vm.pwCheckValid.value == true },
            getErrorMessage = { getString(R.string.error_pw_mismatch) },
            showError = { msg -> helper.showError(binding.tilPwCheck, false, true, msg) }
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { _, inset ->
            val ime = inset.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.btnChangePw.translationY = if (ime > 0) -ime.toFloat() else 0f
            inset
        }

        vm.canProceed.observe(this) { binding.btnChangePw.isEnabled = it }

        binding.btnChangePw.setOnClickListener {
            vm.updatePw()
        }

        // ViewModel의 UI 상태를 관찰하여 UI 업데이트
        vm.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    Log.d("ChangePwActivity", "비밀번호 변경 중...")
                }
                is UiState.Success -> {
                    showBlockingToast("비밀번호가 성공적으로 변경되었습니다.", hideCancel = true)
                    finish()
                }
                is UiState.Failure -> {
                    showBlockingToast(state.msg, hideCancel = true)
                }
                else -> {
                    // Idle
                }
            }
        }
    }

    private fun setupTextWatchers() {
        binding.etPassword.doAfterTextChanged {
            vm.newPw.value = it.toString()
        }
        binding.etPwCheck.doAfterTextChanged {
            vm.newPwCheck.value = it.toString()
        }
    }

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
}