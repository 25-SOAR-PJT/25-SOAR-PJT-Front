package com.example.soar.EntryPage.SignUp

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.soar.Network.user.SignUpRequest
import com.example.soar.Utill.combineLatest
import com.example.soar.Network.user.AuthRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/* ───────────── UI 상태 ───────────── */
sealed interface UiState {
    object Idle    : UiState
    object Loading : UiState
    object Success : UiState
    data class Failure(val msg: String) : UiState
}

class Step4ViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val handle: SavedStateHandle     // 회전 시 값 유지
) : ViewModel() {

    /* ── 입력 ─────────────────────── */
    val pw      = handle.getLiveData("pw"     , "")
    val pwCheck = handle.getLiveData("pwCheck", "")

    val pwTouched      = MutableLiveData(false)
    val pwCheckTouched = MutableLiveData(false)

    /* ── 검증 ─────────────────────── */
    private val RULE = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*]{8,20}$")
    val pwValid      : LiveData<Boolean> = pw.map { it.matches(RULE) }
    val pwCheckValid : LiveData<Boolean> =
        pw.combineLatest(pwCheck) { a, b -> b == a && b.isNotEmpty() }

    val canProceed: LiveData<Boolean> =
        listOf(pwValid, pwCheckValid).combineLatest { list -> list.all { it } }

    /* ── UI 상태 ───────────────────── */
    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState : LiveData<UiState> = _uiState

    /* ── 가입 요청 ─────────────────── */
    fun signUp(
        name: String,
        birth: String,
        sexDigit: String,
        email: String,
        otp: String,
        terms: List<Boolean>
    ) = viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val body = SignUpRequest(
                userName        = name,
                userBirthDate   = toIsoDate(birth, sexDigit),
                userPhoneNumber = "010-0000-0000",           // TODO
                userGender      = sexDigit in listOf("1","3"),
                userEmail       = email,
                userPassword    = pw.value.orEmpty(),
                otp             = otp,
                agreedTerms     = terms
            )
            Log.d("SOAR_SIGNUP", "요청 바디: $body")
            // 1. 회원가입 요청
            repo.signUp(body)
                .onSuccess {
                    // ✨ 2. 회원가입 성공 시, 즉시 자동 로그인 시도
                    val loginPassword = pw.value.orEmpty()
                    repo.login(email, loginPassword)
                        .onSuccess {
                            // 3. 자동 로그인까지 성공하면 최종 성공 상태로 변경
                            _uiState.value = UiState.Success
                        }
                        .onFailure { loginError ->
                            // 로그인은 실패한 경우 (네트워크 문제 등)
                            throw Exception("회원가입은 성공했으나, 자동 로그인에 실패했습니다. (${loginError.message})")
                        }
                }
                .onFailure { signUpError ->
                    // 회원가입 자체가 실패한 경우
                    throw signUpError
                }
        } catch (e: Exception) {
            Log.e("SOAR_SIGNUP", "오류: ${e.message}", e)
            _uiState.value = UiState.Failure(e.message ?: "네트워크 오류")
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }

    /* YYMMDD + 성별코드 → ISO 날짜 */
    private fun toIsoDate(yyMMdd: String, sexDigit: String?): String {
        val yy = yyMMdd.substring(0, 2).toInt()
        val mm = yyMMdd.substring(2, 4).toInt()
        val dd = yyMMdd.substring(4, 6).toInt()
        val century = when (sexDigit) {
            "1","2" -> 1900
            "3","4" -> 2000
            "9"     -> 1800
            else    -> if (yy > LocalDate.now().year % 100) 1900 else 2000
        }
        return "%04d-%02d-%02d".format(century + yy, mm, dd)
    }
}

class Step4ViewModelFactory(
    private val repo: AuthRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        state: SavedStateHandle
    ): T = Step4ViewModel(repo, state) as T
}