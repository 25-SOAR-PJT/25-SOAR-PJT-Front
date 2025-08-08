package com.example.soar.EntryPage.SignIn

import android.app.Application
import android.util.Log
import com.example.soar.R
import com.example.soar.Network.TokenManager
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class KakaoApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        TokenManager.init(applicationContext)

        var keyHash = Utility.getKeyHash(this)
        Log.d("키 확인 : ", keyHash)
        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))

    }

}
