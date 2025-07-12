package com.example.soar.EntryPage.SignUp

import androidx.lifecycle.*
import com.example.soar.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 이메일 화면 전용 상태 */
sealed interface EmailState {
    object Idle      : EmailState
    object Loading   : EmailState
    object MailSent  : EmailState
    object Verified  : EmailState
    data class Error(val msg: String) : EmailState
}

class Step3ViewModel @Inject constructor(
    private val repo: AuthRepository,
    handle: SavedStateHandle       // ← 회전해도 입력값 보존
) : ViewModel() {

    /* ── 입력값 ──────────────────────────────────────────── */
    val email = handle.getLiveData("email", "")
    val code  = handle.getLiveData("code" , "")

    val emailValid: LiveData<Boolean> =
        email.map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
    val codeValid : LiveData<Boolean> = code.map { it.length == 4 }

    /* ── UI 상태 ─────────────────────────────────────────── */
    private val _state = MutableLiveData<EmailState>(EmailState.Idle)
    val state : LiveData<EmailState> = _state

    /* ── 타이머 ──────────────────────────────────────────── */
    private val _millis = MutableStateFlow(0L)
    val millis: StateFlow<Long> = _millis       // collect → MM:SS
    private var timerJob: Job? = null

    private fun startTimer(total: Long = 180_000L) {
        timerJob?.cancel()
        _millis.value = total
        timerJob = viewModelScope.launch {
            ((total - 1_000) downTo 0 step 1_000).asFlow()
                .onEach { delay(1_000) }
                .collect { _millis.value = it }
        }
    }

    /* ── 버튼 활성화 조건 ───────────────────────────────── */
    val canRequestMail = combineLatest(email, emailValid) { addr, valid ->
        valid && addr != _lastRequestedEmail
    }

    val canResend = combineLatest(_state.asFlow().asLiveData(), millis.asLiveData()) { st, ms ->
        st is EmailState.MailSent && ms <= 175_000L || st is EmailState.Error
    }

    val canProceed = combineLatest(emailValid, codeValid) { e, c -> e && c }

    /* ── 최근 요청 이메일 기억 ───────────────────────────── */
    private var _lastRequestedEmail = ""

    /* ── 액션 ───────────────────────────────────────────── */
    fun requestEmail() = viewModelScope.launch {
        _state.value = EmailState.Loading
        repo.requestEmailOtp(email.value!!.trim())
            .onSuccess {
                _lastRequestedEmail = email.value!!.trim()
                _state.value = EmailState.MailSent
                startTimer()
            }
            .onFailure { _state.value = EmailState.Error(it.message ?: "발송 실패") }
    }

    fun verifyCode() = viewModelScope.launch {
        _state.value = EmailState.Loading
        repo.verifyEmailOtp(email.value!!.trim(), code.value!!.trim())
            .onSuccess { _state.value = EmailState.Verified }
            .onFailure { _state.value = EmailState.Error(it.message ?: "코드를 다시 확인하세요.") }
    }

    fun reset() {
        timerJob?.cancel()
        _millis.value = 0
        _state.value  = EmailState.Idle
        _lastRequestedEmail = ""
    }

    override fun onCleared() { timerJob?.cancel(); super.onCleared() }
}

/* ---------- LiveData n-개 → LiveData<R> 헬퍼 ---------- */
private fun <A, B, R> combineLatest(
    la: LiveData<A>, lb: LiveData<B>, block: (A, B) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    var a: A? = null; var b: B? = null
    fun update() { if (a != null && b != null) value = block(a!!, b!!) }
    addSource(la) { a = it; update() }
    addSource(lb) { b = it; update() }
}