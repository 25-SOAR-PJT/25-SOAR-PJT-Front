package com.example.soar.AlarmPage

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.soar.R

class AlarmSettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.alarm_setting, rootKey)

        // 변경 이벤트(선택)
        findPreference<SwitchPreferenceCompat>("remind_result")
            ?.setOnPreferenceChangeListener { _, newValue ->
                val on = newValue as Boolean
                // TODO: 토글 시 부가 작업
                true // 변경 허용
            }
    }
}