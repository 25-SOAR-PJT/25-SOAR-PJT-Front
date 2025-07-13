package com.example.soar.Utill

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FocusErrorController(
    private val editText: TextInputEditText,
    private val til: TextInputLayout,
    private val isValid: () -> Boolean,      // validation 로직
    private val getErrorMessage: () -> String?, // 에러메시지
    private val showError: (String) -> Unit, // 실제 에러 표시 방법
) {
    init {

        // 포커스 리스너
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // 포커스 아웃 시
                val text = editText.text?.toString() ?: ""
                if (text.isNotBlank() && !isValid()) {
                    getErrorMessage()?.let { showError(it) }
                } else {
                    til.error = null
                }
            } else {
                til.error = null // 포커스 중엔 무조건 에러 안 보임
            }
        }
        // 입력값 변경 시
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (editText.isFocused) {
                    til.error = null
                } else {
                    val text = editText.text?.toString() ?: ""
                    if (text.isNotBlank() && !isValid()) {
                        getErrorMessage()?.let { showError(it) }
                    } else {
                        til.error = null
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}