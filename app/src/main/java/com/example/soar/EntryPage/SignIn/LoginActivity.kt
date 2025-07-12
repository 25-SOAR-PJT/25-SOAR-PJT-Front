package com.example.soar.EntryPage.SignIn

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.CycleInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.soar.EntryPage.SignUp.SignUpActivity
import com.example.soar.EntryPage.Splash.SplashPageActivity
import com.example.soar.MainActivity
import com.example.soar.Network.RetrofitClient
import com.example.soar.R
import com.example.soar.Utill.ErrorMessageHelper
import com.example.soar.Utill.FocusErrorController
import com.example.soar.Utill.PasswordToggleHelper
import com.example.soar.databinding.ActivityLoginPageBinding
import com.example.soar.repository.AuthRepository
import com.google.android.material.textfield.TextInputLayout


/** Login UI state */
sealed interface UiState {
    object Idle    : UiState
    object Loading : UiState
    object Success : UiState
    data class Failure(val msg: String) : UiState
}


class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginPageBinding

    private val vm: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    /* ───────── ActivityResult: 회원가입 → 성공 시 Splash ───────── */
    private val signUpLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK) {
                startActivity(Intent(this, SplashPageActivity::class.java))
                finish()
            }
        }

    /* ───────────────────────── onCreate ───────────────────────── */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.isEnabled = false

        /* 입력값 ↔️ ViewModel 바인딩 */
        b.etEmail   .doAfterTextChanged { vm.email.value    = it.toString() }
        b.etPassword.doAfterTextChanged { vm.password.value = it.toString() }

        b.tilEmail.setErrorTextAppearance(R.style.Font_Caption_Regular)
        b.tilPassword.setErrorTextAppearance(R.style.Font_Caption_Regular)

        PasswordToggleHelper(b.tilPassword, this@LoginActivity,
            R.drawable.ic_pw_toggle_active, R.drawable.ic_pw_toggle_inactive)


        /* touched flag */
        setTouchListener(b.tilEmail   , vm.emailTouched)
        setTouchListener(b.tilPassword, vm.passwordTouched)

        /* 실시간 스트로크 색상 */
        listOf(
            b.etEmail    to b.tilEmail,
            b.etPassword to b.tilPassword
        ).forEach { (et, til) ->
            et.doAfterTextChanged {
                val defaultClr = if (it.isNullOrBlank()) R.color.ref_gray_200
                else R.color.ref_blue_700
                updateStrokeSelector(til, R.color.ref_blue_700, defaultClr)
            }
        }

        /* 에러 헬퍼 / 포커스 에러 컨트롤러 */
        val helper = ErrorMessageHelper(
            this, R.drawable.ic_error, -3, R.color.ref_red_500
        )
        FocusErrorController(
            b.etEmail, b.tilEmail,
            isValid = { vm.emailValid.value == true },
            getErrorMessage = { getString(R.string.error_email) },
            showError = { msg -> helper.showError(b.tilEmail, false, true, msg) }
        )
        FocusErrorController(
            b.etPassword, b.tilPassword,
            isValid = { vm.pwValid.value == true },
            getErrorMessage = { getString(R.string.error_password) },
            showError = { msg -> helper.showError(b.tilPassword, false, true, msg) }
        )


        /* 버튼 enable */
        vm.canProceed.observe(this) { b.btnLogin.isEnabled = it }

        /* 로그인 시도 */
        b.btnLogin.setOnClickListener { vm.login() }

        /* 상단 X 버튼 */
        b.appbarLogin.btnClose.setOnClickListener { finish() }

        /* 회원가입 이동 */
        b.tvSignUp.setOnClickListener {
            signUpLauncher.launch(Intent(this, SignUpActivity::class.java))
        }


        /* UI 상태머신 */
        vm.uiState.observe(this) { st ->
            when (st) {
                UiState.Idle    -> showLoading(false)
                UiState.Loading -> showLoading(true)
                UiState.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is UiState.Failure -> {
                    shake(b.btnLogin)
                    vm.resetState()
                }
            }
        }
    }

    /* ───────────── helper ───────────── */

    private fun showLoading(show: Boolean) {
        b.btnLogin.isEnabled = !show
    }

    private fun shake(target: View) =
        ObjectAnimator.ofFloat(
            target, "translationX",
            0f, 16f, -16f, 12f, -12f, 6f, -6f, 0f
        ).apply {
            duration = 450
            interpolator = CycleInterpolator(1f)
        }.start()

    private fun setTouchListener(til: TextInputLayout, flag: MutableLiveData<Boolean>) {
        til.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) flag.value = true
        }
    }

    private fun updateStrokeSelector(
        til: TextInputLayout, focusedColorRes: Int, defaultColorRes: Int
    ) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf()
        )
        val colors = intArrayOf(
            ContextCompat.getColor(til.context, focusedColorRes),
            ContextCompat.getColor(til.context, defaultColorRes)
        )
        til.setBoxStrokeColorStateList(android.content.res.ColorStateList(states, colors))
    }
}