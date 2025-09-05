
package com.example.soar.ArchivingPage



import android.app.Activity

import android.content.Intent

import android.os.Bundle

import android.util.TypedValue

import androidx.fragment.app.Fragment

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import android.widget.ImageView

import android.widget.TextView

import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.soar.databinding.FragmentArchivingBinding

import androidx.fragment.app.viewModels

import com.example.soar.DetailPage.DetailPageActivity

import com.example.soar.EntryPage.SignIn.LoginActivity

import com.example.soar.MainActivity

import com.example.soar.Network.TokenManager

import com.example.soar.Network.archiving.BookmarkedPolicy

import com.example.soar.R

import com.example.soar.Network.tag.TagResponse


import com.example.soar.util.showBlockingToast

import com.example.soar.util.TouchBlockingToast


class ArchivingFragment : Fragment() {

    private var _binding: FragmentArchivingBinding? = null

    private val binding get() = _binding!!


    private val viewModel: ArchivingViewModel by viewModels()

    private lateinit var adapter: ArchivingAdapter


    private val keywordLauncher = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()

    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val selectedTagIds =

                result.data?.getIntegerArrayListExtra("selectedTagIds") ?: arrayListOf()

            viewModel.filterPoliciesByTag(selectedTagIds)

        }

    }


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View {

        _binding = FragmentArchivingBinding.inflate(inflater, container, false)

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        setupListeners()

        setupObservers()



        updateSelectedKeywordsUI(emptyList())

    }


    override fun onResume() {

        super.onResume()

// ✨ 수정: 화면에 다시 나타날 때마다 로그인 상태를 확인하여 UI를 갱신

        updateUiForLoginState()

    }


// ✨ 추가: 로그인 상태에 따라 UI를 갱신하는 함수

    private fun updateUiForLoginState() {

        val accessToken = TokenManager.getAccessToken()



        if (accessToken.isNullOrEmpty()) {

// --- 로그아웃 상태 ---

// 1. "편집" 버튼 숨기기

            binding.btnToEdit.visibility = View.GONE

// 2. 상단 문구를 "0건"으로 고정

            binding.appliedPerBookmark.text = "0개"

// 3. 키워드 필터, 구분선, 북마크 리스트(biz_list) 숨기기

            binding.keywordScrollView.visibility = View.GONE

            binding.keywordChipsContainer.visibility = View.GONE

            binding.bizList.visibility = View.GONE

// 4. 로그인 유도 버튼 보이기

            binding.btnLoginEntry.visibility = View.VISIBLE

            binding.btnZeroEntry.visibility = View.GONE // ✨ 북마크 0개 화면 숨기기

        } else {

// --- 로그인 상태 ---

// 1. "편집" 버튼 보이기

            binding.btnToEdit.visibility = View.VISIBLE


// 3. 로그인 유도 버튼 숨기기

            binding.btnLoginEntry.visibility = View.GONE

// 4. ViewModel을 통해 실제 북마크 데이터 가져오기

            viewModel.fetchBookmarkedPolicies()

        }

    }


    private fun setupRecyclerView() {

        adapter = ArchivingAdapter(

            onPolicyClick = { policy ->

                onPolicyItemClick(policy)

            },

            onApplyClick = { policy ->

                viewModel.togglePolicyApplied(policy.policyId)

            }

        )

        binding.bizList.adapter = adapter

        binding.bizList.layoutManager = LinearLayoutManager(requireContext())

    }


    private fun setupListeners() {

        binding.btnToEdit.setOnClickListener {

            val intent = Intent(requireContext(), BookmarkEditActivity::class.java).apply {

                val allPolicies = viewModel.policies.value

                if (allPolicies != null) {

                    putParcelableArrayListExtra("ALL_POLICIES", ArrayList(allPolicies))

                }

            }

            startActivity(intent)

        }


// ✨ 추가: "로그인" 버튼 클릭 리스너 설정

        binding.btnToLogin.setOnClickListener {

            val intent = Intent(requireContext(), LoginActivity::class.java)

            startActivity(intent)

        }


// ✨ 추가: "둘러보기" 버튼 클릭 리스너 설정

        binding.btnToExplore.setOnClickListener {

// 현재 프래그먼트를 호스팅하고 있는 액티비티(MainActivity)의 인스턴스를 가져옴

// 'as?'를 사용하여 안전하게 형변환

            val mainActivity = activity as? MainActivity

// mainActivity가 null이 아닐 경우에만 goToExploreTab() 함수 호출

            mainActivity?.goToExploreTab()

        }

    }


    private fun launchKeywordActivity() {

        val intent = Intent(requireContext(), KeywordActivity::class.java).apply {

            val currentSelectedIds = viewModel.selectedTags.value?.map { it.tagId } ?: emptyList()

            putIntegerArrayListExtra("selectedTagIds", ArrayList(currentSelectedIds))

        }

        keywordLauncher.launch(intent)

    }


    // ArchivingFragment.kt

    private fun setupObservers() {
        viewModel.filteredPolicies.observe(viewLifecycleOwner) { policies ->
            adapter.submitList(policies)
        }

        viewModel.selectedTags.observe(viewLifecycleOwner) { selectedTags ->
            updateSelectedKeywordsUI(selectedTags)
        }

        viewModel.policies.observe(viewLifecycleOwner) { allPolicies ->
            val totalCount = allPolicies.size
            val appliedCount = allPolicies.count { it.applied }
            binding.appliedPerBookmark.text =
                getString(R.string.applied_count_format, appliedCount, totalCount)

            if (allPolicies.isEmpty()) {
                binding.keywordScrollView.visibility = View.GONE
                binding.keywordChipsContainer.visibility = View.GONE
                binding.bizList.visibility = View.GONE
                binding.btnZeroEntry.visibility = View.VISIBLE
                binding.appliedPerBookmark.text = "0개"
            } else {
                binding.keywordScrollView.visibility = View.VISIBLE
                binding.keywordChipsContainer.visibility = View.VISIBLE
                binding.bizList.visibility = View.VISIBLE
                binding.btnZeroEntry.visibility = View.GONE
                binding.appliedPerBookmark.text =
                    getString(R.string.applied_count_format, appliedCount, totalCount)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { /* ... */ }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            requireActivity().showBlockingToast(error, hideCancel = true)
        }

        // ✨ 변경: payload 기반 + onCancel 연결
        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { payload ->
                val policyId = payload.policyIdForUndo
                showBlockingToast(
                    message = payload.message,
                    hideCancel = policyId == null,   // 신청완료가 아닐 땐 취소 버튼 숨김
                    cancelText = "취소",
                    onCancel = {
                        policyId?.let { viewModel.undoApply(it) } // ✅ 되돌리기
                    }
                )
            }
        }
    }



    private fun updateSelectedKeywordsUI(selectedTags: List<TagResponse>) {

        val tagsContainer = binding.keywordChipsContainer

        tagsContainer.removeAllViews()


        val margin = TypedValue.applyDimension(

            TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics

        ).toInt()


        val selectKeywordButton = LayoutInflater.from(requireContext())

            .inflate(R.layout.item_keyword_add, tagsContainer, false)



        selectKeywordButton.setOnClickListener {

            launchKeywordActivity()

        }


        val selectBtnParams =

            (selectKeywordButton.layoutParams as ViewGroup.MarginLayoutParams).apply {

                setMargins(0, 0, margin, 0)

            }

        selectKeywordButton.layoutParams = selectBtnParams

        tagsContainer.addView(selectKeywordButton)



        selectedTags.forEach { tag ->

            val keywordView = LayoutInflater.from(requireContext())

                .inflate(R.layout.item_keyword, tagsContainer, false)


            val textView = keywordView.findViewById<TextView>(R.id.text_keyword)

            val btnClose = keywordView.findViewById<ImageView>(R.id.btn_close)



            textView.text = tag.tagName

            btnClose.visibility = View.GONE



            keywordView.backgroundTintList =

                ContextCompat.getColorStateList(requireContext(), R.color.ref_blue_150)

            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_blue_600))


            val params = (keywordView.layoutParams as ViewGroup.MarginLayoutParams).apply {

                setMargins(0, 0, margin, 0)

            }

            keywordView.layoutParams = params

            tagsContainer.addView(keywordView)

        }

    }


    private fun onPolicyItemClick(policy: BookmarkedPolicy) {

        val intent = Intent(requireContext(), DetailPageActivity::class.java).apply {

            putExtra("policyId", policy.policyId)

        }

        startActivity(intent)

    }


    override fun onStop() {

        super.onStop()

        TouchBlockingToast.clear(requireActivity())

    }


    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}