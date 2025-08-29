

package com.example.soar.ExplorePage

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.ArchivingPage.KeywordActivity
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.Network.tag.TagResponse
import com.example.soar.R
import com.example.soar.databinding.FragmentExploreBinding
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.MainActivity
import com.example.soar.Network.TokenManager

class ExploreFragment : Fragment(), ExploreAdapter.OnItemClickListener {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val exploreViewModel: ExploreViewModel by viewModels()
    private lateinit var bizAdapter: ExploreAdapter

    private var fieldId: Int? = null

    private lateinit var categories: List<LinearLayout>
    private var selectedCategoryId: Int? = null
    private val categoryNames = listOf("일자리", "주거", "교육", "복지문화")

    private val searchActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val changedBookmarks = result.data?.getSerializableExtra("changedBookmarks") as? HashMap<String, Boolean>
            if (changedBookmarks != null && changedBookmarks.isNotEmpty()) {
                exploreViewModel.updatePoliciesBookmarks(changedBookmarks)
            }
        }
    }

    private val personalBizLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val changedBookmarks = result.data?.getSerializableExtra("changedBookmarks") as? HashMap<String, Boolean>
            if (changedBookmarks != null && changedBookmarks.isNotEmpty()) {
                exploreViewModel.updatePoliciesBookmarks(changedBookmarks)
            }
        }
    }

    private val keywordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedTagIds = result.data?.getIntegerArrayListExtra("selectedTagIds") ?: arrayListOf()
            exploreViewModel.updateKeywordsByIds(selectedTagIds)

            val currentCategory = categoryNames.getOrNull(selectedCategoryId ?: -1)
            exploreViewModel.loadPolicies(category = currentCategory, isNewQuery = true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategories()
        observeViewModel()

        fieldId = arguments?.getInt("category_id")
        if (fieldId != null) {
            categoryPressed(fieldId!!)
        } else {
            exploreViewModel.loadPolicies(isNewQuery = true)
        }

        binding.btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            searchActivityLauncher.launch(intent)
        }

        val accessToken = TokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {

            binding.btnPersonalBiz.setOnClickListener {
                val intent = Intent(requireContext(), PersonalBizActivity::class.java)
                personalBizLauncher.launch(intent)
                requireActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
            }
        }
        else {
            binding.btnPersonalBiz.visibility = View.GONE
        }



        binding.openKeywordPage.setOnClickListener {
            val intent = Intent(requireContext(), KeywordActivity::class.java).apply {
                val currentSelectedIds = exploreViewModel.selectedKeywords.value?.map { it.tagId } ?: emptyList()
                putIntegerArrayListExtra("selectedTagIds", ArrayList(currentSelectedIds))
            }
            keywordLauncher.launch(intent)
        }

        // RecyclerView의 스크롤 리스너를 제거하고, NestedScrollView의 스크롤 리스너로 교체
        setupNestedScrollListener()
    }

    private fun setupRecyclerView() {
        bizAdapter = ExploreAdapter(this)
        binding.bizList.apply {
            adapter = bizAdapter
            layoutManager = LinearLayoutManager(context)
            // NestedScrollView에 의해 스크롤이 처리되므로 RecyclerView의 리스너는 필요 없음
        }
    }

    private fun setupNestedScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // NestedScrollView가 맨 아래로 스크롤되었는지 확인
            if (scrollY > oldScrollY && !v.canScrollVertically(1)) {
                // 더 이상 아래로 스크롤할 수 없을 때 (즉, 맨 아래에 도달했을 때)
                Log.d("Paging", "NestedScrollView 맨 아래 도달. 다음 페이지 호출 시도.")
                // ViewModel의 로딩 상태를 확인하고 다음 페이지 로드
                if (exploreViewModel.isLoading.value == false) {
                    exploreViewModel.loadPolicies()
                }
            }
        }
    }

    private fun observeViewModel() {
        exploreViewModel.policies.observe(viewLifecycleOwner) { policies ->
            bizAdapter.submitList(policies)
            if (exploreViewModel.isLoading.value == false && policies.isEmpty()) {
                binding.textSample.visibility = View.VISIBLE
                binding.textSample.text = "관련 정책이 없습니다."
            } else {
                binding.textSample.visibility = View.GONE
            }
        }

        exploreViewModel.selectedKeywords.observe(viewLifecycleOwner) { selectedKeywords ->
            updateSelectedKeywordsUI(selectedKeywords)
        }

        exploreViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingProgressBar.visibility = View.VISIBLE
                binding.bizList.visibility = View.GONE
            } else {
                binding.loadingProgressBar.visibility = View.GONE
                binding.bizList.visibility = View.VISIBLE
            }
        }
        exploreViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategories() {
        categories = listOf(binding.category1, binding.category2, binding.category3, binding.category4)
        categories.forEachIndexed { index, category ->
            category.setOnClickListener {
                categoryPressed(index)
            }
        }
    }

    private fun categoryPressed(categoryId: Int) {
        val newSelectedCategory: String?

        if (selectedCategoryId == categoryId) {
            selectedCategoryId = null
            newSelectedCategory = null
        } else {
            selectedCategoryId = categoryId
            newSelectedCategory = categoryNames[categoryId]
        }
        updateCategoryViews()
        exploreViewModel.loadPolicies(category = newSelectedCategory, isNewQuery = true)
    }

    private fun updateCategoryViews() {
        categories.forEachIndexed { index, categoryView ->
            val color = if (index == selectedCategoryId) R.color.ref_gray_100 else R.color.ref_white
            categoryView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        }
    }

    private fun updateSelectedKeywordsUI(selectedTags: List<TagResponse>) {
        val tagsContainer = binding.keywordChipsContainer
        tagsContainer.removeAllViews()

        if (selectedTags.isEmpty()) {
            binding.textKeywordPlaceholder.visibility = View.VISIBLE
            binding.keywordScrollView.visibility = View.GONE
        } else {
            binding.textKeywordPlaceholder.visibility = View.GONE
            binding.keywordScrollView.visibility = View.VISIBLE

            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
            ).toInt()

            selectedTags.forEach { tag ->
                val keywordView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_keyword, tagsContainer, false)

                val textView = keywordView.findViewById<TextView>(R.id.text_keyword)
                val btnClose = keywordView.findViewById<ImageView>(R.id.btn_close)

                textView.text = tag.tagName
                btnClose.visibility = View.GONE

                keywordView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.ref_blue_150)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_blue_600))

                val params = (keywordView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(0, 0, margin, margin)
                }
                keywordView.layoutParams = params
                tagsContainer.addView(keywordView)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPolicyItemClick(policy: YouthPolicy) {
        val intent = Intent(requireContext(), DetailPageActivity::class.java).apply {
            putExtra("policyId", policy.policyId)
        }
        startActivity(intent)
    }

    override fun onBookmarkClick(policy: YouthPolicy) {
        exploreViewModel.toggleBookmark(policy)
    }
}