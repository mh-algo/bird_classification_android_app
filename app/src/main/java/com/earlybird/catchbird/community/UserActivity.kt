package com.earlybird.catchbird.community

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.databinding.ActivityCommunityUserBinding
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
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
import com.earlybird.catchbird.Rank
import com.earlybird.catchbird.community.model.AlarmDTO
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.community.model.FollowDTO
import com.earlybird.catchbird.community.model.ProfileDTO
import com.earlybird.catchbird.encyclopedia.EncyclopediaOtherRankingPage
import com.earlybird.catchbird.encyclopedia.EncyclopediaRankingActivity
//import com.earlybird.catchbird.community.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    // Ranking
    var rank = arrayListOf<Rank>()
    var rankUid = arrayListOf<String>()
    var rankProfileImage = arrayListOf<String>()
    var rankNickName = arrayListOf<String>()
    var flag = 0

    //private String destinationUid;
    var uid: String? = null
    var currentUserUid: String? = null
    var nickname: String? = null
    var userView: View? = null

    //var fcmPush: FcmPush? = null


    var followListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null
    var imageprofileListenerRegistration: ListenerRegistration? = null
    var recyclerListenerRegistration: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //fcmPush = FcmPush()

        currentUserUid = auth?.currentUser?.uid


            uid = intent.getStringExtra("destinationUid")
            nickname = intent.getStringExtra("nickname")
            binding.unameText.text = nickname


            firestore?.collection("profileImages")?.document(uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    binding.unameText.text = task.result["nickname"].toString()
                    }
            }


            // 순위 정보
             FirebaseFirestore.getInstance().collection("rank")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("test","documents${documents.size()}")
                for(document in documents){
                    rankUid.add(document.data["uid"].toString())
                    Log.d("test","uid${document.data["uid"].toString()}")
                }
                getUserInfo()
                Log.d("test", "랭킹이미지, 닉네임${rankProfileImage} ,${rankNickName}")
                setRank()
            }




            // 도감 플로팅 버튼
            binding.collectionButton.setOnClickListener {
                // 해당 uid를 가진 유저의 도감 정보 조회
                val intent = Intent(this, EncyclopediaOtherRankingPage::class.java)
                intent.putExtra("otherUid", uid)
                startActivity(intent)
            }

            // 순위 정보 클릭 시
            binding.accountRankCount.setOnClickListener {
                val intent = Intent(this, EncyclopediaRankingActivity::class.java)
                intent.putExtra("otherUid", uid)
                startActivity(intent)
            }

            // 본인 계정인 경우 -> 로그아웃, Toolbar 기본으로 설정
            if (uid != null && uid == currentUserUid) {
                binding?.accountBtnFollowSignout?.setBackgroundResource(R.drawable.ic_baseline_logout_24)
                binding?.btnText?.visibility = View.GONE
                binding?.btnText3?.visibility = View.VISIBLE
                binding?.btnText?.setBackgroundColor(0x000000)
                binding?.btnText3?.setOnClickListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    this?.finish()
                    auth?.signOut()
                }
                binding?.accountBtnFollowSignout?.setOnClickListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    this?.finish()
                    auth?.signOut()
                }

            } else {
                binding?.accountBtnFollowSignout?.setBackgroundResource(R.drawable.ic_baseline_person_add_24)
                binding?.accountBtnFollowSignout.setOnClickListener{ requestFollow() }
                binding?.btnText?.setOnClickListener { requestFollow() }
                binding?.btnText2?.setOnClickListener { requestFollow() }

                 }

                userView?.account_btn_follow_signout?.setOnClickListener {
                    requestFollow()
                }





        // Profile Image Click Listener
        binding?.accountIvProfile?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //main인지 유저 액티비티인지?

                //본인의 프로필일 때만 앨범 오픈
                if (uid == currentUserUid) {
                    var photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    this!!.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
                    }

            }
        }

        getFollowing()
        getFollower()

        if (flag == 0) {binding.accountRankCount.text = "권외"}

        setContentView(binding.root)
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
                binding?.accountBtnFollowSignout?.setBackgroundResource(R.drawable.ic_baseline_person_add_disabled_24)
                binding?.btnText?.visibility = View.GONE
                binding?.btnText2?.visibility = View.VISIBLE
                binding?.btnText?.text = "팔로우 해제"
            } else {

                if (uid != currentUserUid) {
                    binding?.btnText?.text = "팔로우"
                    binding?.btnText2?.visibility = View.GONE
                    binding?.btnText?.visibility = View.VISIBLE
                    binding?.accountBtnFollowSignout?.setBackgroundResource(R.drawable.ic_baseline_person_add_24)
                }
            }

        }

    }

    private fun getUserInfo() {
        for (rankUids in rankUid) {
            FirebaseFirestore.getInstance().collection("profileImages").document(rankUids)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    Log.d("test getUserInfo()", "rankUids${rankUids}")
                    if (documentSnapshot?.data != null) {
                        Log.d("test getUserInfo()", "documentSnapshot?.data${documentSnapshot?.data}")
                        rankProfileImage.add(documentSnapshot?.data!!["image"].toString())
                        rankNickName.add(documentSnapshot?.data!!["nickname"].toString())
                    }
                }

        }

    }

    private fun setRank() {
        var pos = 0
        var i = 0
        FirebaseFirestore.getInstance().collection("rank")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var score = document.data["score"].toString().toInt()

                    if (rank != null && rank.size != 0) {
                        rank.add(
                            Rank(
                                i,
                                rankProfileImage[i],
                                rankNickName[i],
                                score,
                                rankUid[i]
                            )
                        )
                    }


                    if (rankUid[i] == uid) {
                        pos = i
                        flag = 1
                        binding.accountRankCount.text = (pos+1).toString() + "위"

                    }
                    i += 1
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
        val contentUidList: ArrayList<String>

        init {
            contentDTOs = ArrayList()
            contentUidList = ArrayList()
            // 나의 사진만 찾기
            recyclerListenerRegistration = firestore?.collection("image")?.orderBy("timestamp", Query.Direction.DESCENDING)?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot?.documents!!) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    contentUidList.add(snapshot.id)
                }
                Log.e(TAG, contentDTOs.toString())
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

            holder.imageView.setOnClickListener {
                val intent = Intent(this@UserActivity, CommentsActivity::class.java)
                Log.d(TAG, contentDTOs.toString())
                intent.putExtra("destinationUid", uid)
                intent.putExtra("nickname", nickname)
                intent.putExtra("imageUrl", contentDTOs[position].imageUrl)
                intent.putExtra("explain", contentDTOs[position].explain)
                intent.putExtra("contentDTO", contentDTOs[position])
                intent.putExtra("contentUid", contentUidList[position])
                startActivity(intent)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 앨범에서 Profile Image 사진 선택시 호출 되는 부분
        if (requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {

            var imageUri = data?.data
            binding.accountIvProfile.setImageURI(imageUri)
            val uid = FirebaseAuth.getInstance().currentUser!!.uid //파일 업로드
            //사진을 업로드 하는 부분  userProfileImages 폴더에 uid에 파일을 업로드함

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("userProfileImages/")



            storageRef.putFile(imageUri!!).continueWithTask(){
                return@continueWithTask  storageRef.downloadUrl
            }.addOnCompleteListener { uri ->
                //firestore?.collection("profileImages")?.document(uid)!!.get()

                firestore?.collection("profileImages")?.document(uid)!!.update("image", uri.result.toString())
                Toast.makeText(this,
                    "프로필 사진이 변경되었습니다!", Toast.LENGTH_SHORT).show()
                //var profileDTO = ProfileDTO("example", ,uid)
                //firestore?.collection("profileImages")?.document(uid)!!.set(profileDTO)
            }

            /*
            FirebaseStorage
                .getInstance()
                .reference
                .child("userProfileImages")
                .child(uid)
                .putFile(imageUri!!)
                .addOnCompleteListener { task ->
                    val url = task.result.storage.downloadUrl.toString()
                    FirebaseFirestore.getInstance().collection("profileImages").document(uid).update("image",url)


                }*/
        }

    }



}

