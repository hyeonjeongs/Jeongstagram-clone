package com.example.jeongstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jeongstagram.LoginActivity
import com.example.jeongstagram.MainActivity
import com.example.jeongstagram.R
import com.example.jeongstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid:String? = null
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10 //static으로 선언해줌
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)

        uid = arguments?.getString("destinationUid") //이전화면에서 넘어온 값을 받아옴
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid){//uid와 현재user uid 같은경우
            //mypage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout) //follow 버튼을 로그아웃 버튼으로 설정하기
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish() //엑티비티 종료하고
                startActivity(Intent(activity,LoginActivity::class.java)) //로그인 페이지보이도록
                auth?.signOut() //파이어베이스 auth값에 로그아웃 넣어줌
            }
        }else{//상대방 userpage일 경우 누구의 user페이지인지 보여주고 back버튼 활성화
            //otherUserpage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity) //메인엑티비티 가져오기
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back.setOnClickListener {
                mainactivity.bottom_navi.selectedItemId = R.id.home
            }
            mainactivity?.toolbar_title_image?.visibility = View.GONE //이미지바 로고 숨겨주기
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back.visibility = View.VISIBLE
        }
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter() //리사이클러뷰에 어뎁터 달아주기
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity,3) //한행에 세개씩 뜨도록함
        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        return fragmentView
    }
    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener{documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!) //image url 가져와서 프로필이미지로 적용시킴
            }
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init{//내가 올린 이미지(uid)만 파이어베이스에서 가져와서 올릴 수 있도록함
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) //값이 널이면 종료시키는 코드 넣어줌
                    return@addSnapshotListener

                //Get data
                for(snapshot in querySnapshot.documents){ //querySnapshot안에 있는 사진들 받아와서 contentDto로 받아줌
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged() //리사이클러뷰 새로고침해줌
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3 //폭의 1/3크기 가져오기
            var imageview = ImageView(parent.context) //이 이미지에 폭 넣어줄거임
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width) //이미지뷰 정사각형으로 만들어줌
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {


        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {//데이터 매핑하는 곳
            var imageview = (holder as CustomViewHolder).imageview //holder를 customViewHolder로 불러옴
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUri).apply(RequestOptions().centerCrop()).into(imageview) //centeCrop - 중간으로
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}