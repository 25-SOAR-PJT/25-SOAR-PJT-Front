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

data class CommentRequest(
    @SerializedName("comment") var comment: String,
    @SerializedName("policyId") var policyId: String
)

data class CommentResponse (
    @SerializedName("commentId") val commentId: Long,
    @SerializedName("comment") val comment: String,
    @SerializedName("policyId") val policyId: String,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("createdDate") val createdDate: String
)

data class PolicyStepDetail(
    @SerializedName("id") val id: Long,
    @SerializedName("policyId") val policyId: String,
    @SerializedName("submittedDocuments") val submittedDocuments: String?,
    @SerializedName("applyStep") val applyStep: String?,
    @SerializedName("documentStep") val documentStep: String?,
    @SerializedName("noticeStep") val noticeStep: String?,
    @SerializedName("caution") val caution: String?
)