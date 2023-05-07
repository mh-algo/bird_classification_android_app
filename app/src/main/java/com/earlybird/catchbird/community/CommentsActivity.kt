package com.earlybird.catchbird.community


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.earlybird.catchbird.R
import com.earlybird.catchbird.R.id.*
import com.earlybird.catchbird.community.model.AlarmDTO
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.community.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_community_comments.*
import kotlinx.android.synthetic.main.item_comments.view.*
import kotlinx.android.synthetic.main.item_community.view.*
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : AppCompatActivity() {

    var contentUid: String? = null
    var user: FirebaseUser? = null
    var destinationUid: String? = null
    var commentSnapshot: ListenerRegistration? = null
    var nickname: String? = null
    var imageUrl: String? = null
    var explain: String? = null
    var contentDTO: ContentDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_comments)

        user = FirebaseAuth.getInstance().currentUser
        destinationUid = intent.getStringExtra("destinationUid")
        contentUid = intent.getStringExtra("contentUid")
        nickname = intent.getStringExtra("nickname")
        imageUrl = intent.getStringExtra("imageUrl")
        explain = intent.getStringExtra("explain")
        contentDTO = intent.getSerializableExtra("contentDTO") as ContentDTO

        cancel_button2.setOnClickListener {
            finish()
        }
        detailviewitem_favorite_imageview.setOnClickListener { favoriteEvent() }
        comment_btn_send.setOnClickListener {
            val comment = ContentDTO.Comment()

            comment.userId = FirebaseAuth.getInstance().currentUser!!.email
            comment.comment = comment_edit_message.text.toString()
            comment.uid = FirebaseAuth.getInstance().currentUser!!.uid
            comment.timestamp = System.currentTimeMillis()
            comment.nickname = nickname

            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .document()
                .set(comment)

            //commentAlarm(destinationUid!!, comment_edit_message.text.toString())
            comment_edit_message.setText("")

        }

        // 글 프로필과 내용 이미지
        // Profile Image
        FirebaseFirestore.getInstance()
            .collection("profileImages")
            .document(destinationUid!!)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot?.data != null) {
                    val url = documentSnapshot?.data!!["image"]
                    Glide.with(this)
                        .load(url)
                        .apply(RequestOptions().circleCrop()).into(detailviewitem_profile_image)
                }
            }
        detailviewitem_profile_image.setOnClickListener {
            val user_intent = Intent(this, UserActivity::class.java)
            user_intent.putExtra("destinationUid", destinationUid)
            user_intent.putExtra("nickname", nickname)
            startActivity(user_intent)
        }

        Glide.with(this)
            .load(imageUrl)
            .into(detailviewitem_imageview_content)
        // 닉네임 및 설명 텍스트
        detailviewitem_profile_textview.text = nickname
        detailviewitem_explain_textview.text = explain

        // 팔로잉 아이콘
        FirebaseFirestore.getInstance().collection("users")?.document(FirebaseAuth.getInstance().currentUser!!.uid!!)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var userDTO = task.result.toObject(FollowDTO::class.java)
                if (userDTO?.followings != null && userDTO.followings.containsKey(contentDTO!!.uid)) {
                    detailviewitem_following.visibility = View.VISIBLE
                }
            }
        }

        // 게시날짜 표시
        val relativeTime = getRelativeTime(contentDTO!!.timestamp!!)
        detailviewitem_date.text = relativeTime


        // 좋아요 이벤트
        if (contentDTO!!.favorites.containsKey(FirebaseAuth.getInstance().currentUser?.uid)) {
            detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
        }
        textView4.text = contentDTO!!.favoriteCount.toString()

        comment_recyclerview.adapter = CommentRecyclerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

    }



    //좋아요 이벤트 기능

    private fun favoriteEvent() {
        var firestore = FirebaseFirestore.getInstance()
        var tsDoc = firestore?.collection("image")?.document(contentUid!!)
        firestore.runTransaction { transaction ->

            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val contentDTO_comp = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

            if (contentDTO_comp!!.favorites.containsKey(uid)) {
                // Unstar the post and remove self from stars
                contentDTO_comp?.favoriteCount = contentDTO_comp?.favoriteCount!! - 1
                contentDTO_comp?.favorites.remove(uid)
                detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)

            } else {
                contentDTO_comp.favoriteCount = contentDTO_comp?.favoriteCount!! + 1
                contentDTO_comp.favorites[uid] = true
                detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_baseline_favorite_24)
                //favoriteAlarm(contentDTOs[position].uid!!)
            }
            transaction.set(tsDoc, contentDTO_comp)
            textView4.text = contentDTO_comp.favoriteCount.toString()

        }



    }

    // 상대 날짜 계산
    fun timestampToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getRelativeTime(timestamp: Long): String {
        val currentDate = Date()
        val diffInMilliseconds = currentDate.time - timestamp

        val secondsInMilli = 1000L
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val monthsInMilli = daysInMilli * 30

        val elapsedMonths = diffInMilliseconds / monthsInMilli
        val elapsedDays = diffInMilliseconds / daysInMilli
        val elapsedHours = diffInMilliseconds / hoursInMilli
        val elapsedMinutes = diffInMilliseconds / minutesInMilli
        val elapsedSeconds = diffInMilliseconds / secondsInMilli

        return when {
            elapsedMonths > 0 -> "$elapsedMonths"+"달 전"
            elapsedDays > 0 -> "$elapsedDays"+"일 전"
            elapsedHours > 0 -> "$elapsedHours"+"시간 전"
            elapsedMinutes > 0 -> "$elapsedMinutes"+"분 전"
            else -> "방금"
        }
    }


    override fun onStop() {
        super.onStop()
        commentSnapshot?.remove()
    }




    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val comments: ArrayList<ContentDTO.Comment>

        init {
            comments = ArrayList()
            commentSnapshot = FirebaseFirestore
                .getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot?.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()

                }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comments, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var view = holder.itemView

            // Profile Image
            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot?.data != null) {
                        val uname = documentSnapshot?.data!!["nickname"].toString()
                        view.commentitem_textview_profile.text = uname

                        val url = documentSnapshot?.data!!["image"]
                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(view.commentitem_imageview_profile)
                    }
                }


            view.commentitem_textview_comment.text = comments[position].comment
        }

        override fun getItemCount(): Int {

            return comments.size
        }

        private inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

}