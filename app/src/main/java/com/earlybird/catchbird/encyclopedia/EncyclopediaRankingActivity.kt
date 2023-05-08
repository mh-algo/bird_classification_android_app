package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private var dummy = ArrayList<Rank>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.encyclopediaBtnRankingpageOk.setOnClickListener {
           finish()
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid

        dummy.apply {
            add(
                Rank(1,R.drawable.dummy_bird,"aaaaaaaaa", 5000)
            )
            add(
                Rank(2, R.drawable.dummy_bird,"bbbbbbbb", 4000)
            )
            add(
                Rank(3,R.drawable.dummy_bird,"ccccdc", 3400)
            )
            add(
                Rank(4, R.drawable.dummy_bird,"dddddd", 3000)
            )
            add(
                Rank(5, R.drawable.dummy_bird,"eeeeeee", 2700)
            )
            add(
                Rank(6, R.drawable.dummy_bird,"fffffff", 2000)
            )
            add(
                Rank(7,R.drawable.dummy_bird,"gggggg", 1000)
            )

        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = MyAdapter(dummy)
    }
        inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
            private lateinit var rank: Rank
            private var userRank: TextView = itemView.findViewById(R.id.encyclopedia_rank)
            private var userProfileImage: ImageView = itemView.findViewById(R.id.encyclopedia_profileImage)
            private var userId: TextView = itemView.findViewById(R.id.encyclopedia_user_id)
            private var userScore: TextView = itemView.findViewById(R.id.encyclopedia_score)

            fun bind(rank: Rank){
                this.rank=rank
                userRank.text = this.rank.rank.toString()
                //Glide.with(itemView.context).load(R.drawable.dummy_bird).centerCrop().into(userProfileImage)
                userId.text = this.rank.userId
                userScore.text = this.rank.score.toString()
                userProfileImage.clipToOutline = true

                itemView.setOnClickListener{
                    val intent = Intent(this@EncyclopediaRankingActivity, EncyclopediaOtherRankingPage::class.java)
                    intent.putExtra("otherUid", otherUid)
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