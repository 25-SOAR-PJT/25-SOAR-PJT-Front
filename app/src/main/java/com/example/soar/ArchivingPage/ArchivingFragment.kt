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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.databinding.FragmentArchivingBinding
import androidx.fragment.app.viewModels
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.R
import com.example.soar.Network.tag.TagResponse
import com.example.soar.databinding.CustomToastBinding

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
        viewModel.fetchBookmarkedPolicies()
    }

    private fun setupRecyclerView() {
        // 1. 어댑터를 생성할 때, 클릭 시 실행될 동작(람다)을 전달합니다.
        adapter = ArchivingAdapter(
            onPolicyClick = { policy ->
                onPolicyItemClick(policy)
            },
            onApplyClick = { policy ->
                // ViewModel의 API 호출 함수를 실행
                viewModel.togglePolicyApplied(policy.policyId)
            }
        )
        binding.bizList.adapter = adapter
        binding.bizList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.btnToEdit.setOnClickListener {
            val intent = Intent(requireContext(), BookmarkEditActivity::class.java).apply {
                // ViewModel에 저장된 전체 정책 리스트를 전달
                // Parcelable로 만들었기 때문에 putParcelableArrayListExtra 사용 가능
                val allPolicies = viewModel.policies.value
                if (allPolicies != null) {
                    putParcelableArrayListExtra("ALL_POLICIES", ArrayList(allPolicies))
                }
            }
            startActivity(intent)
        }

    }

    private fun launchKeywordActivity() {
        val intent = Intent(requireContext(), KeywordActivity::class.java).apply {
            val currentSelectedIds = viewModel.selectedTags.value?.map { it.tagId } ?: emptyList()
            putIntegerArrayListExtra("selectedTagIds", ArrayList(currentSelectedIds))
        }
        keywordLauncher.launch(intent)
    }

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
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        // 새로 추가된 LiveData 옵저버들
        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                // Custom Toast를 사용하도록 수정
                showCustomToast(message)
            }
        }

    }

    private fun updateSelectedKeywordsUI(selectedTags: List<TagResponse>) {
        val tagsContainer = binding.keywordChipsContainer
        tagsContainer.removeAllViews()

        val margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics
        ).toInt()

        // 1. '키워드 선택' 버튼을 항상 먼저 추가합니다.
        val selectKeywordButton = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_keyword_add, tagsContainer, false) // 별도의 레이아웃 사용 권장

        // '키워드 선택' 버튼에 클릭 리스너 설정
        selectKeywordButton.setOnClickListener {
            launchKeywordActivity()
        }

        // 마진 설정
        val selectBtnParams =
            (selectKeywordButton.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(0, 0, margin, 0) // 오른쪽 마진만 적용
            }
        selectKeywordButton.layoutParams = selectBtnParams
        tagsContainer.addView(selectKeywordButton)

        // 2. 선택된 각 태그에 대해 키워드 칩을 추가합니다.
        selectedTags.forEach { tag ->
            val keywordView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_keyword, tagsContainer, false)

            val textView = keywordView.findViewById<TextView>(R.id.text_keyword)
            val btnClose = keywordView.findViewById<ImageView>(R.id.btn_close)

            textView.text = tag.tagName
            btnClose.visibility = View.GONE // 필요에 따라 GONE 또는 VISIBLE 처리

            keywordView.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.ref_blue_150)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ref_blue_600))

            val params = (keywordView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(0, 0, margin, 0) // 오른쪽 마진 적용
            }
            keywordView.layoutParams = params
            tagsContainer.addView(keywordView)
        }
    }

    // 2. DetailPageActivity로 이동하는 함수를 추가합니다.
    private fun onPolicyItemClick(policy: BookmarkedPolicy) {
        val intent = Intent(requireContext(), DetailPageActivity::class.java).apply {
            putExtra("policyId", policy.policyId)
        }
        startActivity(intent)
    }

    // Custom Toast를 보여주는 함수를 추가합니다.
    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val customToastBinding = CustomToastBinding.inflate(inflater)
        var toast: Toast? = null

        customToastBinding.textMessage.text = message
        customToastBinding.btnCancel.setOnClickListener {
            toast?.cancel()
        }

        toast = Toast(requireContext()).apply {
            duration = Toast.LENGTH_SHORT
            view = customToastBinding.root
        }
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}