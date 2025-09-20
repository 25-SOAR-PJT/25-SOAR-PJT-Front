package com.example.soar.MyPage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.user.UpdatePwRequest
import com.example.soar.Utill.combineLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


/* ───────────── UI 상태 ───────────── */
sealed interface UiState {
    object Idle    : UiState
    object Loading : UiState
    object Success : UiState
    data class Failure(val msg: String) : UiState
}

class ChangePwViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    /* ── 입력 ─────────────────────── */
    // 새 비밀번호 입력 LiveData
    val newPw      = MutableLiveData("")
    val newPwCheck = MutableLiveData("")

    val pwTouched      = MutableLiveData(false)
    val pwCheckTouched = MutableLiveData(false)

    /* ── 검증 ─────────────────────── */
    private val RULE = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*]{8,20}$")
    val pwValid      : LiveData<Boolean> = newPw.map { it.matches(RULE) }
    val pwCheckValid : LiveData<Boolean> =
        newPw.combineLatest(newPwCheck) { a, b -> b == a && b.isNotEmpty() }

    val canProceed: LiveData<Boolean> =
        listOf(pwValid, pwCheckValid).combineLatest { list -> list.all { it } }

    /* ── UI 상태 ─────────────────── */
    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> get() = _uiState


    /* ── 비밀번호 업데이트 요청 ─────────────────── */
    fun updatePw() = viewModelScope.launch {
        _uiState.value = UiState.Loading // 요청 시작
        try {
            val body = UpdatePwRequest(
                newPassword     = newPw.value.orEmpty(),
                confirmPassword = newPwCheck.value.orEmpty()
            )
            Log.d("UPDATE_PW", "요청 바디: $body")

            // 1. 비밀번호 업데이트 요청
            repo.updatePw(body)
                .onSuccess {
                    // ✨ 2. 비밀번호 업데이트 성공 시 UI 상태 변경
                    _uiState.value = UiState.Success
                    Log.d("UPDATE_PW", "비밀번호 변경 성공")
                }
                .onFailure { error ->
                    // 비밀번호 업데이트 자체가 실패한 경우
                    _uiState.value = UiState.Failure(error.message ?: "알 수 없는 오류가 발생했습니다.")
                    Log.e("UPDATE_PW", "비밀번호 변경 실패: ${error.message}", error)
                }
        } catch (e: Exception) {
            Log.e("UPDATE_PW", "오류: ${e.message}", e)
            _uiState.value = UiState.Failure(e.message ?: "네트워크 오류")
        }
    }
}