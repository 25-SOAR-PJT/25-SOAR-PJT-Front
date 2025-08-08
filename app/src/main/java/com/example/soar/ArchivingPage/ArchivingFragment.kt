package com.example.soar.ArchivingPage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.CalendarPage.CalendarScheduleAdapter
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.CurationSequencePage.CurationSequeceActivity
import com.example.soar.R
import com.example.soar.databinding.FragmentArchivingBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalDate

data class Business(
    val id: Int,
    val date: LocalDate,
    val title: String,
    val type: Int,
    val tags: List<TagResponse>,
    var isApplied: Boolean = false,
    var isBookmarked: Boolean = false
)

data class TagResponse(
    val tagId: Int,
    val tagName: String,
    val fieldId: Int
)

class ArchivingFragment : Fragment() {
    private var _binding: FragmentArchivingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ArchivingAdapter
    private val dataList = mutableListOf<Business>()
    private val originalDataList = mutableListOf<Business>() // 초기 전체 데이터 저장

    // ActivityResultLauncher 등록
    private val keywordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedTagIds = result.data?.getIntegerArrayListExtra("selectedTagIds") ?: arrayListOf()

            Log.d("ArchivingFragment", "받은 selectedTagIds = $selectedTagIds")

            if (selectedTagIds.isEmpty()) {
                resetFilteredData()
            } else {
                filterDataByTagIds(selectedTagIds)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivingBinding.inflate(inflater, container, false)

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

        binding.btnSelectKeyword.setOnClickListener{
            binding.btnSelectKeyword.setOnClickListener {
                val intent = Intent(requireContext(), KeywordActivity::class.java)
                keywordLauncher.launch(intent)
            }
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView & Adapter 초기화
        adapter = ArchivingAdapter()
        binding.bizList.adapter = adapter
        binding.bizList.layoutManager = LinearLayoutManager(requireContext())

        // 더미 태그 데이터 연결
        val tagList = loadDummyTagData() // JSON에서 TagResponse 전체 로드

        // 더미 데이터 or API 데이터 로드 후 전체 리스트 초기화
        originalDataList.addAll(loadDummyBusinessData(tagList))

        // 전체 데이터로 리스트 표시
        adapter.submitList(originalDataList)

        Log.d("ArchivingFragment", "business size = ${originalDataList.size}")

        // 태그 선택 이벤트 결과 수신
        parentFragmentManager.setFragmentResultListener("tagResultKey", this) { _, bundle ->
            val selectedTagIds = bundle.getIntegerArrayList("selectedTagIds") ?: emptyList()
            if (selectedTagIds.isEmpty()) {
                resetFilteredData()
            } else {
                filterDataByTagIds(selectedTagIds)
            }
        }
    }

    // 더미데이터 연결
    private fun loadDummyBusinessData(tagList: List<TagResponse>): List<Business> {
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

    // 추후 삭제
    private fun loadDummyTagData(): List<TagResponse> {
        val json = requireContext().assets.open("response_tags.json")
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


    //api 연결 후 사용
//    private fun loadAllData() {
//        // 전체 데이터 로드 후 originalDataList 저장
//        originalDataList.clear()
//        originalDataList.addAll(apiLoadData())
//
//        resetFilteredData()
//    }

    private fun filterDataByTagIds(tagIds: List<Int>) {
        // 데이터 필터링 로직
        val filtered = originalDataList.filter { business ->
            business.tags.any { tag -> tagIds.contains(tag.tagId) }
        }
        adapter.submitList(filtered)
    }

    private fun resetFilteredData() {
        // 1. 전체 데이터 다시 로딩 (API 호출 or 로컬 전체 데이터 복원)
        dataList.clear()
        dataList.addAll(originalDataList) // originalDataList = 초기 전체 데이터

        // 2. 어댑터 갱신
        adapter.submitList(dataList)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}