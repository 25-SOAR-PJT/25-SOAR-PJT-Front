package com.example.soar.EntryPage.SignUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map      // ★ 이 한 줄만 있으면 OK

class Step1ViewModel : ViewModel() {

    private val _items = MutableLiveData<List<PolicyItem>>(listOf(
        PolicyItem(0, "[필수] 서비스 이용약관",        required = true),
        PolicyItem(1, "[필수] 개인정보 수집 및 이용동의", required = true),
        PolicyItem(2, "[선택] 마케팅 정보 수신동의",     required = false),
        PolicyItem(3, "[선택] 필수 알림 동의",         required = false)
    ))
    val items: LiveData<List<PolicyItem>> get() = _items

    private val _allChecked = MutableLiveData(false)
    val allChecked: LiveData<Boolean> get() = _allChecked

    /** 필수 항목이 모두 체크됐을 때만 true */
    val canProceed: LiveData<Boolean> = items.map { list ->
        list.filter { it.required }.all { it.checked }
    }

    fun toggleItem(idx: Int) {
        _items.value = _items.value!!.toMutableList().also { list ->
            list[idx] = list[idx].copy(checked = !list[idx].checked)
        }
        syncAllState()
    }

    fun toggleAll() {
        val target = !(_allChecked.value ?: false)
        _items.value = _items.value!!.map { it.copy(checked = target) }
        _allChecked.value = target
    }

    private fun syncAllState() {
        _allChecked.value = _items.value!!.all { it.checked }
    }
}