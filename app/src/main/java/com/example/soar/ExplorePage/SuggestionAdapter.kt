package com.example.soar.ExplorePage

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.ItemRecentSearchBinding
import com.example.soar.databinding.ItemSearchSuggestionBinding

// --- SuggestionAdapter (검색 제안) ---
// RecentSearchAdapter와 동일한 레이아웃을 사용하므로 ViewHolder 구조가 유사함
class SuggestionAdapter(
    private val onSuggestionClick: (String) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    private val suggestions: MutableList<String> = mutableListOf()
    private var currentQuery: String = "" // 현재 검색어를 저장할 변수

    fun updateData(newSuggestions: List<String>, query: String) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        currentQuery = query // 현재 검색어 업데이트
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // item_search_same.xml 레이아웃을 사용
        val binding = ItemSearchSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchTerm = suggestions[position]
        holder.bind(searchTerm, currentQuery) // 현재 검색어도 함께 전달
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    inner class ViewHolder(private val binding: ItemSearchSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchTerm: String, query: String) {
            // 일치하는 부분만 텍스트 색상 변경
            val startIndex = searchTerm.indexOf(query, ignoreCase = true)
            if (startIndex != -1) {
                val spannableString = SpannableString(searchTerm)
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.ref_blue_500)), // 강조할 색상
                    startIndex,
                    startIndex + query.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.textSuggestion.text = spannableString
            } else {
                binding.textSuggestion.text = searchTerm
            }

            // 제안 클릭 리스너 설정
            itemView.setOnClickListener {
                onSuggestionClick(searchTerm)
            }
        }
    }
}

