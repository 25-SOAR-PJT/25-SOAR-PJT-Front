package com.example.soar.EntryPage.SignIn

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.CycleInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
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
import android.util.Log
import android.widget.Toast
import com.example.soar.Network.ApiResponse
import com.example.soar.Network.TokenManager
import com.example.soar.Network.user.KakaoLoginRequest
import com.example.soar.Network.user.SignInResponse
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


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
        b.etPassword.doAfterTextChanged {
            vm.password.value = it.toString()
        }

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
        vm.canProceed.observe(this) { canProceed ->
            // 로딩 중이 아닐 때만 유효성에 따라 버튼 상태 변경
            if (vm.uiState.value !is UiState.Loading) {
                b.btnLogin.isEnabled = canProceed
            }
        }

        /* 로그인 시도 */
        b.btnLogin.setOnClickListener { vm.login() }

        /* 상단 X 버튼 */
        b.appbarLogin.btnClose.setOnClickListener { finish() }

        /* 회원가입 이동 */
        b.tvSignUp.setOnClickListener {
            signUpLauncher.launch(Intent(this, SignUpActivity::class.java))
        }

        b.btnKakao.setOnClickListener {
            //handleKakaoLogin()
            kakaoLogin()
        }


        /* UI 상태머신 */
        vm.uiState.observe(this) { st ->
            when (st) {
                UiState.Idle    -> showLoading(false)
                UiState.Loading -> showLoading(true)
                UiState.Success -> {
                    showLoading(false)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is UiState.Failure -> {
                    showLoading(false)
                    shake(b.btnLogin)
                    // ✨ [반영된 로직] 로그인 실패 시 비밀번호 필드 아래에 에러 메시지 표시
                    helper.showError(b.tilPassword, false, true, st.msg)
                    vm.resetState()
                }
            }
        }
    }

    /* ───────────── helper ───────────── */

    private fun showLoading(show: Boolean) {
        if (show) {
            // 로딩 중일 때는 무조건 비활성화
            b.btnLogin.isEnabled = false
        } else {
            // 로딩이 끝나면 유효성 검사 결과에 따라 버튼 상태 복원
            b.btnLogin.isEnabled = vm.canProceed.value ?: false
        }
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

    private fun handleKakaoLogin() {
        // 카카오계정으로 로그인 공통 콜백
        val callback: (OAuthToken?, Throwable?) -> Unit = callback@{ token, error ->
            if (error != null) {
                Log.e("LoginActivity", "카카오계정으로 로그인 실패", error)
                // 사용자가 로그인 창을 닫으면 ClientError(ClientErrorCause.Cancelled) 발생
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    // 특별한 처리가 필요 없다면 무시
                    return@callback
                }
                // 그 외 에러는 사용자에게 피드백
                vm.resetState() // UI 상태 초기화
            } else if (token != null) {
                Log.i("LoginActivity", "카카오계정으로 로그인 성공 ${token.accessToken}")
                // ViewModel을 통해 서버에 로그인 요청
                vm.loginWithKakao(token.accessToken)
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("LoginActivity", "카카오톡으로 로그인 실패", error)
                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i("LoginActivity", "카카오톡으로 로그인 성공 ${token.accessToken}")
                    vm.loginWithKakao(token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }


    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                handleKakaoLogin(token, error)
            }
        }
    }

    // ✅ 로그인 성공/실패 후 처리
    private fun handleKakaoLogin(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Log.e("LoginActivity", "카카오톡으로 로그인 실패", error)
            Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            Log.d("KakaoLoginActivity", "✅ 카카오 로그인 성공! 토큰: ${token.accessToken}")
            sendTokenToServer(token.accessToken)
        }
    }

    // ✅ 서버로 카카오 accessToken 전송
    private fun sendTokenToServer(accessToken: String) {
        Log.d("KakaoLoginActivity", "🚀 서버로 카카오 로그인 요청 중... 토큰: $accessToken")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<ApiResponse<SignInResponse>> =
                    RetrofitClient.apiService.kakaoSignIn(KakaoLoginRequest(accessToken))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val data = responseBody?.data

                        if (data != null) {
                            Log.d("KakaoLoginActivity", "✅ 서버 응답: 로그인 성공")
                            Log.d("KakaoLoginActivity", "✅ 받은 accessToken: ${data.accessToken}")
                            Log.d("KakaoLoginActivity", "✅ 받은 refreshToken: ${data.refreshToken}")

                            TokenManager.saveAccessToken(data.accessToken ?: "")
                            TokenManager.saveRefreshToken(data.refreshToken ?: "")
                            TokenManager.saveUserId(data.userId)

                            Log.d("KakaoLoginActivity", "✅ 저장된 accessToken 확인용: ${TokenManager.getAccessToken()}")

                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            if (data.firstSocialLogin == true && data.socialProvider == "kakao") {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "소셜 계정 최초 로그인입니다. 설정 메뉴에서 기본 정보를 지정해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("KakaoLoginActivity", "🚨 로그인 실패: data가 null")
                            Toast.makeText(this@LoginActivity, "로그인 실패: 서버 응답 없음", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("KakaoLoginActivity", "🚨 서버 오류: $errorMessage")
                        Toast.makeText(this@LoginActivity, "서버 오류: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("KakaoLoginActivity", "🚨 예외 발생: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}