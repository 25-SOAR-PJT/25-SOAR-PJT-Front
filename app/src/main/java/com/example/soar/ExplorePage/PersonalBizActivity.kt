package com.example.soar.ExplorePage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.CurationSequencePage.CurationSequenceActivity
import com.example.soar.Network.TokenManager
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.Utill.SwipeToDismissUtil
import com.example.soar.databinding.ActivityPersonalBizBinding
import com.google.android.flexbox.FlexboxLayout

class PersonalBizActivity : AppCompatActivity(), ExploreAdapter.OnItemClickListener {
    private lateinit var binding: ActivityPersonalBizBinding

    private val viewModel: PersonalBizViewModel by viewModels()
    private lateinit var bizAdapter: ExploreAdapter

    private val changedBookmarks = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalBizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 스와이프 종료 시 결과를 반환하는 람다를 SwipeToDismissUtil에 전달
        SwipeToDismissUtil(this) {
            dismissWithResult()
        }

        binding.modifyTags.setOnClickListener {
            val intent = Intent(this, CurationSequenceActivity::class.java)
            startActivity(intent)
        }



        setupUI()
        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.fetchPersonalPolicies()
    }

    private fun setupUI() {
        val userName = TokenManager.getUserInfo()?.userName ?: "사용자"
        binding.textUsername.text = userName

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 1.0).toInt()
        val height = (displayMetrics.heightPixels * 0.8).toInt()
        window.setLayout(width, height)
        window.setGravity(android.view.Gravity.BOTTOM)
    }

    private fun setupRecyclerView() {
        bizAdapter = ExploreAdapter(this)
        binding.bizList.apply {
            adapter = bizAdapter
            layoutManager = LinearLayoutManager(this@PersonalBizActivity)
            itemAnimator = null
        }
    }

    private fun setupListeners() {
        binding.modifyTags.setOnClickListener {
            val intent = Intent(this, CurationSequenceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.policies.observe(this) { policies ->
            binding.policyCount.text = getString(R.string.pb_suggestion_count, policies.size)
            bizAdapter.submitList(policies)
        }

        viewModel.userTags.observe(this) { tags ->
            updateSelectedTagsUI(tags)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.bizList.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg.isNotBlank()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSelectedTagsUI(selectedTags: List<TagResponse>) {
        val tagsContainer = binding.tagLauncher.findViewById<LinearLayout>(R.id.tag_launcher_container)
        tagsContainer.removeAllViews()

        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()

        if (selectedTags.isEmpty()) {
            val emptyTextView = LayoutInflater.from(this).inflate(R.layout.item_tag, tagsContainer, false) as TextView
            emptyTextView.text = "설정된 태그가 없습니다."
            (emptyTextView.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, margin, 0)
            tagsContainer.addView(emptyTextView)
        } else {
            selectedTags.forEach { tag ->
                val tagView = LayoutInflater.from(this).inflate(R.layout.item_tag, tagsContainer, false) as TextView
                tagView.text = tag.tagName
                (tagView.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, margin, 0)
                tagsContainer.addView(tagView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPersonalPolicies()
    }

    // 뒤로가기 버튼 클릭 시 호출
    override fun onBackPressed() {
        dismissWithResult()
    }

    // Activity를 종료하면서 결과를 반환하는 공통 함수
    private fun dismissWithResult() {
        val resultIntent = Intent()
        resultIntent.putExtra("changedBookmarks", HashMap(changedBookmarks))
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
        overridePendingTransition(0, R.anim.slide_out_down)
    }

    override fun onPolicyItemClick(policy: YouthPolicy) {
        val intent = Intent(this, com.example.soar.DetailPage.DetailPageActivity::class.java).apply {
            putExtra("policyId", policy.policyId)
        }
        startActivity(intent)
    }

    override fun onBookmarkClick(policy: YouthPolicy) {
        val newBookmarkState = !(policy.bookmarked ?: false)
        changedBookmarks[policy.policyId] = newBookmarkState
        viewModel.toggleBookmark(policy)
    }
}