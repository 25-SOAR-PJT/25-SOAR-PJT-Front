// HomeViewModel.kt
package com.example.soar.HomePage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.explore.ExploreRepository
import com.example.soar.Network.home.AgePopularPolicy
import com.example.soar.Network.home.BannerResponse
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.user.UserInfoResponse
import com.example.soar.Network.home.LatestPolicy
import com.example.soar.Network.home.HomeRepository
import com.example.soar.Network.home.PopularPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val homeRepository = HomeRepository()
    private val exploreRepository = ExploreRepository()

    // ---- 기존 LiveData들 유지 ----
    private val _userInfo = MutableLiveData<UserInfoResponse>()
    val userInfo: LiveData<UserInfoResponse> get() = _userInfo

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _banners = MutableLiveData<List<BannerResponse>>()
    val banners: LiveData<List<BannerResponse>> get() = _banners

    private val _latestPolicy = MutableLiveData<LatestPolicy?>()
    val latestPolicy: LiveData<LatestPolicy?> get() = _latestPolicy

    private val _popularPolicies = MutableLiveData<List<PopularPolicy>>()
    val popularPolicies: LiveData<List<PopularPolicy>> get() = _popularPolicies

    private val _agePopularPolicies = MutableLiveData<List<AgePopularPolicy>>()
    val agePopularPolicies: LiveData<List<AgePopularPolicy>> get() = _agePopularPolicies

    // ---- 단발성 UI 이벤트(로그아웃 지시 등) ----
    private val _shouldForceLogout = MutableLiveData<Boolean>()
    val shouldForceLogout: LiveData<Boolean> get() = _shouldForceLogout

    /** 기존 단건 호출 (그대로 둠) */
    fun fetchUserInfo() {
        viewModelScope.launch {
            authRepository.getUserInfo()
                .onSuccess { _userInfo.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    /** ✅ 재시도 포함 버전: 최대 5회 */
    fun fetchUserInfoWithRetry(
        maxAttempts: Int = 5,
        initialBackoffMs: Long = 600L
    ) {
        viewModelScope.launch {
            var attempt = 1
            var backoff = initialBackoffMs

            while (attempt <= maxAttempts) {
                val result = runCatching { authRepository.getUserInfo() }.getOrNull()

                if (result?.isSuccess == true) {
                    _userInfo.value = result.getOrNull()
                    return@launch
                } else {
                    // 실패 메시지는 에러 LiveData로도 흘려보내 UI가 참고 가능
                    _error.value = result?.exceptionOrNull()?.message ?: "사용자 정보 조회 실패 (시도 $attempt)"
                    Log.e("error","사용자 정보 조회 실패 (시도 $attempt)" )
                }

                if (attempt == maxAttempts) break

                delay(backoff)
                backoff = (backoff * 1.6f).toLong().coerceAtMost(4000L)
                attempt++
            }

            // 모두 실패 → Fragment에 '로그아웃 해' 신호
            _shouldForceLogout.value = true
        }
    }

    fun fetchBanners() {
        viewModelScope.launch {
            homeRepository.getBanners()
                .onSuccess { _banners.value = it }
                .onFailure { _error.value = it.message ?: "배너를 불러오는 데 실패했습니다." }
        }
    }

    fun fetchLatestPolicy() {
        viewModelScope.launch {
            homeRepository.getLatestPolicy()
                .onSuccess { _latestPolicy.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchPopularPolicy() {
        viewModelScope.launch {
            homeRepository.getPopularPolicies()
                .onSuccess { _popularPolicies.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchAgePopularPolicies() {
        viewModelScope.launch {
            homeRepository.getAgePopularPolicies()
                .onSuccess { _agePopularPolicies.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun toggleBookmarkForAgePolicy(policy: AgePopularPolicy) {
        viewModelScope.launch {
            exploreRepository.toggleBookmark(policy.policyId)
                .onSuccess {
                    val current = _agePopularPolicies.value?.toMutableList() ?: return@onSuccess
                    val idx = current.indexOfFirst { it.policyId == policy.policyId }
                    if (idx != -1) {
                        current[idx] = policy.copy(bookmarked = !policy.bookmarked)
                        _agePopularPolicies.value = current
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }
}
