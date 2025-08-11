package com.example.soar.ArchivingPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.soar.ApiResponse
import com.example.soar.R
import com.example.soar.TagResponse
import com.example.soar.databinding.ActivityKeywordBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class KeywordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeywordBinding
    private var selectedTagIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeywordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialSelectedTagIds = intent.getIntegerArrayListExtra("selectedTagIds")
        if (initialSelectedTagIds != null) {
            selectedTagIds = initialSelectedTagIds.toMutableSet()
            updateSubmitButtonState()
        }

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
            updateFlexbox(fieldId, sampleText, tags, flexboxLayout)
        }

        // btn_tag_submit 클릭 리스너
        binding.btnTagSubmit.setOnClickListener {
            Log.d("KeywordActivity", "제출 클릭, 선택된 태그 = $selectedTagIds")

            val resultIntent = Intent().apply {
                putIntegerArrayListExtra("selectedTagIds", ArrayList(selectedTagIds))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.btnReset.setOnClickListener {
            resetAllTags()
        }
    }

    private fun groupTagsByField(items: List<TagResponse>): Map<Int, List<TagResponse>> {
        return items.groupBy { it.fieldId }
    }

    private fun updateFlexbox(
        fieldId: Int,
        sampleText: String,
        tags: List<TagResponse>,
        flexboxLayout: FlexboxLayout
    ) {
        flexboxLayout.removeAllViews()

        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics
        ).toInt()

        // 1. 샘플 태그를 다른 태그들과 동일하게 처리하기 위해 TagResponse 객체로 생성합니다.
        // 샘플 태그임을 구분하기 위해 tagId에 특별한 값(-1)을 부여합니다.
        val sampleTagResponse = TagResponse(
            tagId = -1,
            tagName = sampleText,
            fieldId = fieldId
        )

        val tagsToShow = if (tags.isNotEmpty()) {
            tags.subList(0, tags.size - 1)
        } else {
            emptyList()
        }

        // 2. 샘플 태그와 일반 태그 목록을 하나의 리스트로 합칩니다.
        val allTags = listOf(sampleTagResponse) + tagsToShow

        // 3. 통합된 리스트를 순회하며 모든 태그를 생성하고 설정합니다.
        allTags.forEach { tag ->
            val tagLayout = LayoutInflater.from(this)
                .inflate(R.layout.item_keyword, flexboxLayout, false)

            val tagText = tagLayout.findViewById<TextView>(R.id.text_keyword)
            val btnClose = tagLayout.findViewById<ImageView>(R.id.btn_close)

            val params = tagLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, margin, margin)
            tagLayout.layoutParams = params

            tagText.text = tag.tagName

            // 4. 태그가 선택되었는지 여부에 따라 UI를 업데이트합니다.
            tagLayout.isSelected = selectedTagIds.contains(tag.tagId)
            btnClose.visibility = if (tagLayout.isSelected) View.VISIBLE else View.GONE

            // 초기 UI 상태를 설정하는 헬퍼 함수를 호출하여 코드를 깔끔하게 만듭니다.
            if (tagLayout.isSelected) {
                setSelectedUI(tagLayout)
            } else {
                setDefaultUI(tagLayout)
            }

            // 5. 클릭 이벤트를 처리합니다.
            tagLayout.setOnClickListener {
                // 샘플 태그(-1)가 클릭된 경우
                if (tag.tagId == -1) {
                    selectAllTagIds(fieldId)
                    if (selectedTagIds.isEmpty()) {
                        setDefaultUI(tagLayout)
                        btnClose.visibility = View.GONE
                    } else {
                        setSelectedUI(tagLayout)
                        btnClose.visibility = View.VISIBLE
                    }
                } else {
                    if (selectedTagIds.contains(tag.tagId)) {
                        selectedTagIds.remove(tag.tagId)
                        setDefaultUI(tagLayout)
                        btnClose.visibility = View.GONE
                    } else {
                        selectedTagIds.add(tag.tagId)
                        setSelectedUI(tagLayout)
                        btnClose.visibility = View.VISIBLE
                    }
                }

                updateSubmitButtonState()
            }

            flexboxLayout.addView(tagLayout)
        }
    }

    private fun selectAllTagIds(fieldId: Int) {
        val tagsInField = loadDummyTagData().filter { it.fieldId == fieldId }
        val isAllSelected = tagsInField.all { selectedTagIds.contains(it.tagId) }

        if (isAllSelected) {
            tagsInField.forEach { tag -> selectedTagIds.remove(tag.tagId) }
        } else {
            tagsInField.forEach { tag -> selectedTagIds.add(tag.tagId) }
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

    private fun resetAllTags() {
        // 모든 선택된 태그 ID를 초기화
        selectedTagIds.clear()

        val flexboxMap = mapOf(
            1 to binding.flexLayout1, 2 to binding.flexLayout2,
            3 to binding.flexLayout3, 4 to binding.flexLayout4
        )

        flexboxMap.values.forEach { flexboxLayout ->
            for (i in 0 until flexboxLayout.childCount) {
                val tagView = flexboxLayout.getChildAt(i)
                tagView.isSelected = false

                setDefaultUI(tagView)
                tagView.findViewById<ImageView>(R.id.btn_close)?.visibility = View.GONE
            }
        }

        updateSubmitButtonState()
    }

    private fun updateSubmitButtonState() {
        binding.btnTagSubmit.isEnabled = selectedTagIds.isNotEmpty()
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
}
