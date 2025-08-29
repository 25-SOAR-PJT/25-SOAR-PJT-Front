package com.example.soar.Network.detail

import com.google.gson.annotations.SerializedName

data class YouthPolicyDetail(
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("supervisingInstName") val supervisingInstName: String,
    @SerializedName("largeClassification") val largeClassification: String,
    @SerializedName("mediumClassification") val mediumClassification: String,
    @SerializedName("dateLabel") val dateLabel: String?,
    @SerializedName("policyExplanation") val policyExplanation: String?,
    @SerializedName("policySupportContent") val policySupportContent: String?,
    @SerializedName("applyMethodContent") val applyMethodContent: String?,
    @SerializedName("submitDocumentContent") val submitDocumentContent: String?,
    @SerializedName("policyKeyword") val policyKeyword: String?,
    @SerializedName("etcMatterContent") val etcMatterContent: String?,
    @SerializedName("supportTargetMinAge") val supportTargetMinAge: Int,
    @SerializedName("supportTargetMaxAge") val supportTargetMaxAge: Int,
    @SerializedName("businessPeriodEtc") val businessPeriodEtc: String?,
    @SerializedName("businessPeriodStart") val businessPeriodStart: String?,
    @SerializedName("businessPeriodEnd") val businessPeriodEnd: String?,
    @SerializedName("dateType") val dateType: String?
)