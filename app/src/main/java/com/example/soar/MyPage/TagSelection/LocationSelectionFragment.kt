package com.example.soar.MyPage.TagSelection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.DialogLocationSelectionBinding
import androidx.fragment.app.activityViewModels
import com.example.soar.Network.tag.TagResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationSelectionFragment : Fragment(R.layout.dialog_location_selection) {

    private var _binding: DialogLocationSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var majorRegionAdapter: MajorRegionAdapter
    private lateinit var subRegionAdapter: SubRegionAdapter

    // [추가] activityViewModel 선언
    private val activityViewModel: TagSelectionViewModel by activityViewModels()

    private val locationMap = mutableMapOf<String, MutableList<TagResponse>>()
    private var selectedMajorRegion: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DialogLocationSelectionBinding.bind(view)

        // [수정] parseLocationData() 대신 ViewModel 데이터 관찰
        activityViewModel.allTags.observe(viewLifecycleOwner) { allTags ->
            if (allTags.isNullOrEmpty()) return@observe

            parseLocationData(allTags) // 받아온 데이터로 UI 구성
            setupMajorRegionRecyclerView()
            setupSubRegionRecyclerView()

            val initialMajorRegion = locationMap.keys.firstOrNull()
            if (initialMajorRegion != null) {
                selectMajorRegion(initialMajorRegion)
            }
        }
    }

    private fun setupSubRegionRecyclerView() {
        subRegionAdapter = SubRegionAdapter { selectedTag ->
            setFragmentResult(
                "location_request", bundleOf(
                    "selected_id" to selectedTag.tagId,
                    "selected_name" to selectedTag.tagName
                )
            )
            findNavController().popBackStack()
        }
        binding.rvSubRegion.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subRegionAdapter
        }
    }

    private fun parseLocationData(allTags: List<TagResponse>) {
        // 기존의 파일 로드 코드 삭제
        locationMap.clear() // 데이터를 다시 파싱하기 전에 맵을 비워줍니다.

        allTags.filter { it.fieldId == 9 }.forEach { tag ->
            // tagName을 공백 기준으로 2개로 나눕니다. (예: "서울시 강남구" -> ["서울시", "강남구"])
            val parts = tag.tagName.split(" ", limit = 2)
            if (parts.size == 2) {
                val major = parts[0] // "서울시"
                // major를 key로 하여 locationMap에 tag를 추가합니다.
                locationMap.getOrPut(major) { mutableListOf() }.add(tag)
            }
        }
    }

    private fun setupMajorRegionRecyclerView() {
        majorRegionAdapter = MajorRegionAdapter(locationMap.keys.toList()) { region ->
            selectMajorRegion(region)
        }
        binding.rvMajorRegion.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = majorRegionAdapter
        }
    }

    private fun selectMajorRegion(region: String) {
        selectedMajorRegion = region
        majorRegionAdapter.setSelected(region)
        subRegionAdapter.submitList(locationMap[region].orEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// --- RecyclerView Adapters ---

class MajorRegionAdapter(
    private val regions: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<MajorRegionAdapter.ViewHolder>() {

    private var selectedPosition = 0

    fun setSelected(region: String) {
        val newPosition = regions.indexOf(region)
        if (newPosition != -1) {
            notifyItemChanged(selectedPosition)
            selectedPosition = newPosition
            notifyItemChanged(selectedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_major_region, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(regions[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = regions.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_major_region_name)
        fun bind(region: String, isSelected: Boolean) {
            nameTextView.text = region
            itemView.isSelected = isSelected
            itemView.setBackgroundResource(if (isSelected) R.drawable.bg_region_item_selected else android.R.color.transparent)
            itemView.setOnClickListener { onClick(region) }
        }
    }
}

class SubRegionAdapter(
    private val onClick: (TagResponse) -> Unit
) : RecyclerView.Adapter<SubRegionAdapter.ViewHolder>() {

    private var subRegions: List<TagResponse> = emptyList()

    fun submitList(list: List<TagResponse>) {
        subRegions = list.sortedBy { it.tagName }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_region, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(subRegions[position])
    }

    // [추가] 누락된 getItemCount() 함수를 구현합니다.
    override fun getItemCount(): Int = subRegions.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_sub_region_name)
        fun bind(regionTag: TagResponse) {
            // [수정] 첫 번째 단어를 제외한 나머지 부분을 sub-region 이름으로 설정
            val parts = regionTag.tagName.split(" ", limit = 2)

            // parts가 2개 이상으로 나뉘었다면(예: "경기도 고양시 일산서구"), 두 번째 부분을 사용
            if (parts.size > 1) {
                nameTextView.text = parts[1] // "고양시 일산서구"
            } else {
                // 단일 단어라면(예: "기타"), 그냥 전체 이름을 표시
                nameTextView.text = regionTag.tagName
            }

            itemView.setOnClickListener { onClick(regionTag) }
        }
    }
}