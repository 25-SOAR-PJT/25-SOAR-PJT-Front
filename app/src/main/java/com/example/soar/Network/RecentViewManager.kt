package com.example.soar.Network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RecentViewManager {
    private const val PREF_NAME = "SOARPrefs"
    private const val KEY_RECENT_POLICIES = "recent_policies"
    private const val MAX_SIZE = 5

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 최근 본 정책 ID 리스트를 가져옵니다.
     * @return 저장된 policyId 리스트 (최신순)
     */
    fun getRecentPolicies(): List<String> {
        val json = prefs.getString(KEY_RECENT_POLICIES, null) ?: return emptyList()
        // 저장된 JSON 문자열을 List<String>으로 변환
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * 새로운 정책 ID를 최근 본 목록에 추가합니다.
     * - 이미 목록에 있으면 가장 앞으로 이동시킵니다.
     * - 목록이 5개를 초과하면 가장 오래된 항목을 제거합니다.
     * @param policyId 새로 조회한 정책의 ID
     */
    fun addPolicy(policyId: String) {
        val currentList = getRecentPolicies().toMutableList()

        // 1. 만약 ID가 이미 리스트에 있다면, 기존 것을 제거합니다. (순서를 최신으로 바꾸기 위해)
        currentList.remove(policyId)

        // 2. 리스트의 가장 앞에 (인덱스 0) 새로운 ID를 추가합니다.
        currentList.add(0, policyId)

        // 3. 리스트의 크기가 최대 크기를 초과하면, 가장 오래된 항목(마지막 아이템)을 제거합니다.
        while (currentList.size > MAX_SIZE) {
            currentList.removeAt(currentList.lastIndex)
        }

        // 4. 변경된 리스트를 다시 JSON 문자열로 변환하여 저장합니다.
        val json = gson.toJson(currentList)
        prefs.edit().putString(KEY_RECENT_POLICIES, json).apply()
    }
}