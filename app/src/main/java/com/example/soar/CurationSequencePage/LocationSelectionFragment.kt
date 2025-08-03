package com.example.soar.CurationSequencePage

import android.os.Bundle
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationSelectionFragment : Fragment(R.layout.dialog_location_selection) {

    private var _binding: DialogLocationSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var majorRegionAdapter: MajorRegionAdapter
    private lateinit var subRegionAdapter: SubRegionAdapter

    private val locationMap = mutableMapOf<String, MutableList<TagResponse>>()
    private var selectedMajorRegion: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DialogLocationSelectionBinding.bind(view)

        parseLocationData()
        setupMajorRegionRecyclerView()
        setupSubRegionRecyclerView()

        val initialMajorRegion = locationMap.keys.firstOrNull()
        if (initialMajorRegion != null) {
            selectMajorRegion(initialMajorRegion)
        }
    }

    private fun setupSubRegionRecyclerView() {
        subRegionAdapter = SubRegionAdapter { selectedTag ->
            setFragmentResult("location_request", bundleOf(
                "selected_id" to selectedTag.tagId,
                "selected_name" to selectedTag.tagName
            ))
            findNavController().popBackStack()
        }
        binding.rvSubRegion.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subRegionAdapter
        }
    }


    private fun parseLocationData() {
        val jsonString = requireContext().assets.open("response_tags.json")
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<ApiResponse>() {}.type
        val response: ApiResponse = Gson().fromJson(jsonString, type)

        response.data.filter { it.fieldId == 10 }.forEach { tag ->
            val parts = tag.tagName.split(" ", limit = 2)
            if (parts.size == 2) {
                val major = parts[0]
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
        subRegions = list
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
            nameTextView.text = regionTag.tagName.split(" ").last()
            itemView.setOnClickListener { onClick(regionTag) }
        }
    }
}