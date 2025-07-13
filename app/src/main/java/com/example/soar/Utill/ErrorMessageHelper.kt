package com.example.soar.Utill


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout

class ErrorMessageHelper(
    private val context: Context,
    private val errorIconRes: Int,
    private val iconShiftY: Int = -6, // Y축 이동 px (기본값 -6)
    private val errorColorRes: Int
) {
    fun showError(
        til: TextInputLayout,
        valid: Boolean,
        touched: Boolean,
        msg: String
    ) {
        if (!touched || valid) {
            til.error = null
            return
        }

        val displayMsg = when {
            msg.contains("Failed") -> "서버와 연결하지 못하였습니다"
            // 필요 시 다른 매핑도 여기에 추가
            else -> msg
        }

        val icon = ContextCompat.getDrawable(context, errorIconRes)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        val span = CustomAlignImageSpan(icon!!, shift = iconShiftY)
        val errorText = "  $displayMsg"
        val ssb = SpannableStringBuilder(errorText)
        ssb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        til.error = ssb

        val colorStateList = ContextCompat.getColorStateList(context, errorColorRes)
        til.setErrorTextColor(colorStateList)
    }

    class CustomAlignImageSpan(drawable: Drawable, private val shift: Int = 0) : ImageSpan(drawable, ALIGN_BOTTOM) {
        override fun draw(
            canvas: Canvas, text: CharSequence,
            start: Int, end: Int, x: Float,
            top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val drawable = drawable
            canvas.save()
            val transY = bottom - drawable.bounds.bottom + shift
            canvas.translate(x, transY.toFloat())
            drawable.draw(canvas)
            canvas.restore()
        }
    }
}