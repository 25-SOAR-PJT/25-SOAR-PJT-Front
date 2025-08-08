package com.example.soar.EntryPage.SignUp

import androidx.lifecycle.*
import java.time.LocalDate

/** 검증 결과 상태 */
sealed interface VerifyState {
    object Idle : VerifyState
    object Success : VerifyState
    data class Error(val msg: String) : VerifyState
}

class Step2ViewModel(
    private val handle: SavedStateHandle           // ← 화면 회전 시 값 보존
) : ViewModel() {

    /* ── ① 입력값 ─────────────────────────────────────────────────── */
    val name = handle.getLiveData("name", "")
    val birth = handle.getLiveData("birth", "")   // YYMMDD
    val sexDigit = handle.getLiveData("sexDigit", "")   // 1자리

    /* ── ② touched flag ──────────────────────────────────────────── */
    val nameTouched = handle.getLiveData("nameTouched", false)
    val birthTouched = handle.getLiveData("birthTouched", false)
    val sexTouched = handle.getLiveData("sexTouched", false)

    /* ── ③ 개별 유효성 ───────────────────────────────────────────── */
    val isNameValid: LiveData<Boolean> =
        name.map { it.matches(NAME_REGEX) }

    /** 생년월일(YYMMDD) + 실존 날짜 체크 */
    val isBirthValid: LiveData<Boolean> = birth.map { txt ->
        if (txt.length != 6 || !txt.all(Char::isDigit)) return@map false
        try {
            val yy = txt.substring(0, 2).toInt()
            val mm = txt.substring(2, 4).toInt()
            val dd = txt.substring(4, 6).toInt()
            val century = if (yy > LocalDate.now().year % 100) 1900 else 2000
            LocalDate.of(century + yy, mm, dd)   // Exception → invalid
            true
        } catch (_: Exception) {
            false
        }
    }

    /** 성별 코드(1,2,3,4,9) & 길이 1 */
    val isSexValid: LiveData<Boolean> =
        sexDigit.map { it.length == 1 && it[0] in "12349" }

    /* ── ④ 모두 통과 여부 → 버튼 활성화 ──────────────────────────── */
    val canProceed: LiveData<Boolean> =
        listOf(isNameValid, isBirthValid, isSexValid)
            .combineLatest { list -> list.all { it } }

    /* ── ⑤ 실명 인증 상태 ─────────────────────────────────────────── */
    private val _verifyState = MutableLiveData<VerifyState>(VerifyState.Idle)
    val verifyState: LiveData<VerifyState> = _verifyState

    /** 입력값이 모두 유효하면 즉시 성공 처리 */
    fun verifyIdentity() {
        _verifyState.value =
            if (canProceed.value == true) VerifyState.Success
            else VerifyState.Error("입력 값을 확인해주세요.")
    }

    fun resetVerifyState() {
        _verifyState.value = VerifyState.Idle
    }

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z\\s]{1,20}$")
    }
}

/* ---------- LiveData 확장 : 여러 값을 combine ---------- */
fun <T> List<LiveData<T>>.combineLatest(block: (List<T>) -> T): LiveData<T> =
    MediatorLiveData<T>().also { m ->
        val data = MutableList(size) { null as T? }
        forEachIndexed { i, src ->
            m.addSource(src) { v ->
                data[i] = v; if (data.all { it != null }) m.value = block(data.filterNotNull())
            }
        }
    }