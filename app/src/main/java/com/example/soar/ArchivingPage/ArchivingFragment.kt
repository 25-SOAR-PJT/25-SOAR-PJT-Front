package com.example.soar.ArchivingPage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.ApiResponse
import com.example.soar.Business
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.CurationSequencePage.CurationSequeceActivity
import com.example.soar.TagResponse
import com.example.soar.databinding.FragmentArchivingBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate


// (Business, TagResponse 데이터 클래스들은 그대로 유지)

class ArchivingFragment : Fragment() {
    private var _binding: FragmentArchivingBinding? = null
    private val binding get() = _binding!!

    // activityViewModels()를 사용해 액티비티 스코프의 ViewModel을 공유
    private val tagViewModel: TagViewModel by activityViewModels()

    private lateinit var adapter: ArchivingAdapter

    private var selectedTagIds = arrayListOf<Int>()
    private val keywordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newSelectedTagIds = result.data?.getIntegerArrayListExtra("selectedTagIds") ?: arrayListOf()
            this.selectedTagIds = newSelectedTagIds

            if (this.selectedTagIds.isEmpty()) {
                tagViewModel.resetFilteredData()
            } else {
                tagViewModel.filterDataByTagIds(this.selectedTagIds)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView & Adapter 초기화
        adapter = ArchivingAdapter()
        binding.bizList.adapter = adapter
        binding.bizList.layoutManager = LinearLayoutManager(requireContext())

        // ViewModel에 초기 데이터 로드 (Fragment의 Context가 필요하므로 여기서 호출)
        val tagList = loadDummyTagData() // JSON에서 TagResponse 전체 로드
        val initialBusinessData = loadDummyBusinessData(tagList)
        tagViewModel.setInitialData(initialBusinessData)

        // ViewModel의 dataList를 관찰하고 데이터 변경 시 어댑터 갱신
        tagViewModel.dataList.observe(viewLifecycleOwner) { newList ->
            adapter.submitList(newList)
        }

        // 버튼 클릭 리스너 설정
        binding.btnSelectKeyword.setOnClickListener {
            val intent = Intent(requireContext(), KeywordActivity::class.java).apply {
                putIntegerArrayListExtra("selectedTagIds", selectedTagIds)
            }
            keywordLauncher.launch(intent)
        }

        // (기존 버튼 리스너들은 그대로 유지)
        binding.btn1.setOnClickListener {
            val intent = Intent(requireContext(), DetailPageActivity::class.java)
            startActivity(intent)
        }
        binding.btn2.setOnClickListener {
            val intent = Intent(requireContext(), OnBoardingActivity::class.java)
            startActivity(intent)
        }
        binding.btn3.setOnClickListener {
            val intent = Intent(requireContext(), CurationSequeceActivity::class.java)
            startActivity(intent)
        }
    }

    // Fragment에서 context를 사용해 더미 데이터 로드
    private fun loadDummyTagData(): List<TagResponse> {
        val json = requireContext().assets.open("response_tags.json")
            .bufferedReader().use { it.readText() }
        val gson = Gson()
        val type = object : TypeToken<ApiResponse>() {}.type
        val response: ApiResponse = gson.fromJson(json, type)
        return response.data
    }

    private fun loadDummyBusinessData(tagList: List<TagResponse>): List<Business> {
        // (기존 loadDummyBusinessData 함수는 그대로 유지)
        return listOf(
            Business(
                id = 1,
                date = LocalDate.now(),
                title = "창업 지원 프로그램",
                type = 0,
                tags = tagList.filter { it.tagId in listOf(1, 2) }
            ),
            Business(
                id = 2,
                date = LocalDate.now(),
                title = "청년 주거 지원금",
                type = 1,
                tags = tagList.filter { it.tagId in listOf(7, 8) }
            ),
            Business(
                id = 3,
                date = LocalDate.now(),
                title = "청년 창업 멘토링 프로그램",
                type = 0,
                tags = tagList.filter { it.tagId in listOf(3, 4) }
            ),
            Business(
                id = 4,
                date = LocalDate.now(),
                title = "다문화 가정 주거 지원",
                type = 1,
                tags = tagList.filter { it.tagId in listOf(9, 10) }
            ),
            Business(
                id = 5,
                date = LocalDate.now(),
                title = "장학금 및 교육비 지원",
                type = 2,
                tags = tagList.filter { it.tagId in listOf(11, 12, 13) }
            ),
            Business(
                id = 6,
                date = LocalDate.now(),
                title = "문화 행사 티켓 무료 제공",
                type = 2,
                tags = tagList.filter { it.tagId in listOf(23, 24, 25) }
            ),
            Business(
                id = 7,
                date = LocalDate.now(),
                title = "청소년 심리 상담 지원",
                type = 3,
                tags = tagList.filter { it.tagId in listOf(26, 27) }
            )
        )
    }
}