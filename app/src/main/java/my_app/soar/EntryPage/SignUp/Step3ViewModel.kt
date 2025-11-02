
package my_app.soar.EntryPage.SignUp

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import my_app.soar.Network.user.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// 💡 1. 에러 출처를 구분하기 위한 enum 클래스 추가
enum class ErrorSource {
    EMAIL, CODE
}

enum class LoadingFor { REQUEST_EMAIL, VERIFY_CODE }

/** 이메일 화면 전용 상태 */
sealed interface EmailState {
    object Idle : EmailState
    object Loading : EmailState
    object MailSent : EmailState
    object Verified : EmailState
    // 💡 2. Error 상태가 에러 출처(source)를 갖도록 수정
    data class Error(val msg: String, val source: ErrorSource) : EmailState
}

class Step3ViewModel @Inject constructor(
    private val repo: AuthRepository,
    handle: SavedStateHandle // ← 회전해도 입력값 보존
) : ViewModel() {

    /* ── 입력값 ──────────────────────────────────────────── */
    val email = handle.getLiveData("email", "")
    val code = handle.getLiveData("code", "")
    val emailValid: LiveData<Boolean> =
        email.map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
    val codeValid: LiveData<Boolean> = code.map { it.length == 4 }

    private val _loadingFor = MutableLiveData<LoadingFor?>(null)
    val loadingFor: LiveData<LoadingFor?> = _loadingFor

    /* 최근 요청한 이메일 → Flow 로 변경 */
    private val _lastRequestedEmail = MutableStateFlow("")

    /* ── UI 상태 ─────────────────────────────────────────── */
    private val _state = MutableLiveData<EmailState>(EmailState.Idle)
    val state: LiveData<EmailState> = _state

    /* ── 타이머 ──────────────────────────────────────────── */
    private val _millis = MutableStateFlow(0L)
    val millis: StateFlow<Long> = _millis // collect → MM:SS
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

    /* ───────── 버튼 활성화 ───────── */
    /** ① 인증메일 요청 아이콘 */
    val canRequestMail: LiveData<Boolean> = combine(
        email.asFlow(),
        emailValid.asFlow(),
        _lastRequestedEmail
    ) { addr, valid, last ->
        valid && addr.trim() != last // 주소가 바뀌어야 재요청 가능
    }.asLiveData()

    /** ② 재전송 버튼 */
    val canResend: LiveData<Boolean> = combine(
        _state.asFlow(),
        millis
    ) { st, ms ->
        // 💡 수정: 'when'을 사용해 상태별로 조건을 명확히 분리합니다.
        when (st) {
            // 상태가 MailSent일 때 (== inputField가 활성화됐을 때)
            // 타이머가 5초 이상 지났는지 확인합니다.
            is EmailState.MailSent -> ms <= 175_000L

            // 상태가 Error일 때
            is EmailState.Error -> {

                // "이미 존재하는 이메일입니다." 메시지가 아닐 때만 true를 반환
                st.msg != "이미 존재하는 이메일입니다."
            }

            // 그 외 모든 상태에서는 비활성화합니다.
            else -> false
        }
    }.asLiveData()

    /** ③ 다음 버튼 */
    val canProceed: LiveData<Boolean> =
        combine(emailValid.asFlow(), codeValid.asFlow()) { e, c -> e && c }.asLiveData()

    /* ───────── 액션 ───────── */
    fun requestEmail() = viewModelScope.launch {
        _loadingFor.value = LoadingFor.REQUEST_EMAIL
        _state.value = EmailState.Loading
        repo.requestEmailOtp(email.value!!.trim())
            .onSuccess {
                _lastRequestedEmail.value = email.value!!.trim()
                startTimer()
                _state.value = EmailState.MailSent
            }
            // 💡 3. 이메일 요청 실패 시 ErrorSource.EMAIL로 에러 생성
            .onFailure {
                _state.value = EmailState.Error(it.message ?: "발송 실패", ErrorSource.EMAIL)
            }
        _loadingFor.value = null
    }

    fun verifyCode() = viewModelScope.launch {
        _loadingFor.value = LoadingFor.VERIFY_CODE
        _state.value = EmailState.Loading
        repo.verifyEmailOtp(email.value!!.trim(), code.value!!.trim())
            .onSuccess { _state.value = EmailState.Verified }
            // 💡 4. 코드 인증 실패 시 ErrorSource.CODE로 에러 생성
            .onFailure {
                _state.value = EmailState.Error(it.message ?: "코드를 다시 확인하세요.", ErrorSource.CODE)
            }
        _loadingFor.value = null
    }

    fun reset() {
        timerJob?.cancel()
        _millis.value = 0
        _state.value = EmailState.Idle
    }

    override fun onCleared() {
        timerJob?.cancel(); super.onCleared()
    }
}


/* ---------- LiveData n-개 → LiveData<R> 헬퍼 ---------- */
private fun <A, B, R> combineLatest(
    la: LiveData<A>, lb: LiveData<B>, block: (A, B) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    var a: A? = null;
    var b: B? = null

    fun update() {
        if (a != null && b != null) value = block(a!!, b!!)
    }

    addSource(la) { a = it; update() }
    addSource(lb) { b = it; update() }
}


class Step3ViewModelFactory(
    private val repo: AuthRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        state: SavedStateHandle
    ): T = Step3ViewModel(repo, state) as T
}
