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
        const val APPLICATION_FCMTOKEN = "/api/fcm/apply/token"
        const val APPLICATION_POLICY_NOTICE = "/api/alarm/notice/policy"
        const val APPLICATION_ATTENDANCE = "/api/alarm/attendance_check"
    }

    // Calendar 관련
    object Calendar {
        const val INIT = "/api/calendar"
        const val APPLY_PATCH = "/api/apply"
        const val BY_DATE = "/api/calendar/{date}"
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
        const val USER_INFO = "/api/auth/get-userinfo"
        const val RESET_NAME = "/api/auth/reset-name"
        const val RESET_PASSWORD = "/api/auth/reset-password"
        const val SIGNUP_OTP = "/api/auth/signup/otp"
        const val SIGNUP_OTP_CHECK = "/api/auth/signup/otp/check"
        const val FIND_ID = "/api/auth/find-id"
        const val FIND_PASSWORD = "/api/auth/find-password"
        const val KAKAO_LOGIN = "/api/auth/kakao"
        const val TAG = "/api/user-tag/user"
        const val TAG_MODIFY = "/api/user-tag/"
        const val UPDATE_PW = "/api/auth/update-password"
    }

    // Project/My 관련
    object MyProject {
        const val USER_PROJECT_LIST = "/api/project/{userId}"
    }

    // Explore 관련
    object Explore {
        const val MAIN = "/api/youth-policy/main"
        const val MAIN_WITH_LOGIN = "/api/user-policy/main"
        const val MULTI_SEARCH = "/api/youth-policy/search/multi"
    }

    // UserYouthPolicy 관련
    object UserYouthPolicy{
        const val MAIN ="/api/user-policies/mainLoginTags"
        const val TOGGLE_BOOKMARK = "/api/user-policies/{policyId}/bookmarks/toggle"
    }

    // UserYouthPolicyTag 관련
    object UserYouthPolicyTag{
        const val QS = "/api/youth-policy-tag/tags/qs"
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

    // Details 관련
    object Details {
        const val DETAIL_BY_ID = "/api/youth-policy/{policyId}"
    }


    // Tag 관련
    object Tag {
        const val TAG = "/api/tag/"
    }

    object CurationSequence {
        const val MULTI_SEARCH = "/api/youth-policy/search/multi"
    }
}