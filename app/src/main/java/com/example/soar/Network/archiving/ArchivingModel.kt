package com.example.soar.Network.archiving

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// GET /api/user-policies/bookmarks/with-meta
@Parcelize
data class BookmarkedPolicy(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("dateLabel") val dateLabel: String,
    @SerializedName("businessPeriodEnd") val businessPeriodEnd: String,
    @SerializedName("dateType") val dateType: String,
    @SerializedName("applied") val applied: Boolean,
    @SerializedName("tags") val tags: List<PolicyTag>
) : Parcelable

@Parcelize
data class PolicyTag(
    @SerializedName("tagId") val tagId: Int,
    @SerializedName("tagName") val tagName: String
) : Parcelable

// POST /api/user-policies/apply/bulk
data class ApplyPolicyRequest(
    @SerializedName("policyIds") val policyIds: List<String>
)

data class ApplyPolicyResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("requestedCount") val requestedCount: Int,
    @SerializedName("appliedCount") val appliedCount: Int,
    @SerializedName("results") val results: List<ApplyResult>
)

data class ApplyResult(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class BulkUnbookmarkRequest(
    @SerializedName("policyIds") val policyIds: List<String>
)

data class BulkUnbookmarkResponse(
    @SerializedName("requestedCount") val requestedCount: Int,
    @SerializedName("removedCount") val removedCount: Int,
    @SerializedName("skippedCount") val skippedCount: Int,
    @SerializedName("removedPolicyIds") val removedPolicyIds: List<String>
)

data class ToggleApplyResponse(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("applied") val applied: Boolean,
    @SerializedName("applyUrl") val applyUrl: String?, // URL은 없을 수도 있으므로 Nullable
    @SerializedName("message") val message: String
)

@Parcelize
data class AppliedPolicy(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("dateLabel") val dateLabel: String,
    @SerializedName("dateType") val dateType: String,
    val applied: Boolean = true
) : Parcelable