package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.earlybird.catchbird.R
import com.earlybird.catchbird.Rank
import com.earlybird.catchbird.databinding.ActivityEncyclopediaRankingBinding

class EncyclopediaRankingActivity : AppCompatActivity() {

    private val binding: ActivityEncyclopediaRankingBinding by lazy {
        ActivityEncyclopediaRankingBinding.inflate(layoutInflater)
    }
    private var dummy = ArrayList<Rank>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.encyclopediaBtnRankingpageOk.setOnClickListener {
           finish()
        }
        dummy.apply {
            add(
                Rank(1, "a", 5000)
            )
            add(
                Rank(2, "b", 4000)
            )
            add(
                Rank(3, "c", 3400)
            )
            add(
                Rank(4, "d", 3000)
            )
            add(
                Rank(5, "e", 2700)
            )
            add(
                Rank(6, "f", 2000)
            )
            add(
                Rank(7, "g", 1000)
            )

        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = MyAdapter(dummy)
    }
        inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
            private lateinit var rank: Rank
            private var userRank: TextView = itemView.findViewById(R.id.encyclopedia_rank)
            private var userId: TextView = itemView.findViewById(R.id.encyclopedia_user_id)
            private var userScore: TextView = itemView.findViewById(R.id.encyclopedia_score)

            fun bind(rank: Rank){
                this.rank=rank
                userRank.text = this.rank.rank.toString()
                userId.text = this.rank.userId
                userScore.text = this.rank.score.toString()


                itemView.setOnClickListener{
                    // todo list클릭시 해당유저 새 정보데이터 출력화면
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