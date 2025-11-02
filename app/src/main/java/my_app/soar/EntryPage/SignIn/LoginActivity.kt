package my_app.soar.EntryPage.SignIn

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
import my_app.soar.EntryPage.SignUp.SignUpActivity
import my_app.soar.EntryPage.Splash.SplashPageActivity
import my_app.soar.MainActivity
import my_app.soar.Network.RetrofitClient
import my_app.soar.R
import my_app.soar.Utill.ErrorMessageHelper
import my_app.soar.Utill.FocusErrorController
import my_app.soar.Utill.PasswordToggleHelper
import my_app.soar.databinding.ActivityLoginPageBinding
import my_app.soar.Network.user.AuthRepository
import com.google.android.material.textfield.TextInputLayout
import android.util.Log
import my_app.soar.MyPage.FindActivity
import my_app.soar.Network.ApiResponse
import my_app.soar.Network.TokenManager
import my_app.soar.Network.user.KakaoLoginRequest
import my_app.soar.Network.user.SignInResponse
import my_app.soar.Utill.TermAgreeActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import my_app.soar.util.showBlockingToast


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

    // ✨ 1. 최초 소셜 로그인 시 TermAgreeActivity를 실행하고 결과를 받기 위한 Launcher 추가
    private val termAgreementLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 약관 동의 화면이 닫히면 (동의/비동의 무관) 메인 액티비티로 이동합니다.
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /* ───────────────────────── onCreate ───────────────────────── */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.isEnabled = false

        // 비밀번호 / 아이디 찾기 페이지 연결
        b.tvFindAccount.setOnClickListener{
            val intent = Intent(this, FindActivity::class.java)
            startActivity(intent)
        }

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
        b.appbarLogin.btnClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                // 기존의 모든 액티비티를 스택에서 제거하고 새로운 태스크로 시작
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish() // LoginActivity 종료

        }

        /* 회원가입 이동 */
        b.tvSignUp.setOnClickListener {
            signUpLauncher.launch(Intent(this, SignUpActivity::class.java))
        }

        b.btnKakao.setOnClickListener {
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java).apply {
            // Clear the entire activity stack and start a new task
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Finish the current LoginActivity
    }

    // ✅ 로그인 성공/실패 후 처리
    private fun handleKakaoLogin(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Log.e("LoginActivity", "카카오톡으로 로그인 실패", error)
            showBlockingToast("카카오 로그인 실패: ${error.message}", long = false, hideCancel = true)
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

                            // ✨ 2. 최초 소셜 로그인 여부에 따라 분기 처리
                            if (data.firstSocialLogin == true && data.socialProvider == "kakao") {
                                // 최초 로그인 시 -> 약관 동의 화면으로 이동
                                val intent = Intent(this@LoginActivity, TermAgreeActivity::class.java).apply {
                                    // [선택] 민감정보 처리 동의의 ID는 3
                                    putExtra("POLICY_ID", 3)
                                }
                                termAgreementLauncher.launch(intent)
                            } else {
                                // 최초 로그인이 아닐 시 -> 바로 메인 화면으로 이동
                                showBlockingToast("로그인 성공", long = false, hideCancel = true)
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }

                            TokenManager.saveIsKakaoUser(true)
                            showBlockingToast("로그인 성공", long = false, hideCancel = true)
                            if (data.firstSocialLogin == true && data.socialProvider == "kakao") {
                                my_app.soar.util.TouchBlockingToast.show(
                                    activity = this@LoginActivity,
                                    message = "소셜 계정 최초 로그인입니다. 설정 메뉴에서 기본 정보를 지정해주세요.",
                                    long = true,
                                    cancelText = "확인",
                                    hideCancel = false         // 버튼 노출
                                )
                            }
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("KakaoLoginActivity", "🚨 로그인 실패: data가 null")
                            showBlockingToast("로그인 실패: 서버 응답 없음", long = false, hideCancel = true)
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("KakaoLoginActivity", "🚨 서버 오류: $errorMessage")
                        showBlockingToast("서버 오류: $errorMessage", long = false, hideCancel = true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("KakaoLoginActivity", "🚨 예외 발생: ${e.message}", e)
                    showBlockingToast("예외 발생: ${e.message}", long = false, hideCancel = true)
                }
            }
        }
    }


}