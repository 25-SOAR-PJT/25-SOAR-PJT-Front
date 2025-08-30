package com.example.soar.Network.home

import com.google.gson.annotations.SerializedName

data class LatestPolicy(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("dateLabel") val dateLabel: String,
    @SerializedName("businessPeriodEnd") val businessPeriodEnd: String
)

data class PopularPolicy(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("largeClassification") val largeClassification: String
)

data class AgePopularPolicy(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("supervisingInstName") val supervisingInstName: String,
    @SerializedName("dateLabel") val dateLabel: String,
    @SerializedName("bookmarked") val bookmarked: Boolean,
    @SerializedName("ageGroup") val ageGroup: String
)
