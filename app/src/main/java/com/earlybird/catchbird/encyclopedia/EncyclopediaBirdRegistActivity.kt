package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import android.graphics.Insets.add
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.earlybird.catchbird.R
import com.earlybird.catchbird.Rank
import com.earlybird.catchbird.Regist
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdRegistBinding
import com.earlybird.catchbird.databinding.ActivityEncyclopediaRankingBinding
import com.earlybird.catchbird.map.MapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_encyclopedia_bird_infor.view.*
import kotlinx.android.synthetic.main.item_classification.view.*


class EncyclopediaBirdRegistActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdRegistBinding by lazy {
        ActivityEncyclopediaBirdRegistBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var uid: String? = null
    var firestore: FirebaseFirestore? = null
    private var dummy = ArrayList<Regist>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        dummy.apply{
            add(
                Regist(R.drawable.dummy_bird,"2020-03-03","13/25"),
            )
            add(
                Regist(R.drawable.dummy_bird,"2020-02-03","13/45"),
            )
            add(
                Regist(R.drawable.dummy_bird,"2020-03-63","13/24"),
            )
            add(
                Regist(R.drawable.dummy_bird,"2020-03-03","13/25"),
            )
            add(
                Regist(R.drawable.dummy_bird,"2020-03-03","13/25"),
            )
            add(
                Regist(R.drawable.dummy_bird,"2020-03-03","13/25"),
            )
        }
        binding.recyclerviewRegist.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerviewRegist.adapter = MyAdapter(dummy)
    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private lateinit var regist: Regist
        private var registImage: ImageView = itemView.findViewById(R.id.encyclopedia_regist_bird_image)
        private var registDate: TextView = itemView.findViewById(R.id.encyclopedia_regist_bird_date)
        private var registTime: TextView = itemView.findViewById(R.id.encyclopedia_regist_bird_time)

        fun bind(regist: Regist){
            this.regist=regist
            registImage.setBackgroundResource(R.drawable.dummy_bird)
            registDate.text = this.regist.date
            registTime.text = this.regist.time


            itemView.setOnClickListener{
                val intent = Intent(this@EncyclopediaBirdRegistActivity, MapFragment::class.java)

                startActivity(intent)
            }
        }

    }
    inner class MyAdapter(private val list:List<Regist>): RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view=layoutInflater.inflate(R.layout.item_encyclopedia_bird_regist_list, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val regist=list[position]
            holder.bind(regist)
        }
    }
}