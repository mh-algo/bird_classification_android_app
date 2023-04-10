package com.earlybird.catchbird.community

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.databinding.ActivityCommunityUserBinding
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.earlybird.catchbird.community.LoginActivity
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.R.id.account_tv_post_count
import com.earlybird.catchbird.community.model.AlarmDTO
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.community.model.FollowDTO
import com.earlybird.catchbird.community.model.ProfileDTO
//import com.earlybird.catchbird.community.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_community_user.*
import kotlinx.android.synthetic.main.activity_community_user.view.*
import java.util.ArrayList

class UserActivity : AppCompatActivity() {
    private val binding: ActivityCommunityUserBinding by lazy {
        ActivityCommunityUserBinding.inflate(layoutInflater)
    } //activity_community_user.xml

    val PICK_PROFILE_FROM_ALBUM = 10

    // Firebase
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    //private String destinationUid;
    var uid: String? = null
    var currentUserUid: String? = null

    var userView: View? = null

    //var fcmPush: FcmPush? = null


    var followListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null
    var imageprofileListenerRegistration: ListenerRegistration? = null
    var recyclerListenerRegistration: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val arguments = Bundle()
        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //fcmPush = FcmPush()

        currentUserUid = auth?.currentUser?.uid


            uid = intent.getStringExtra("destinationUid")

            // 본인 계정인 경우 -> 로그아웃, Toolbar 기본으로 설정
            if (uid != null && uid == currentUserUid) {
                Toast.makeText(this,
                    "It's your profile!", Toast.LENGTH_LONG).show()

                binding?.accountBtnFollowSignout?.text = getString(R.string.signout)
                binding?.accountBtnFollowSignout?.setOnClickListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    this?.finish()
                    auth?.signOut()
                }
            } else {
                binding?.accountBtnFollowSignout?.text = getString(R.string.follow)
                binding?.accountBtnFollowSignout.setOnClickListener{ requestFollow() }

                 }

                userView?.account_btn_follow_signout?.setOnClickListener {
                    requestFollow()
                }





        // Profile Image Click Listener
        binding?.accountIvProfile?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //main인지 유저 액티비티인지?

                //앨범 오픈
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                this!!.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
            }
        }

        getFollowing()
        getFollower()
        binding?.accountRecyclerview?.layoutManager = GridLayoutManager(this!!, 3)
        binding?.accountRecyclerview?.adapter = UserFragmentRecyclerViewAdapter()



    }

    fun getProfileImage() {
        uid = intent.getStringExtra("destinationUid")
        imageprofileListenerRegistration = firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot?.getString("image") != null) {
                    val url = documentSnapshot.getString("image")!!

                    Glide.with(this)
                        .load(url)
                        .apply(RequestOptions().circleCrop()).into(binding!!.accountIvProfile)
                }
            }

    }


    fun getFollowing() {
        followingListenerRegistration = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            binding?.accountTvFollowingCount?.text = followDTO?.followingCount.toString()
        }
    }


    fun getFollower() {

        followListenerRegistration = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            binding?.accountTvFollowerCount?.text = followDTO?.followerCount.toString()
            if (followDTO?.followers?.containsKey(currentUserUid)!!) {

                binding?.accountBtnFollowSignout?.text = getString(R.string.follow_cancel)
                binding?.accountBtnFollowSignout
                    ?.background
                    ?.setColorFilter(ContextCompat.getColor(this!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
            } else {

                if (uid != currentUserUid) {

                    binding?.accountBtnFollowSignout?.text = getString(R.string.follow)
                    binding?.accountBtnFollowSignout?.background?.colorFilter = null
                }
            }

        }

    }


    fun requestFollow() {


        var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!)
        firestore?.runTransaction { transaction ->

            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if (followDTO == null) {

                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction

            }
            // Unstar the post and remove self from stars
            if (followDTO?.followings?.containsKey(uid)!!) {

                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings.remove(uid)
            } else {

                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followings[uid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore!!.collection("users").document(uid!!)
        firestore?.runTransaction { transaction ->

            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)
            if (followDTO == null) {

                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if (followDTO?.followers?.containsKey(currentUserUid!!)!!) {
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            } else {
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
            }// Star the post and add self to stars

            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }

    }

    fun followerAlarm(destinationUid: String) {

        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser!!.email
        alarmDTO.uid = auth?.currentUser!!.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()

        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        //var message = auth?.currentUser!!.email + getString(R.string.alarm_follow)
        //fcmPush?.sendMessage(destinationUid, "알림 메세지 입니다.", message)
    }

    override fun onResume() {
        super.onResume()
        getProfileImage()
    }


    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDTOs: ArrayList<ContentDTO>

        init {
            contentDTOs = ArrayList()
            // 나의 사진만 찾기
            recyclerListenerRegistration = firestore?.collection("image")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot?.documents!!) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                account_tv_post_count.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3
            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop())
                .into(imageview)
        }

        override fun getItemCount(): Int {

            return contentDTOs.size
        }

        // RecyclerView Adapter - View Holder
        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }

    override fun onStop() {
        super.onStop()
        followListenerRegistration?.remove()
        followingListenerRegistration?.remove()
        imageprofileListenerRegistration?.remove()
        recyclerListenerRegistration?.remove()
    }



}

