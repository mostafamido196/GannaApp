package com.samy.ganna.pojo

data class Page(
    val id: Int,
    val title : String,
    var mainText: String,
    val description: String,
    val startRef:Int,
    val endRef:Int
)
