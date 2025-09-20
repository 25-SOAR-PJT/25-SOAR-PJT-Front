package com.example.soar

import com.example.soar.Network.tag.TagResponse
import java.time.LocalDate

data class Business(
    val id: Int,
    val date: LocalDate,
    val title: String,
    val type: Int,
    val tags: List<TagResponse>,
    var isApplied: Boolean = false,
    var isBookmarked: Boolean = false
)

/*
data class TagResponse(
    val tagId: Int,
    val tagName: String,
    val fieldId: Int
)*/


data class ApiResponse(
    val status: String,
    val data: List<TagResponse>
)

data class EmailResultItem(val email: String)