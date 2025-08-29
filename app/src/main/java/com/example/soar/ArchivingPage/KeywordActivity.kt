// com.example.soar.ArchivingPage/KeywordActivity.kt

package com.example.soar.ArchivingPage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.soar.CurationSequencePage.TagUiModel
import com.example.soar.R
import com.example.soar.databinding.ActivityKeywordBinding
import com.google.android.flexbox.FlexboxLayout

class KeywordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeywordBinding
    private val viewModel: KeywordViewModel by viewModels()

    // fieldId와 FlexboxLayout을 매핑
    private lateinit var flexboxMap: Map<Int, FlexboxLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeywordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFlexboxMap()
        setupUI()
        setupListeners()
        setupObservers()

        // ViewModel을 통해 태그 데이터 로드
        viewModel.loadTags()

        // 이전 화면에서 전달받은 선택된 태그 ID로 ViewModel 초기화
        val initialSelectedTagIds = intent.getIntegerArrayListExtra("selectedTagIds")?.toSet() ?: emptySet()
        viewModel.initializeSelection(initialSelectedTagIds)
    }

    private fun initializeFlexboxMap() {
        flexboxMap = mapOf(
            1 to binding.flexLayout1, 2 to binding.flexLayout2,
            3 to binding.flexLayout3, 4 to binding.flexLayout4
        )
    }

    private fun setupUI() {
        binding.appbar.textTitle.text = getString(R.string.keyword)
    }

    private fun setupListeners() {
        binding.appbar.btnBack.setOnClickListener {
            // 현재 선택된 태그를 결과로 전달하고 종료
            returnResult()
        }

        binding.btnTagSubmit.setOnClickListener {
            returnResult()
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetSelection()
        }
    }

    private fun setupObservers() {
        // tagsUiModel이 변경될 때마다 전체 Flexbox UI를 다시 그림
        viewModel.tagsUiModel.observe(this) { allTags ->
            updateAllFlexboxUI(allTags)
        }

        // selectedTagIds가 변경될 때마다 제출 버튼 상태 업데이트
        viewModel.selectedTagIds.observe(this) { selectedIds ->
            binding.btnTagSubmit.isEnabled = selectedIds.isNotEmpty()
        }

        // Toast 메시지 이벤트 관찰
        viewModel.showToast.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAllFlexboxUI(tags: List<TagUiModel>) {
        val groupedTags = tags.groupBy { it.fieldId }

        flexboxMap.forEach { (fieldId, flexboxLayout) ->
            val tagsForField = groupedTags[fieldId] ?: emptyList()
            updateFlexboxForCategory(flexboxLayout, tagsForField)
        }
    }

    private fun updateFlexboxForCategory(flexboxLayout: FlexboxLayout, tags: List<TagUiModel>) {
        flexboxLayout.removeAllViews()

        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
        ).toInt()

        tags.forEach { tag ->
            val keywordView = LayoutInflater.from(this)
                .inflate(R.layout.item_keyword, flexboxLayout, false)
            val textView = keywordView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName

            if (tag.isSelected) {
                setSelectedUI(keywordView)
            } else {
                setDefaultUI(keywordView)
            }

            keywordView.setOnClickListener {
                viewModel.toggleTagSelection(tag.tagId)
            }

            val params = (keywordView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(0, 0, margin, margin)
            }
            keywordView.layoutParams = params
            flexboxLayout.addView(keywordView)
        }
    }

    private fun setDefaultUI(tagView: View) {
        tagView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ref_coolgray_100)
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        val btnClose = tagView.findViewById<ImageView>(R.id.btn_close)
        textView.setTextColor(ContextCompat.getColor(this, R.color.ref_gray_800))
        btnClose.visibility = View.GONE
    }

    private fun setSelectedUI(tagView: View) {
        tagView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.ref_blue_150)
        val textView = tagView.findViewById<TextView>(R.id.text_keyword)
        val btnClose = tagView.findViewById<ImageView>(R.id.btn_close)
        textView.setTextColor(ContextCompat.getColor(this, R.color.ref_blue_600))
        btnClose.visibility = View.VISIBLE
    }

    private fun returnResult() {
        val selectedIds = viewModel.selectedTagIds.value ?: emptySet()
        val resultIntent = Intent().apply {
            putIntegerArrayListExtra("selectedTagIds", ArrayList(selectedIds))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}