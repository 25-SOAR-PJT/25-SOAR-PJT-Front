package com.example.soar.ExplorePage

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.ArchivingPage.KeywordActivity
import com.example.soar.ArchivingPage.TagViewModel
import com.example.soar.R
import com.example.soar.databinding.FragmentExploreBinding


class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private var fieldId: Int? = null

    private lateinit var categories: List<LinearLayout>
    private var selectedCategoryId: Int? = null

    private val tagViewModel: TagViewModel by activityViewModels()

    private val keywordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedTagIds = result.data?.getIntegerArrayListExtra("selectedTagIds") ?: arrayListOf()
            if (selectedTagIds.isEmpty()) {
                tagViewModel.resetFilteredData()
            } else {
                tagViewModel.filterDataByTagIds(selectedTagIds)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fieldId = arguments?.getInt("category_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 확인용 임시 바인딩
        binding.textSample.text = fieldId.toString()

        binding.btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            keywordLauncher.launch(intent)
        }

        binding.btnPersonalBiz.setOnClickListener {
            val intent = Intent(requireContext(), PersonalBizActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_up,
                R.anim.slide_out_down
            )
        }

        // TODO: 사업 연결하는 아답터 만들어야 함. recycler view 이름: item_explore_biz

        categories = listOf(
            binding.category1,
            binding.category2,
            binding.category3,
            binding.category4
        )

        // 홈화면에서 카테고리 아이디 넘어왔을때
        if (fieldId != null) {
            // 해당 카테고리를 눌린 상태로 만듭니다.
            categoryPressed(fieldId!!)
        }

        // 각 카테고리에 클릭 리스너 설정
        binding.category1.setOnClickListener {
            categoryPressed(0)
        }
        binding.category2.setOnClickListener {
            categoryPressed(1)
        }
        binding.category3.setOnClickListener {
            categoryPressed(2)
        }
        binding.category4.setOnClickListener {
            categoryPressed(3)
        }

        binding.openKeywordPage.setOnClickListener {
            val intent = Intent(requireContext(), KeywordActivity::class.java)
            keywordLauncher.launch(intent)
        }

    }

    fun categoryPressed(categoryId: Int) {
        // 이미 선택된 카테고리를 다시 누르면 선택 해제
        if (selectedCategoryId == categoryId) {
            // 선택 해제 로직
            selectedCategoryId = null
        } else {
            // 새로운 카테고리 선택
            selectedCategoryId = categoryId
        }

        // 모든 카테고리 뷰의 색상을 초기화하고, 선택된 뷰만 색상을 변경
        categories.forEachIndexed { index, categoryView ->
            if (index == selectedCategoryId) {
                // 선택된 카테고리의 배경색 변경 (활성화 상태)
                categoryView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.ref_gray_100))
            } else {
                // 선택되지 않은 카테고리의 배경색 변경 (비활성화 상태)
                categoryView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.ref_white))
            }
        }
    }

    companion object {
    }
}