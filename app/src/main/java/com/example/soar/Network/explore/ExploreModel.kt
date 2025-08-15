// 파일 경로: com/example/soar/Network/tag/TagsResponse.kt
package com.example.soar.Network.tag

import com.google.gson.annotations.SerializedName

data class TagResponse(
    @SerializedName("tagId") val tagId: Int,
    @SerializedName("tagName") val tagName: String,
    @SerializedName("fieldId") val fieldId: Int
)

