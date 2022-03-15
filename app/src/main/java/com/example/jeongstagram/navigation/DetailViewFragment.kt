package com.example.jeongstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jeongstagram.R
import com.example.jeongstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null //파이어베이스 변수 만들
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore = FirebaseFirestore.getInstance() //초기화
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_rv.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_rv.layoutManager = LinearLayoutManager(activity) //화면 세로로 입력하기 위해 Linear로 하기
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUIDList : ArrayList<String> = arrayListOf() //content uid에 접근할 수 있는 배열

        init{
            //데이터베이스에 접근해서 데이터 받아오는 쿼리(시간순으로 받아옴,스냅샷 찍음)
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear() //초기화
                contentUIDList.clear() //초기화
                if(querySnapshot == null)
                    return@addSnapshotListener //프로그램 안정성 높임
                for(snapshot in querySnapshot!!.documents){//스냅샷 돌리면서 넘어오는 데이터에 접근
                    var item = snapshot.toObject(ContentDTO::class.java) //contentDTO에 담아옴
                    contentDTOs.add(item!!)
                    contentUIDList.add(snapshot.id)
                }
                notifyDataSetChanged() //값이 새로고침되도록함
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //서버에서 나온 부분 매핑시켜줌
            var viewholder = (holder as CustomViewHolder).itemView

            //userid
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUri).into(viewholder.detailviewitem_imageview)

            //explain
            viewholder.detailviewitem_explain_tv.text = contentDTOs!![position].explain

            //likes
            viewholder.detailviewitem_favoritecounter_tv.text = "Likes "+ contentDTOs!![position].favoriteCount

            //profileImage
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUri).into(viewholder.detailviewitem_profile_image)

            //This code is when the button is clicked
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }

            //This code is when the page is loaded
            if(contentDTOs!![position].favorites.containsKey(uid)) {
                //좋아요 버튼 클릭한 경우
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }else{
                //좋아요 버튼 없애는 경우
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)

            }
            //프로필 이미지 클릭했을떄
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        //좋아요 버튼 이벤트
        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUIDList[position]) //내가 선택한 컨텐츠 받아서 거기에좋아요 할 수 있는 이벤트 만듬
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    //좋아요 버튼 클릭되어 경우 -> 버튼 누르면 좋아요 취소해야함
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid) //uid제거해줌
                }else{
                    //버튼 클릭이 안된경우
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                }
                transaction.set(tsDoc,contentDTO) //이 트랜잭션을 다시 서버로 돌려줌
            }
        }
    }

}