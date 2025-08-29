package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View // View import 추가
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.soar.MyPage.TagSelection.TagSelectionActivity
import com.example.soar.Network.TokenManager
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.ActivityProfileBinding
import com.google.android.flexbox.FlexboxLayout


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppbar()
        setupUserInfo()
        setupObservers()
        profileViewModel.fetchUserTags()
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.fetchUserTags()
    }

    private fun setupAppbar() {
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.profile)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupUserInfo() {
        val userName = TokenManager.getUserInfo()?.userName ?: "사용자"
        binding.name.text = userName

        // 카카오 로그인 사용자인지 확인하여 비밀번호 변경 버튼의 가시성을 설정합니다.
        if (TokenManager.isKakaoUser()) {
            binding.btnChangePw.visibility = View.GONE
        } else {
            // 일반 사용자의 경우 버튼을 표시하고 클릭 리스너 설정
            binding.btnChangePw.visibility = View.VISIBLE
            binding.btnChangePw.setOnClickListener {
                val intent = Intent(this, ChangePwActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun setupObservers() {
        profileViewModel.userTags.observe(this, Observer { userTags ->
            if (userTags != null) {
                updateCurationChips(userTags)
            }
        })
        profileViewModel.fetchError.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, "프로필 데이터를 불러오지 못했습니다: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCurationChips(tags: List<TagResponse>) {
        binding.flexboxLocation.removeAllViews()
        binding.flexboxJob.removeAllViews()
        binding.flexboxEducation.removeAllViews()
        binding.flexboxExtra.removeAllViews()
        binding.flexboxKeyword.removeAllViews()

        val groupedTags = tags.groupBy { it.fieldId }

        val locationTags = groupedTags[9] ?: emptyList()
        val jobTags = groupedTags[5] ?: emptyList()
        val educationTags = groupedTags[8] ?: emptyList()
        val extraTags = groupedTags[7] ?: emptyList()
        val keywordTags = listOfNotNull(
            groupedTags[1], groupedTags[2], groupedTags[3], groupedTags[4]
        ).flatten()

        addChipsToFlexbox(binding.flexboxLocation, locationTags, TagSelectionActivity.STEP1_FIELD_ID)
        addChipsToFlexbox(binding.flexboxJob, jobTags, TagSelectionActivity.STEP2_FIELD_ID)
        addChipsToFlexbox(binding.flexboxEducation, educationTags, TagSelectionActivity.STEP3_FIELD_ID)
        addChipsToFlexbox(binding.flexboxExtra, extraTags, TagSelectionActivity.STEP4_FIELD_ID)
        addChipsToFlexbox(binding.flexboxKeyword, keywordTags, TagSelectionActivity.STEP5_FIELD_ID)
    }

    private fun addChipsToFlexbox(flexbox: FlexboxLayout, tags: List<TagResponse>, fieldId: Int) {
        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()

        val displayTags = if (tags.isNullOrEmpty()) {
            listOf(TagResponse(0, "해당사항 없음", fieldId))
        } else {
            tags
        }

        displayTags.forEach { tag ->
            val chipView = LayoutInflater.from(this).inflate(R.layout.item_summary_chip, flexbox, false)
            val textView = chipView.findViewById<TextView>(R.id.text_keyword)
            textView.text = tag.tagName

            chipView.setOnClickListener {
                val intent = Intent(this, TagSelectionActivity::class.java).apply {
                    putExtra(TagSelectionActivity.EXTRA_EDIT_MODE, true)
                    putExtra(TagSelectionActivity.EXTRA_STARTING_FIELD_ID, fieldId)
                    val userTags = profileViewModel.userTags.value?.toTypedArray()
                    putExtra(TagSelectionActivity.EXTRA_USER_TAGS, userTags)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            (chipView.layoutParams as? FlexboxLayout.LayoutParams)?.setMargins(0, 0, margin, margin)
            flexbox.addView(chipView)
        }
    }
}