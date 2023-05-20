package com.earlybird.catchbird.community


//import com.earlybird.catchbird.community.util.FcmPush
//import kotlin.collections.EmptyList.size
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.community.model.AlarmDTO
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.community.model.FollowDTO
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.home.CameraFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_community.*
import kotlinx.android.synthetic.main.fragment_community.view.*
import kotlinx.android.synthetic.main.item_community.view.*
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*


class CommunityFragment : Fragment() {

    val PICK_PROFILE_FROM_ALBUM = 10
    var user: FirebaseUser? = null
    var uid : String? = null
    var firestore: FirebaseFirestore? = null
    var imagesSnapshot: ListenerRegistration? = null
    var okHttpClient: OkHttpClient? = null
    //var fcmPush: FcmPush? = null
    var mainView: View? = null
    private lateinit var callback: OnBackPressedCallback




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        user = FirebaseAuth.getInstance().currentUser
        firestore = FirebaseFirestore.getInstance()
        okHttpClient = OkHttpClient()
        uid = FirebaseAuth.getInstance().currentUser?.uid



        if (user == null) {

            context?.let {
                AlertDialog.Builder(it)
                    .setTitle("로그인 필요")
                    .setMessage("커뮤니티를 이용하시려면 로그인이 필요합니다!")
                    .setPositiveButton("로그인", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            val intent = Intent(context, LoginActivity::class.java)
                            startActivity(intent)
                            //Log.d("MyTag", "positive")
                        }
                    })
                    .setNegativeButton("취소", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, CameraFragment()).commit()
                            //Log.d("MyTag", "negative")
                        }
                    })
                    .setNeutralButton("회원가입", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            val intent = Intent(context, SignupActivity::class.java)
                            startActivity(intent)
                            //Log.d("MyTag", "neutral")
                        }
                    })
                    .setCancelable(false) // 뒤로가기 사용불가
                    .create()
                    .show()
            }
        }

        //fcmPush = FcmPush()

        //푸시토큰 서버 등록
        //registerPushToken()


        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_community, container, false)


        view?.profile_btn?.setOnClickListener{
            uid = FirebaseAuth.getInstance().currentUser?.uid
            var uname: String? = null
            firestore?.collection("profileImages")?.document(uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uname = task.result["nickname"].toString()
                    }
                }
            val intent = Intent(context, UserActivity::class.java)
            intent.putExtra("nickname", uname)
            intent.putExtra("destinationUid", uid)
            startActivity(intent)
        }

        view?.write_button?.setOnClickListener{
            val intent = Intent(context, WriteActivity::class.java)
            startActivity(intent)
        }









        // Profile 버튼에 쓰일 프로필 이미지 가져오기
        if (uid != null)
        {
            firestore?.collection("profileImages")?.document(uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result["image"]
                        Glide.with(view.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(view.profile_btn)
                    }
                }
        }



        view.communityfragment_recyclerview.adapter = CommunityRecyclerViewAdapter()
        view.communityfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onResume(){
        super.onResume()
        CommunityRecyclerViewAdapter().notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        imagesSnapshot?.remove()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, CameraFragment()).commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    inner class CommunityRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDTOs: ArrayList<ContentDTO>
        val contentUidList: ArrayList<String>



        init {
            contentDTOs = ArrayList()
            contentUidList = ArrayList()
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid == null) {
            } else {
                firestore?.collection("users")?.document(uid!!)?.get()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var userDTO = task.result.toObject(FollowDTO::class.java)
                            if (userDTO?.followings != null) {
                                //getContents(userDTO?.followings)
                            }
                        }
                    }
            }


            imagesSnapshot = firestore?.collection("image")
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)!!
                        println(item.uid)
                        contentDTOs.add(item)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()

                }




        }






        fun getContents(followers: MutableMap<String, Boolean>?) {
            imagesSnapshot = firestore?.collection("image")?.orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //contentDTOs.clear()
                //contentUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)!!
                    if (followers?.keys?.contains(item.uid)!!) {
                        //contentDTOs.add(item)
                        //contentUidList.add(snapshot.id)
                    }
                }
                notifyDataSetChanged()
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community, parent, false)


            return CustomViewHolder(view)

        }



        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView



            // 칩 구현 - 전체 or 팔로잉
            view?.chip_following?.setOnCheckedChangeListener { chip, isChecked ->
                if (!isChecked) {

                    viewHolder.detailviewitem_following.visibility = View.GONE
                    contentDTOs.clear()
                    contentUidList.clear()
                    chip_following.text = "글 전체"
                    imagesSnapshot = firestore?.collection("image")
                        ?.orderBy("timestamp", Query.Direction.DESCENDING)
                        ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                            if (querySnapshot == null) return@addSnapshotListener

                            for (snapshot in querySnapshot!!.documents) {
                                var item = snapshot.toObject(ContentDTO::class.java)!!
                                println(item.uid)
                                contentDTOs.add(item)
                                contentUidList.add(snapshot.id)
                            }
                            notifyDataSetChanged()

                        }
                    notifyDataSetChanged()
                } else {
                    contentDTOs.clear()
                    contentUidList.clear()
                    chip_following.text = "팔로잉"
                    imagesSnapshot = firestore?.collection("image")
                        ?.orderBy("timestamp", Query.Direction.DESCENDING)
                        ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                            if (querySnapshot == null) return@addSnapshotListener

                            for (snapshot in querySnapshot!!.documents) {
                                var item = snapshot.toObject(ContentDTO::class.java)!!

                                firestore?.collection("users")
                                    ?.document(FirebaseAuth.getInstance().currentUser!!.uid!!)
                                    ?.get()?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            var userDTO =
                                                task.result.toObject(FollowDTO::class.java)
                                            if (userDTO?.followings!!.containsKey(item.uid)) {
                                                println(item.uid)
                                                contentDTOs.add(item)
                                                contentUidList.add(snapshot.id)
                                                notifyDataSetChanged()
                                            }
                                        }
                                    }
                            }
                            notifyDataSetChanged()
                        }
                }
            }

            // 검색
            view?.userSearchView?.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query.equals("")) {
                        contentDTOs.clear()
                        contentUidList.clear()
                        imagesSnapshot = firestore?.collection("image")
                            ?.orderBy("timestamp", Query.Direction.DESCENDING)
                            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                if (querySnapshot == null) return@addSnapshotListener

                                for (snapshot in querySnapshot!!.documents) {
                                    var item = snapshot.toObject(ContentDTO::class.java)!!

                                    println(item.uid)
                                    contentDTOs.add(item)
                                    contentUidList.add(snapshot.id)
                                }
                                notifyDataSetChanged()
                            }
                    }
                    if (query != null) {
                        contentDTOs.clear()
                        contentUidList.clear()
                        imagesSnapshot = firestore?.collection("image")
                            ?.whereEqualTo("explain", query)
                            ?.orderBy("timestamp", Query.Direction.DESCENDING)
                            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                if (querySnapshot == null) return@addSnapshotListener

                                for (snapshot in querySnapshot!!.documents) {
                                    var item = snapshot.toObject(ContentDTO::class.java)!!

                                    println(item.uid)
                                    contentDTOs.add(item)
                                    contentUidList.add(snapshot.id)
                                }
                                notifyDataSetChanged()
                            }
                        imagesSnapshot = firestore?.collection("image")
                            ?.whereEqualTo("nickname", query)
                            ?.orderBy("timestamp", Query.Direction.DESCENDING)
                            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                if (querySnapshot == null) return@addSnapshotListener

                                for (snapshot in querySnapshot!!.documents) {
                                    var item = snapshot.toObject(ContentDTO::class.java)!!

                                    println(item.uid)
                                    contentDTOs.add(item)
                                    contentUidList.add(snapshot.id)
                                }
                                notifyDataSetChanged()
                            }

                    }

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {

                    if (newText.equals("")) {
                        contentDTOs.clear()
                        contentUidList.clear()
                        this.onQueryTextSubmit("");
                    }
                    else if (newText != null || newText != "") {
                        contentDTOs.clear()
                        contentUidList.clear()
                        imagesSnapshot = firestore?.collection("image")
                            ?.whereEqualTo("explain",newText)
                            ?.orderBy("timestamp", Query.Direction.DESCENDING)
                            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                if (querySnapshot == null) return@addSnapshotListener

                                for (snapshot in querySnapshot!!.documents) {
                                    var item = snapshot.toObject(ContentDTO::class.java)!!

                                    println(item.uid)
                                    contentDTOs.add(item)
                                    contentUidList.add(snapshot.id)
                                }
                                notifyDataSetChanged()
                            }

                        imagesSnapshot = firestore?.collection("image")
                            ?.whereEqualTo("nickname", newText)
                            ?.orderBy("timestamp", Query.Direction.DESCENDING)
                            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                if (querySnapshot == null) return@addSnapshotListener

                                for (snapshot in querySnapshot!!.documents) {
                                    var item = snapshot.toObject(ContentDTO::class.java)!!

                                    println(item.uid)
                                    contentDTOs.add(item)
                                    contentUidList.add(snapshot.id)
                                }
                                notifyDataSetChanged()
                            }
                    }

                    return false
                }
            })








            // Profile Image 가져오기
            firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result["image"]
                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(viewHolder.detailviewitem_profile_image)
                    }
                }

            // 게시날짜 표시
            val relativeTime = getRelativeTime(contentDTOs[position].timestamp!!) // "just now", "1 minute ago", "5 days ago" 등의 상대적 시간 반환
            viewHolder.detailviewitem_date.text = relativeTime

            // 팔로잉 아이콘 표시
            if (uid != null) {
                firestore?.collection("users")?.document(FirebaseAuth.getInstance().currentUser!!.uid!!)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var userDTO = task.result.toObject(FollowDTO::class.java)
                        if (userDTO?.followings != null && userDTO.followings.containsKey(contentDTOs[position].uid)) {
                            viewHolder.detailviewitem_following.visibility = View.VISIBLE
                        }
                    }
                }
            }


            //UserActivity로 이동
            viewHolder.detailviewitem_profile_image.setOnClickListener {
                val user_intent = Intent(context, UserActivity::class.java)
                var uname = ""

                firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                    ?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user_intent.putExtra("nickname", task.result["nickname"].toString())
                        }
                    }
                user_intent.putExtra("destinationUid", contentDTOs[position].uid)
                user_intent.putExtra("userId", contentDTOs[position].userId)

                startActivity(user_intent)

            }


            // 유저 아이디
            //viewHolder.detailviewitem_profile_textview.text = contentDTOs[position].nickname
            firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewHolder.detailviewitem_profile_textview.text = task.result["nickname"].toString()
                    }
                }

            // 글 이미지
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)

            viewHolder.detailviewitem_imageview_content.setOnClickListener{
                val intent = Intent(activity, CommentsActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                    ?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            intent.putExtra("nickname", task.result["nickname"].toString())
                        }
                    }

                intent.putExtra("imageUrl", contentDTOs[position].imageUrl)
                intent.putExtra("explain", contentDTOs[position].explain)
                intent.putExtra("contentDTO", contentDTOs[position])
                startActivity(intent)
            }

            // 설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = contentDTOs[position].explain
            // 좋아요 이벤트
            viewHolder.detailviewitem_favorite_imageview.setOnClickListener { favoriteEvent(position) }

            //좋아요 버튼 설정
            if (contentDTOs[position].favorites.containsKey(FirebaseAuth.getInstance().currentUser?.uid)) {

                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_baseline_favorite_24)

            } else {

                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            //좋아요 카운터 설정
            viewHolder.detailviewitem_favoritecounter_textview.text = contentDTOs[position].favoriteCount.toString()

            // 코멘트 창으로 이동
            viewHolder.detailviewitem_comment_imageview.setOnClickListener {
                val intent = Intent(activity, CommentsActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                //intent.putExtra("nickname", contentDTOs[position].nickname)
                firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                    ?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            intent.putExtra("nickname", task.result["nickname"].toString())
                        }
                    }
                intent.putExtra("imageUrl", contentDTOs[position].imageUrl)
                intent.putExtra("explain", contentDTOs[position].explain)
                intent.putExtra("contentDTO", contentDTOs[position])
                startActivity(intent)
            }


        }


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



        fun favoriteAlarm(destinationUid: String) {

            val alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = user?.email
            alarmDTO.uid = user?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
            //var message = user?.email + getString(R.string.alarm_favorite)
            //fcmPush?.sendMessage(destinationUid, "알림 메세지 입니다.", message)
        }


        //좋아요 이벤트 기능

        private fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("image")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    // Unstar the post and remove self from stars
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites.remove(uid)

                } else {
                    // Star the post and add self to stars
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites[uid] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }


    }

    //커뮤니티
    /*
    fun registerPushToken(){
        var pushToken = FirebaseMessaging.getInstance().token
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var map = mutableMapOf<String,Any>()
        if (uid != null)
        {
            map["pushtoken"] = pushToken!!


            FirebaseFirestore.getInstance().collection("pushtokens").document(uid).also {

            try {
                    it.set(map)
                }
                catch (e: RuntimeException){
                    it.update(map)
                }

            }
        }
    } */


    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}




