package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import android.graphics.Insets.add
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.earlybird.catchbird.R
import com.earlybird.catchbird.Rank
import com.earlybird.catchbird.Regist
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdRegistBinding
import com.earlybird.catchbird.databinding.ActivityEncyclopediaRankingBinding
import com.earlybird.catchbird.map.MapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_encyclopedia_bird_infor.view.*
import kotlinx.android.synthetic.main.item_classification.view.*


class EncyclopediaBirdRegistActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdRegistBinding by lazy {
        ActivityEncyclopediaBirdRegistBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var otherUid: String? = null
    var birdKor: String? = null
    var firestore: FirebaseFirestore? = null
    private var currentUserUid : String? = null
    val data = BirdImageList.data
    val registImageData = arrayListOf<Regist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        otherUid = intent.getStringExtra("otherUid")
        birdKor = intent.getStringExtra("birdKor")
        binding.encyclopediaBtnOk.setOnClickListener {
            finish()
        }
        binding.encyclopediaTitle.text = birdKor+" 등록 사진"
        val db = Firebase.firestore
        if(otherUid == "null"){
            db.collection("birdImageData").document(currentUserUid.toString()).collection("imageInfo")
                .get()//todo list도 만들어서 새 설명창에 버튼누르면 찍은 사진 출력되게 하기
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (birdKor.equals(document.data["bird"].toString())) {
                            var image = document.data["imageUri"]
                            registImageData.add(
                                Regist(
                                    image.toString(),
                                    document.data["date"].toString(),
                                    document.data["time"].toString()
                                )
                            )

                        }
                    }
                    if(registImageData.size==0){
                        binding.textView2.visibility = View.VISIBLE
                        binding.recyclerviewRegist.visibility = View.INVISIBLE
                    }else{
                        binding.textView2.visibility = View.INVISIBLE
                        binding.recyclerviewRegist.visibility = View.VISIBLE
                        binding.recyclerviewRegist.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
                        binding.recyclerviewRegist.adapter = MyAdapter(registImageData)
                    }

                }

        }else {
            db.collection("birdImageData").document(otherUid.toString()).collection("imageInfo")
                .get()//todo list도 만들어서 새 설명창에 버튼누르면 찍은 사진 출력되게 하기
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (birdKor.equals(document.data["bird"].toString())) {
                            var image = document.data["imageUri"]
                            registImageData.add(
                                Regist(
                                    image.toString(),
                                    document.data["date"].toString(),
                                    document.data["time"].toString()
                                )
                            )

                        }
                    }
                    if(registImageData.size==0){
                        binding.textView2.visibility = View.VISIBLE
                        binding.recyclerviewRegist.visibility = View.INVISIBLE
                    }else{
                        binding.textView2.visibility = View.INVISIBLE
                        binding.recyclerviewRegist.visibility = View.VISIBLE
                        binding.recyclerviewRegist.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
                        binding.recyclerviewRegist.adapter = MyAdapter(registImageData)
                    }
                }

        }
            
    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private lateinit var regist: Regist
        private var registImage: ImageView = itemView.findViewById(R.id.encyclopedia_regist_bird_image)
        private var registDate: TextView = itemView.findViewById(R.id.encyclopedia_regist_bird_date)
        private var registTime: TextView = itemView.findViewById(R.id.encyclopedia_regist_bird_time)

        fun bind(regist: Regist){
            this.regist=regist
            Glide.with(itemView.context).load(regist.image).centerCrop().into(registImage)
            registDate.text = this.regist.date
            registTime.text = this.regist.time


            
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