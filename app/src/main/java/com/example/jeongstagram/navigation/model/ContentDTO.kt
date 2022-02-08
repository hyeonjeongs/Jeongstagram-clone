package com.example.jeongstagram.navigation.model

class ContentDTO(var explain : String? =null, //설명관리
                 var imageUri : String? = null, //이미지주소관리
                 var uid : String? = null, //올린 유저의 이미지 관리해주는 유저관리
                 var userId :String? = null, //어떤 유저가 올린지 id관리
                 var timestamp : Long? = null, //몇시몇분에 올린지 관리
                 var favoriteCount : Int = 0, //좋아요 몇개 눌렀는지 관리해주는 변수
                 var favorites : MutableMap<String,Boolean> = HashMap()){ //중복 좋아요 방지(좋아요 누른 유저 관리)
    //댓글 관리해주는 Comment 클래스
    data class Comment(var uid : String? = null, //user 관리
                       var userId : String? = null,//이메일 관련 유저아이디 관리
                       var comment : String? = null, //댓글 관리
                       var timestamp : Long? = null) //몇시몇분에 올렸는지 관리
}