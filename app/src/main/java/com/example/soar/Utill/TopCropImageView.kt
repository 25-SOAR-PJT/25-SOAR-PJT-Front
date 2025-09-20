package com.example.soar.Utill

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class TopCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        // scaleType을 MATRIX로 설정해야 imageMatrix가 적용됩니다.
        scaleType = ScaleType.MATRIX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        configureMatrix()
    }

    private fun configureMatrix() {
        // 이미지가 없으면 아무것도 하지 않습니다.
        val drawable = drawable ?: return

        val viewWidth = width.toFloat() - paddingLeft - paddingRight
        val viewHeight = height.toFloat() - paddingTop - paddingBottom
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        // 뷰의 너비에 맞게 이미지의 스케일(비율)을 계산합니다.
        val scale = if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
            viewHeight / drawableHeight
        } else {
            viewWidth / drawableWidth
        }

        // 새 Matrix를 만들고 계산된 스케일을 적용합니다.
        val matrix = Matrix()
        matrix.setScale(scale, scale)

        // ImageView에 최종 Matrix를 적용합니다.
        imageMatrix = matrix
    }
}