/* --------- FocusErrorControllerGroup.kt -------------- */
package com.example.soar.Utill

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * 여러 TextInputLayout + 1개의 하이픈(ImageView) 를 한 덩어리로 관리
 */
/**
 * 여러 TextInputLayout + 하이픈(ImageView)을 한 덩어리로 관리.
 *
 * ▸ error 표시는 외부에서 넘겨준 showError() 로 처리한다.
 */
class FocusErrorControllerGroup(
    private val edits: List<TextInputEditText>,
    private val tils : List<TextInputLayout>,
    private val hyphen: AppCompatImageView,
    private val defaultColor: Int,
    private val focusedColor: Int,
    private val errorColor  : Int,
    private val isValid: () -> Boolean,
    private val getErrorMessage: () -> String,
    private val showError: (String) -> Unit,        // ★ 변경! (til.error 직접 X)
    private val clearError: () -> Unit              // ★ 추가! 에러 해제 방법
) {
    init {
        edits.forEach { et ->
            et.setOnFocusChangeListener { _, _ -> syncColors() }
            et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { syncColors() }
                override fun beforeTextChanged(s: CharSequence?, s1: Int, s2: Int, s3: Int) {}
                override fun onTextChanged(s: CharSequence?, s1: Int, s2: Int, s3: Int) {}
            })
        }
        syncColors()
    }

    private fun syncColors() {
        val anyFocused = edits.any { it.hasFocus() }
        val allEmpty   = edits.all { it.text.isNullOrEmpty() }
        val validNow   = isValid()

        val strokeColor = when {
            anyFocused -> focusedColor
            allEmpty   -> defaultColor
            !validNow  -> errorColor
            else       -> defaultColor
        }
        val tint = ColorStateList.valueOf(strokeColor)

        // 테두리·하이픈 색 동기화
        tils.forEach { til ->
            val states  = arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf())
            val colors  = intArrayOf(strokeColor, strokeColor)
            til.setBoxStrokeColorStateList(ColorStateList(states, colors))
        }
        hyphen.imageTintList = tint

        // 에러 메시지
        when {
            !validNow && !allEmpty && !anyFocused -> showError(getErrorMessage())
            else                                  -> clearError()
        }
    }
}