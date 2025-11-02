import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String
)
data class ApplicationPolicyNoticeRequest(
    @SerializedName("policyId") val policyId: Long
)