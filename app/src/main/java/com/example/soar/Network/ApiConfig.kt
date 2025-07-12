package com.example.soar.Network

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080"

    // Alarm 관련
    object Alarm {
        const val APPLICATION_END = "/api/alarm/application/end"
        const val APPLICATION_RESULT = "/api/alarm/application/result"
        const val APPLICATION_START = "/api/alarm/application/start"
        const val APPLY_CHECK = "/api/alarm/apply/check"
        const val APPLY_FINISH = "/api/alarm/apply/finish"
    }

    // Calendar 관련
    object Calendar {
        const val INIT = "/api/calendar"
        const val APPLY_PATCH = "/api/apply"
        const val BY_DATE = "/api/calendar/{date}"
    }

    // CurationSequence 관련
    object CurationSequence {
        const val SELECT = "/api/curation-sequence/select"
        const val GET_SEQUENCE = "/api/curation-sequence/{seqId}"
        const val RESULT = "/api/curation-sequence/result/{seqId}"
        const val SEARCH_ADDRESS = "/api/curation-sequence/result/{seqId}"
    }

    // Home 관련
    object Home {
        const val USER_INFO = "/api/home/user"
        const val BANNERS = "/api/home/supportbanners"
        const val SUPPORT_BANNERS = "/api/home/supportbanners"
    }

    // User 관련 (회원/로그인/비밀번호 등)
    object User {
        const val SIGNUP = "/api/auth/signup"
        const val SIGNIN = "/api/auth/signin"
        const val SIGNOUT = "/api/auth/signout"
        const val USER_INFO = "/api/auth/user-info?userEmail={userEmail}"
        const val RESET_NAME = "/api/auth/reset-name"
        const val RESET_PASSWORD = "/api/auth/reset-password"
        const val SIGNUP_OTP = "/api/auth/signup/otp"
        const val SIGNUP_OTP_CHECK = "/api/auth/signup/otp/check"
        const val FIND_ID = "/api/auth/find-id"
        const val FIND_PASSWORD = "/api/auth/find-password"
        const val KAKAO_LOGIN = "/api/auth/kakao"
    }

    // Project/My 관련
    object MyProject {
        const val USER_PROJECT_LIST = "/api/project/{userId}"
    }

    // Browse 관련
    object Browse {
        const val MAIN = "/api/browse"
        const val LIST = "/api/browse/list"
        const val SEARCH_HISTORY = "/api/browse/search/history"
        const val SUGGESTIONS = "/api/browse/search/suggestions"
        const val SEARCH = "/api/browse/search"
        const val CATEGORY = "/api/browse/category"
        const val CATEGORY_KEYWORDS = "/api/browse/category/keywords"
        const val FILTERS = "/api/browse/filters"
    }

    // Archive 관련
    object Archive {
        const val SAVE = "/api/archive/save"
        const val MAIN = "/api/archive"
        const val APPLY = "/api/archive/apply"
        const val COUNT = "/api/archive/count"
        const val EDIT = "/api/archive/edit"
        const val FILTER = "/api/archive/filter"
    }

    // SupportDetails 관련
    object SupportDetails {
        const val DETAIL = "/api/support/{id}"
        const val MORE_DETAIL = "/api/support/{id}/detail"
        const val REVIEWS = "/api/support/{id}/reviews"
        const val DOCUMENTS = "/api/support/{id}/documents"
    }
}