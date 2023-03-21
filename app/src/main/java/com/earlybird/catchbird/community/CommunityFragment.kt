package com.earlybird.catchbird.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.earlybird.catchbird.R
import com.earlybird.catchbird.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class CommunityFragment : Fragment() {


var user: FirebaseUser? = null
var firestore: FirebaseFirestore? = null
var imagesSnapshot: ListenerRegistration? = null
var mainView: View? = null

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {

    user = FirebaseAuth.getInstance().currentUser
    firestore = FirebaseFirestore.getInstance()

    //RecycleView와 어댑터를 연결
    mainView = inflater.inflate(R.layout.fragment_community, container, false)

    return mainView
}

override fun onResume() {
    super.onResume()
    //mainView?.communityfragment_recyclerview?.layoutManager = LinearLayoutManager(activity)
    //mainView?.communityfragment_recyclerview?.adapter = DetailRecyclerViewAdapter()
    //var mainActivity = activity as MainActivity
    //mainActivity.progress_bar.visibility = View.INVISIBLE
}

override fun onStop(){
    super.onStop()
    imagesSnapshot?.remove()
}

inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    val contentDTOs: ArrayList<ContentDTO>
    val contentUidList: ArrayList<String>

    init {
        contentDTOs = ArrayList()
        contentUidList = ArrayList()
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        firestore?.collection("users")?.document(uid!!)?.get()?.addOnCompleteListener {task ->
            if (task.isSuccessful) {
                /*
                var userDTO = task.result.toObject(FollowDTO::class.java)
                if (userDTO?.followings != null) {
                    getCotents(userDTO?.followings)
                } */
            }
        }
    }

    fun getCotents(followers: MutableMap<String, Boolean>?) {
        imagesSnapshot = firestore?.collection("images")?.orderBy("timestamp")?.
        addSnapshotListener { querySnapShot, firebaseFirestoreException ->
            contentDTOs.clear()
            contentUidList.clear()
            if(querySnapShot == null) return@addSnapshotListener
            for (snapshot in querySnapShot!!.documents) {
                var item = snapshot.toObject(ContentDTO::class.java)!!
                println(item.uid)
                if (followers?.keys?.contains(item.uid)!!) {
                    contentDTOs.add(item)
                    contentUidList.add(snapshot.id)
                }
            }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_community, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = (holder as CustomViewHolder).itemView

        //프로필 이미지 가져오기
        firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
            ?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var url = task.result["image"]
                    /*
                    Glide.with(holder.itemView.context)
                        .load(url)
                        .apply(
                            RequestOptions()
                            .circleCrop())
                        .into(viewHolder
                            .detailview_profile_image)

                     */
                }
            }

        //User Fragment로 이동
        // ...

        //유저 아이디
        //viewHolder.detailviewitem_profile_textview.text = contentDTOs[position]

        //가운데 이미지
        /*
        Glide.with(holder.itemView.context)
            .load(contentDTOs[position].imageUrl)
            .into(viewHolder.detailviewitem_imageview_content)

        //설명 텍스트
        //viewHolder.detailviewitem_explain_textview.text = contentDTOs[position].explain

        //좋아요 이벤트
        viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
            favoriteEvent(position)
        } */

        //좋아요 버튼 설정
        /*
        if (contentDTOs[position].favorite.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {
            viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
        } else {
            viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
        }
        //좋아요 카운터 설정
        viewHolder.detailviewitem_favoritecounter_textview.text = "좋아요" + contentDTOs[position].favoriteCount+"개"
        */
    }

    override fun getItemCount(): Int {
        return contentDTOs.size
    }

    //좋아요 이벤트 기능
    private fun favoriteEvent(position: Int) {
        var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
        firestore?.runTransaction { transaction ->
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

            /*
            if(contentDTO!!.favorites.containsKey(uid)) {
                // Unstar the post and remove self from stars
                contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                contentDTO?.favorites.remove(uid)
            } else {
                // Star the post and add self to stars
                contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                contentDTO?.favorites[uid] = true
            }
            transaction.set(tsDoc, contentDTO)

             */
        }
    }
}

inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)



}

