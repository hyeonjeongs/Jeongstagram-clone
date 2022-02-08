package com.example.jeongstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jeongstagram.R
import com.example.jeongstagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0 //요청 코드
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null //Uri 담을 수 있는 곳
    var auth : FirebaseAuth? = null //파이어베이스에서 유저 정보 가져옴
    var firestore : FirebaseFirestore? = null //데이터베이스 사용할 수있도록 파이어 스토어 추가
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initiate(초기화)
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //open the album(엑티비티 실행하자마자 화면 열릴수 있도록 해주는 코드)
        var photoPikerIntent = Intent(Intent.ACTION_PICK)
        photoPikerIntent.type = "image/*"
        startActivityForResult(photoPikerIntent,PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        addphoto_btn.setOnClickListener {
            contentUpload()
        }
    }

    //선택한 이미지 받는 부분
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_FROM_ALBUM){
            if(resultCode== Activity.RESULT_OK){ //결과 값이 사진을 선택했을때
                //This is path to the selected image(이미지경로 넘어옴)
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            }else{
                //Edit the addPhotoActivity if you leave the album without selecting it(취소버튼 누르면)
                finish() //엑티비티 닫아줌
            }
        }
    }
    fun contentUpload(){
        //Make filename

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) //파일 이름 만들어주는 코드(이름 중복생성되지 않도록 만들어줌)
        var imageFileName = "IMAGE_" + timestamp + "_.png" //중복생성되지 않는 파일명 만들어짐

        var storageRef = storage?.reference?.child("images")?.child(imageFileName) //이미지를 업로드(images 폴더명에 넣어주고 파일명으로 넣어줌)

        //Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()
            //insert downloadUrl of image
            contentDTO.imageUri = uri.toString()

            //insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert suerId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO) //images 콜렉션에 넣어줌

            setResult(Activity.RESULT_OK) //정상적으로 닫혔다는 frag값을 넘기기 위해 이 값을 넣어줌

            finish()
        }

        //Callback method
        /*storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                //insert downloadUrl of image
                contentDTO.imageUri = uri.toString()

                //insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert suerId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO) //images 콜렉션에 넣어줌

                setResult(Activity.RESULT_OK) //정상적으로 닫혔다는 frag값을 넘기기 위해 이 값을 넣어줌

                finish()

            }
        }*/
    }
}