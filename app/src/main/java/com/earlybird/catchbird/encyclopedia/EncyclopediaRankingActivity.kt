package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.earlybird.catchbird.R
import com.earlybird.catchbird.Rank
import com.earlybird.catchbird.databinding.ActivityEncyclopediaRankingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.item_classification.view.*

class EncyclopediaRankingActivity : AppCompatActivity() {
    // todo firebase에 있는 모든 유저의 새 등록 정보를 가져와 갯수 별로 점수 부여 후 점수와 순위를 매김
    // todo 새 사진이 등록되어 있지 않다면 순위에서 표시하지 않음
    private val binding: ActivityEncyclopediaRankingBinding by lazy {
        ActivityEncyclopediaRankingBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    private var currentUserUid : String? = null
    var otherUid: String? = null
    var rank = arrayListOf<Rank>()
    var rankUid = arrayListOf<String>()
    var rankProfileImage = arrayListOf<String>()
    var rankNickName = arrayListOf<String>()
    lateinit var db:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.encyclopediaBtnRankingpageOk.setOnClickListener {
           finish()
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        db = Firebase.firestore
        db.collection("rank")
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



    }

    private fun getUserInfo() {
        for (rankUids in rankUid) {
            db.collection("profileImages").document(rankUids)
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
    private fun setRank(){
        var i = 0
        db.collection("rank")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener{documents ->
                Log.d("test setRank()","documents${documents.size()}")

                for(document in documents){
                    var score = document.data["score"].toString().toInt()
                    rank.add(Rank(i+1,rankProfileImage[i],rankNickName[i], score,rankUid.get(i)))
                    Log.d("test setRank()","document for문 - rank${rank}")
                    i += 1
                    Log.d("test setRank()","${i}, ${rankProfileImage.size}. ${rankNickName.size}, ${rankUid.size}")
                }
                Log.d("test setRank()"," i= ${i} , ${rank.size-1}")
                Log.d("test setRank()","i${i}")
                Log.d("test setRank()","if문${rank}")
                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = MyAdapter(rank)
            }

    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
            private lateinit var rank: Rank
            private var userRank: TextView = itemView.findViewById(R.id.encyclopedia_rank)
            private var userProfileImage: ImageView = itemView.findViewById(R.id.encyclopedia_profileImage)
            private var userNickname: TextView = itemView.findViewById(R.id.encyclopedia_user_id)
            private var userScore: TextView = itemView.findViewById(R.id.encyclopedia_score)

            fun bind(rank: Rank){
                this.rank=rank
                userRank.text = this.rank.rank.toString()
                Glide.with(itemView.context).load(rank.profileImage).centerCrop().into(userProfileImage)
                userNickname.text = this.rank.userNickname
                userScore.text = this.rank.score.toString()
                userProfileImage.clipToOutline = true
                Log.d("test bind","리사이클러뷰${rank}")
                itemView.setOnClickListener{
                    val intent = Intent(this@EncyclopediaRankingActivity, EncyclopediaOtherRankingPage::class.java)
                    intent.putExtra("otherUid", rank.uid)
                    // todo 클릭시 해당 유저의 uid를 같이 넘김
                    startActivity(intent)
                }
            }

        }
        inner class MyAdapter(private val list:List<Rank>): RecyclerView.Adapter<MyViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val view=layoutInflater.inflate(R.layout.item_encyclopedia_ranking_lisk, parent, false)
                return MyViewHolder(view)
            }

            override fun getItemCount(): Int = list.size

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                val rank=list[position]
                holder.bind(rank)
            }
        }
    }