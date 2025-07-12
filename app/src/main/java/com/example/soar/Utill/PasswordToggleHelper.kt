package com.example.soar.Utill

import android.content.Context
import android.text.InputType
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout

class PasswordToggleHelper(
    private val til: TextInputLayout,
    private val context: Context,
    private val activeIconRes: Int,
    private val inactiveIconRes: Int
) {

    init {
        // 최초 상태: 비밀번호 숨김(눈닫힘)
        til.endIconDrawable = ContextCompat.getDrawable(context, inactiveIconRes)
        til.editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        til.setEndIconOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        val editText = til.editText ?: return
        val isVisible = editText.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        if (isVisible) {
            // "비밀번호 숨김"으로 전환
            til.endIconDrawable = ContextCompat.getDrawable(context, inactiveIconRes)
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            // "비밀번호 표시"로 전환
            til.endIconDrawable = ContextCompat.getDrawable(context, activeIconRes)
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        // 커서 위치 유지
        editText.setSelection(editText.text?.length ?: 0)
    }
}