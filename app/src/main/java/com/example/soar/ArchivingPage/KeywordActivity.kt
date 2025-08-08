package com.example.soar.ArchivingPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.soar.R
import com.example.soar.databinding.ActivityKeywordBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class KeywordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeywordBinding
    private val selectedTagIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeywordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.keyword)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            Log.d("KeywordActivity", "뒤로가기 클릭, 선택된 태그 = $selectedTagIds")

            val resultIntent = Intent().apply {
                putIntegerArrayListExtra("selectedTagIds", ArrayList(selectedTagIds))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // fieldId별 FlexboxLayout 매핑
        val flexboxMap = mapOf(
            1 to binding.flexLayout1, 2 to binding.flexLayout2,
            3 to binding.flexLayout3, 4 to binding.flexLayout4
        )

        val sampleTextMap = mapOf(
            1 to getString(R.string.job_all),
            2 to getString(R.string.dwelling_all),
            3 to getString(R.string.education_all),
            4 to getString(R.string.welfare_all)
        )

        // 더미 태그 데이터 로드
        val tagList = loadDummyTagData()  // JSON에서 로드한다고 가정
        val groupedTags = groupTagsByField(tagList)

        // fieldId별 Flexbox에 태그 표시
        groupedTags.forEach { (fieldId, tags) ->
            val flexboxLayout = flexboxMap[fieldId] ?: return@forEach
            val sampleText = sampleTextMap[fieldId] ?: ""
            updateFlexbox(sampleText, tags, flexboxLayout)
        }
    }

    private fun groupTagsByField(items: List<TagResponse>): Map<Int, List<TagResponse>> {
        return items.groupBy { it.fieldId }
    }

    private fun updateFlexbox(
        sampleText: String,
        tags: List<TagResponse>,
        flexboxLayout: FlexboxLayout
    ) {
        flexboxLayout.removeAllViews()

        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 36f, resources.displayMetrics
        ).toInt()
        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics
        ).toInt()
        val horizontalPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
        ).toInt()

        // 샘플 태그
        val sampleTag = TextView(this).apply {
            text = sampleText
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, heightPx
            ).apply { setMargins(0, 0, margin, margin) }
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
            setBackgroundResource(R.drawable.round_background2)
            backgroundTintList = ContextCompat.getColorStateList(this@KeywordActivity, R.color.ref_blue_150)
            setTextColor(ContextCompat.getColor(this@KeywordActivity, R.color.ref_blue_600))
            setTextAppearance(R.style.Font_Label_Semibold)
            gravity = Gravity.CENTER_VERTICAL
        }
        flexboxLayout.addView(sampleTag)

        // 태그 생성
        tags.forEach { tag ->
            val tagLayout = LayoutInflater.from(this)
                .inflate(R.layout.item_keyword, flexboxLayout, false)

            val tagText = tagLayout.findViewById<TextView>(R.id.text_keyword)
            val btnClose = tagLayout.findViewById<ImageView>(R.id.btn_close)

            val params = tagLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, margin, margin)
            tagLayout.layoutParams = params

            tagText.text = tag.tagName

            tagLayout.setOnClickListener {
                if (selectedTagIds.contains(tag.tagId)) {
                    selectedTagIds.remove(tag.tagId)
                    setDefaultUI(tagLayout)
                    btnClose.visibility = View.GONE
                } else {
                    selectedTagIds.add(tag.tagId)
                    setSelectedUI(tagLayout)
                    btnClose.visibility = View.VISIBLE
                }

                setResult(
                    RESULT_OK,
                    intent.putIntegerArrayListExtra("selectedTagIds", ArrayList(selectedTagIds))
                )
            }

            flexboxLayout.addView(tagLayout)
        }

    }

    private fun setDefaultUI(tagView: View) {
        tagView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ref_coolgray_100)
        tagView.findViewById<TextView>(R.id.text_keyword)
            .setTextColor(ContextCompat.getColor(this, R.color.ref_gray_800))
    }

    private fun setSelectedUI(tagView: View) {
        tagView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ref_blue_150)
        tagView.findViewById<TextView>(R.id.text_keyword)
            .setTextColor(ContextCompat.getColor(this, R.color.ref_blue_600))
    }


    // 추후 삭제
    private fun loadDummyTagData(): List<TagResponse> {
        val json = assets.open("response_tags.json")
            .bufferedReader().use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<ApiResponse>() {}.type
        val response: ApiResponse = gson.fromJson(json, type)
        return response.data
    }
    // JSON 최상위 구조를 위한 data class
    data class ApiResponse(
        val status: String,
        val data: List<TagResponse>
    )
}
