package com.example.soar.EntryPage.SignIn

import android.util.Patterns
import androidx.lifecycle.*
import com.example.soar.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    /* ── ① 입력값 ───────────────────────────── */
    val email    = MutableLiveData("")
    val password = MutableLiveData("")

    /* ── ② touched flag ─────────────────────── */
    val emailTouched    = MutableLiveData(false)
    val passwordTouched = MutableLiveData(false)

    /* ── ③ 개별 유효성 ──────────────────────── */
    val emailValid: LiveData<Boolean> =
        email.map { Patterns.EMAIL_ADDRESS.matcher(it).matches() }

    val pwValid: LiveData<Boolean> =
        password.map { it.length >= 8 }

    /* ── ④ 전체 통과 여부 → 버튼 enable ──────── */
    val canProceed = listOf(emailValid, pwValid)
        .combineLatest { list -> list.all { it } }

    /* ── ⑤ UI 상태 ─────────────────────────── */
    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun login() = viewModelScope.launch {
        _uiState.value = UiState.Loading
        repo.login(email.value.orEmpty(), password.value.orEmpty())
            .onSuccess {
                // 성공 시
                _uiState.value = UiState.Success
            }
            .onFailure { error ->
                // 실패 시 (네트워크 오류, 서버 에러 포함)
                _uiState.value = UiState.Failure(error.message ?: "오류가 발생했습니다.")
            }
    }

    fun loginWithKakao(kakaoToken: String) = viewModelScope.launch {
        _uiState.value = UiState.Loading
        repo.kakaoLogin(kakaoToken)
            .onSuccess {
                _uiState.value = UiState.Success
            }
            .onFailure { error ->
                _uiState.value = UiState.Failure(error.message ?: "카카오 로그인 중 오류가 발생했습니다.")
            }
    }

    fun resetState() { _uiState.value = UiState.Idle }

}

/* ---------- LiveData 확장 ---------- */
fun <T> List<LiveData<T>>.combineLatest(block:(List<T>)->T) : LiveData<T> =
    MediatorLiveData<T>().also { m ->
        val data = MutableList(size){null as T?}
        forEachIndexed { i, src ->
            m.addSource(src){ v -> data[i]=v
                if(data.all{ it!=null }) m.value=block(data.filterNotNull())
            }
        }
    }

/* ---------- VM Factory ---------- */
class LoginViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(repo) as T
}