// 파일 경로: com/example/soar/Network/tag/TagsResponse.kt
package com.example.soar.Network.tag

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TagResponse(
    @SerializedName("tagId") val tagId: Int,
    @SerializedName("tagName") val tagName: String,
    @SerializedName("fieldId") val fieldId: Int
) : Parcelable


data class TagIdRequest(
    @SerializedName("tagId")
    val tagId: List<Int>
)
