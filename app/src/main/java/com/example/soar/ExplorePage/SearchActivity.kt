// com.example.soar.ExplorePage/SearchActivity.kt

package com.example.soar.ExplorePage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.databinding.ActivitySearchBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.soar.DetailPage.DetailPageActivity

// [수정] ExploreAdapter.OnItemClickListener 인터페이스 구현
class SearchActivity : AppCompatActivity(), ExploreAdapter.OnItemClickListener {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var resultAdapter: ExploreAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private lateinit var suggestionAdapter: SuggestionAdapter

    private val PREFS_NAME = "SearchPrefs"
    private val RECENT_SEARCHES_KEY = "recent_searches"
    private val MAX_RECENT_SEARCHES_SAVED = 20
    private val MAX_RECENT_SEARCHES_DISPLAY = 6
    private var allRecentSearches: MutableList<String> = mutableListOf()

    private val changedBookmarks = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecentSearch()
        setupResultList()
        setupListeners()
        setupObservers()
        loadRecentSearches()
    }

    private fun setupResultList() {
        // [수정] ExploreAdapter 생성 시 this (SearchActivity)를 리스너로 전달
        resultAdapter = ExploreAdapter(this)
        binding.bizList.apply {
            adapter = resultAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition =
                        layoutManager.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition == totalItemCount - 1 && viewModel.isLoading.value == false) {
                        viewModel.searchPolicies(viewModel.currentQuery)
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.policies.observe(this) { policies ->
            resultAdapter.submitList(policies)
            binding.textNoResults.visibility =
                if (policies.isEmpty() && viewModel.isLoading.value == false) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.textNoResults.visibility = View.GONE
            }
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecentSearch() {
        binding.recentSearchContainer.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentSearchAdapter = RecentSearchAdapter(this) { searchTerm ->
            binding.searchEditText.setText(searchTerm)
            binding.searchEditText.setSelection(searchTerm.length)
            Handler(Looper.getMainLooper()).postDelayed({ performSearchWithQuery() }, 200)
        }
        binding.recentSearchContainer.adapter = recentSearchAdapter

        binding.searchSuggestionsContainer.layoutManager = LinearLayoutManager(this)
        suggestionAdapter = SuggestionAdapter { searchTerm ->
            binding.searchEditText.setText(searchTerm)
            performSearchWithQuery()
        }
        binding.searchSuggestionsContainer.adapter = suggestionAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearchWithQuery()
                hideKeyboard(v)
                true
            } else false
        }

        binding.searchImg.setOnClickListener {
            performSearchWithQuery()
            hideKeyboard(binding.searchEditText)
        }

        binding.deleteAll.setOnClickListener { clearAllRecentSearches() }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) showSuggestions(s.toString()) else hideSuggestions()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearchWithQuery() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            addRecentSearch(query)
            performSearch(query)
            hideSuggestions()
        }
    }

    private fun performSearch(query: String) {
        binding.searchView.visibility = View.GONE
        binding.afterSearchView.visibility = View.VISIBLE
        viewModel.searchPolicies(query, isNewSearch = true)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadRecentSearches() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(RECENT_SEARCHES_KEY, null)
        if (json != null) {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            allRecentSearches = Gson().fromJson(json, type)
            val displaySearches = allRecentSearches.take(MAX_RECENT_SEARCHES_DISPLAY)
            recentSearchAdapter.updateData(displaySearches)
        }
    }

    private fun saveRecentSearches() {
        val editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(RECENT_SEARCHES_KEY, Gson().toJson(allRecentSearches))
        editor.apply()
    }

    private fun addRecentSearch(query: String) {
        allRecentSearches.remove(query)
        allRecentSearches.add(0, query)
        while (allRecentSearches.size > MAX_RECENT_SEARCHES_SAVED) {
            allRecentSearches.removeAt(allRecentSearches.size - 1)
        }
        saveRecentSearches()
        recentSearchAdapter.updateData(allRecentSearches.take(MAX_RECENT_SEARCHES_DISPLAY))
    }

    fun deleteRecentSearch(query: String) {
        allRecentSearches.remove(query)
        saveRecentSearches()
        recentSearchAdapter.updateData(allRecentSearches.take(MAX_RECENT_SEARCHES_DISPLAY))
    }

    private fun clearAllRecentSearches() {
        allRecentSearches.clear()
        saveRecentSearches()
        recentSearchAdapter.updateData(emptyList())
    }

    private fun showSuggestions(query: String) {
        val suggestions = allRecentSearches.filter { it.contains(query, ignoreCase = true) }
        suggestionAdapter.updateData(suggestions, query)
        binding.searchSuggestionsContainer.visibility =
            if (suggestions.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun hideSuggestions() {
        binding.searchSuggestionsContainer.visibility = View.GONE
    }

    // [추가] ExploreAdapter.OnItemClickListener 인터페이스 구현
    override fun onPolicyItemClick(policy: YouthPolicy) {
        val intent = Intent(this, DetailPageActivity::class.java).apply {
            putExtra("policyId", policy.policyId)
        }
        startActivity(intent)
    }

    override fun onBookmarkClick(policy: YouthPolicy) {
        val accessToken = com.example.soar.Network.TokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            // ✨수정: 변경된 북마크 상태를 changedBookmarks에 기록✨
            val newBookmarkState = !(policy.bookmarked ?: false)
            changedBookmarks[policy.policyId] = newBookmarkState

            viewModel.toggleBookmark(policy)
        } else {
            Toast.makeText(this, "로그인 후 이용할 수 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        // 변경된 북마크 정보를 직렬화 가능한 형태로 변환하여 Intent에 추가
        resultIntent.putExtra("changedBookmarks", HashMap(changedBookmarks))
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }
}