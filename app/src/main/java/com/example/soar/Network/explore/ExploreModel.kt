package com.example.soar.Network.explore

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class PolicyResponse(
    @SerializedName("content") val content: List<YouthPolicy>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("last") val isLast: Boolean, // 마지막 페이지 여부
    @SerializedName("number") val pageNumber: Int // 현재 페이지 번호
)

@Parcelize
data class YouthPolicy(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("policyKeyword") val policyKeyword: String?,
    @SerializedName("largeClassification") val largeClassification: String,
    @SerializedName("mediumClassification") val mediumClassification: String,
    @SerializedName("supervisingInstName") val supervisingInstName: String,
    @SerializedName("dateLabel") val dateLabel: String?,
    @SerializedName("bookmarked") val bookmarked: Boolean? = false
) : Parcelable

data class RecentPoliciesRequest(
    @SerializedName("policyIds") val policyIds: List<String>,
)
