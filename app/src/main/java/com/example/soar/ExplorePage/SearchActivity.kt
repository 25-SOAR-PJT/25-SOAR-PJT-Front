// SearchActivity.kt
package com.example.soar.ExplorePage // Replace with your actual package name

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.databinding.ActivitySearchBinding
import com.example.soar.databinding.ItemRecentSearchBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SearchActivity : AppCompatActivity() {

    // Declare the binding object
    private lateinit var binding: ActivitySearchBinding

    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private lateinit var suggestionAdapter: SuggestionAdapter // 검색 제안 어댑터

    // Key for SharedPreferences to store recent searches
    private val PREFS_NAME = "SearchPrefs"
    private val RECENT_SEARCHES_KEY = "recent_searches"
    private val MAX_RECENT_SEARCHES_SAVED = 20 // 저장할 최대 검색어 개수 (20개)
    private val MAX_RECENT_SEARCHES_DISPLAY = 6 // 화면에 보여줄 최대 최근 검색어 개수 (6개)

    // 모든 저장된 검색어 리스트
    private var allRecentSearches: MutableList<String> = mutableListOf()

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        // Inflate the layout using view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the root view of the binding

        // Set up back button click listener using binding
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- 최근 검색어 RecyclerView 설정 ---
        binding.recentSearchContainer.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentSearchAdapter = RecentSearchAdapter(this) { searchTerm ->
            // Handle clicking on a recent search term (e.g., set it to the search bar and perform search)
            binding.searchEditText.setText(searchTerm)
            binding.searchEditText.setSelection(searchTerm.length)
            //performSearchWithQuery()
            // 1초(1000ms) 지연 후 검색을 실행
            Handler(Looper.getMainLooper()).postDelayed({
                performSearchWithQuery()
            }, 200) // 1000ms = 1초
        }
        binding.recentSearchContainer.adapter = recentSearchAdapter

        // --- 검색 제안 RecyclerView 설정 ---
        binding.searchSuggestionsContainer.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        // SuggestionAdapter 생성 시, onSuggestionClick 람다 전달
        suggestionAdapter = SuggestionAdapter { searchTerm ->
            // 제안 클릭 시 검색창에 텍스트 채우고 검색 실행
            binding.searchEditText.setText(searchTerm)
            performSearchWithQuery()
        }
        binding.searchSuggestionsContainer.adapter = suggestionAdapter


        // Load recent searches when activity starts
        loadRecentSearches()

        // Set up search input listener for Enter key
        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearchWithQuery()
                // Hide keyboard after search
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

        // Set up search icon click listener
        binding.searchImg.setOnClickListener {
            performSearchWithQuery()
            // 검색 완료 후 키보드를 숨김
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }

        // Set up "Delete All" click listener using binding
        binding.deleteAll.setOnClickListener {
            clearAllRecentSearches()
        }

        // --- TextWatcher 설정: 입력이 들어올 때마다 제안 업데이트 ---
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s.toString()
                if (currentText.isNotEmpty()) {
                    showSuggestions(currentText)
                } else {
                    hideSuggestions()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })

        // TODO: 사업 연결하는 아답터 만들어야 함. recycler view 이름: item_explore_biz (둘러보기 frag에서 쓰고 그대로 가져오면 될듯)
    }

    // --- Recent Search / Suggestion Logic ---

    private fun loadRecentSearches() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(RECENT_SEARCHES_KEY, null)
        if (json != null) {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            allRecentSearches = Gson().fromJson(json, type)
            // 화면에 보여줄 6개의 최근 검색어만 어댑터에 전달
            val displaySearches = allRecentSearches.take(MAX_RECENT_SEARCHES_DISPLAY)
            recentSearchAdapter.updateData(displaySearches)
        }
    }

    private fun saveRecentSearches() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val json = Gson().toJson(allRecentSearches)
        editor.putString(RECENT_SEARCHES_KEY, json)
        editor.apply()
    }

    private fun addRecentSearch(query: String) {
        // 기존 검색어 중복 제거 및 최신화
        allRecentSearches.remove(query)
        allRecentSearches.add(0, query)

        // 저장 개수 제한 (20개)
        while (allRecentSearches.size > MAX_RECENT_SEARCHES_SAVED) {
            allRecentSearches.removeAt(allRecentSearches.size - 1)
        }
        saveRecentSearches()

        // 화면에 보여줄 6개의 최근 검색어만 어댑터에 전달
        val displaySearches = allRecentSearches.take(MAX_RECENT_SEARCHES_DISPLAY)
        recentSearchAdapter.updateData(displaySearches)
    }

    public fun deleteRecentSearch(query: String) {
        allRecentSearches.remove(query)
        saveRecentSearches()

        // 화면에 보여줄 6개의 최근 검색어만 어댑터에 전달
        val displaySearches = allRecentSearches.take(MAX_RECENT_SEARCHES_DISPLAY)
        recentSearchAdapter.updateData(displaySearches)
    }

    private fun clearAllRecentSearches() {
        allRecentSearches.clear()
        saveRecentSearches()
        recentSearchAdapter.updateData(emptyList())
    }

    private fun performSearchWithQuery() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            addRecentSearch(query)
            performSearch(query) // 실제 검색 로직 호출
            //binding.searchEditText.text.clear() // 검색 완료 후 EditText 비우기
            hideSuggestions() // 검색 후 제안 숨기기
            showRecentSearches() // 검색 후 최근 검색어 다시 보이기
        }
    }

    private fun performSearch(query: String) {
        binding.searchView.visibility = View.GONE
        binding.afterSearchView.visibility = View.VISIBLE
        binding.afterSearchText.text = "${query} 검색완료"
    }

    // --- 검색 제안 관련 함수 ---
    private fun showSuggestions(query: String) {
        val suggestions = allRecentSearches.filter {
            it.contains(query, ignoreCase = true) // 대소문자 구분 없이 포함 여부 확인
        }
        // 제안 목록과 함께 현재 검색어를 어댑터에 전달
        suggestionAdapter.updateData(suggestions, query)

        // 제안이 있을 경우 제안 RecyclerView를 보이게 하고 최근 검색어 섹션을 숨김
        if (suggestions.isNotEmpty()) {
            binding.searchSuggestionsContainer.visibility = View.VISIBLE
        } else {
            // 제안이 없으면 제안 RecyclerView를 숨기고 최근 검색어 섹션을 다시 보이게 함
            hideSuggestions()
        }
    }

    private fun hideSuggestions() {
        binding.searchSuggestionsContainer.visibility = View.GONE
        showRecentSearches() // 제안이 없을 때 최근 검색어를 다시 보이게 함
    }

    private fun showRecentSearches() {
        binding.recentSearchContainer.visibility = View.VISIBLE
    }

    // --- RecentSearchAdapter (최근 검색어) ---
    class RecentSearchAdapter(
        private val context: Context,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

        private val recentSearches: MutableList<String> = mutableListOf()

        fun updateData(newSearches: List<String>) {
            recentSearches.clear()
            recentSearches.addAll(newSearches)
            notifyDataSetChanged()
        }

        fun getData(): List<String> {
            return recentSearches
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val searchTerm = recentSearches[position]
            holder.bind(searchTerm)
        }

        override fun getItemCount(): Int {
            return recentSearches.size
        }

        inner class ViewHolder(private val binding: ItemRecentSearchBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(searchTerm: String) {
                binding.textSearch.text = searchTerm
                binding.textSearch.setOnClickListener { onItemClick(searchTerm) }
                binding.btnDelete.setOnClickListener {
                    if (context is SearchActivity) {
                        context.deleteRecentSearch(searchTerm)
                    }
                }
            }
        }
    }

}