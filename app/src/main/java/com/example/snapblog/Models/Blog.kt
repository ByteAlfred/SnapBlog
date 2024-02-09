package com.example.snapblog.Models

data class Blog(
    val blogId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val date: String = "",
    val userId: String = "",
    var likes: Int = 0


)
