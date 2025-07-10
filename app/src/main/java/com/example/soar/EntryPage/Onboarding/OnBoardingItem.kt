package com.example.soar.EntryPage.Onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnBoard(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subRes: Int
)

