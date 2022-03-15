package com.example.jeongstagram.navigation.model

data class FollowDTO(
        var followerCount: Int = 0,
        var followers : MutableMap<String, Boolean> = HashMap(), //중복 팔로워 방지

        var followingCount :Int = 0,
        var follwings : MutableMap<String,Boolean> = HashMap()
)