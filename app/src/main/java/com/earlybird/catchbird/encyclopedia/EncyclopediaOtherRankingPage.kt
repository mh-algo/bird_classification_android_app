package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.earlybird.catchbird.Bird
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.ActivityEncyclopediaOtherRankingPageBinding
import com.earlybird.catchbird.databinding.ActivityEncyclopediaRankingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EncyclopediaOtherRankingPage : AppCompatActivity() {
    private val binding: ActivityEncyclopediaOtherRankingPageBinding by lazy {
        ActivityEncyclopediaOtherRankingPageBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var otherUid: String? = null
    var firestore: FirebaseFirestore? = null
    var currentUserUid: String? = null
    var spinnerList = BirdImageList.data  // 전체사진, 도감 등록된 사진 구별하기 위한 변수
    val data = BirdImageList.data
    var registDataKor = mutableSetOf<String>()
    var registDataAll = arrayListOf<BirdImageData>()
    val registImageData = arrayListOf<BirdImageData>()

    private val databaseName:String = "birdName"
    private var database: SQLiteDatabase? = null
    private val birdImage = "bird_image"
    private var type = "otherUserEncyclopedia"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (database == null){
            createDatabase()
        }
        loadAllImageData()

        binding.encyclopediaBtnOk.setOnClickListener {
            finish()
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid
        otherUid = intent.getStringExtra("otherUid")


        val db = Firebase.firestore

        db.collection("birdImageData").document(otherUid.toString()).collection("imageInfo")
            .get()//todo list도 만들어서 새 설명창에 버튼누르면 찍은 사진 출력되게 하기
            .addOnSuccessListener { documents ->
                for (document in documents){
                    registDataKor.add(document.data["bird"].toString())
                    var image = document.data["imageUri"]
                    registDataAll.add(BirdImageData(document.data["bird"].toString(),document.data["bird"].toString(),image.toString(),image.toString()))
                }
                for(datas in data){
                    for(registDataKors in registDataKor){
                        if(registDataKors == datas.birdKor)
                            registImageData.add(datas)
                    }
                }
                binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
                binding.recyclerView.adapter = MyAdapter(data)
            }

        db.collection("profileImages").document(otherUid.toString())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(documentSnapshot?.data != null){
                    binding.encyclopediaTitle.text = documentSnapshot?.data!!["nickname"].toString()+"님의 도감"
                }
            }
        fun BirdDataList(){
            binding.textView5.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.recyclerView.adapter = MyAdapter(data)
            spinnerList = BirdImageList.data
        }
        fun BirdRegistDataList(){
            if(registImageData.size == 0){
                binding.textView5.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.INVISIBLE
            }else{
                binding.textView5.visibility = View.INVISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
                binding.recyclerView.adapter = MyAdapter(registImageData)
                spinnerList = registImageData
            }
        }
        var sData = resources.getStringArray(R.array.sort)
        var adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,sData)
        MainActivity().loadAllImageData()
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // 전체사진, 도감등록사진 중 선택했을 때 어떤 코드 실행될지
                when(p2){
                    0 -> {
                        BirdDataList()
                    } // 전체사진
                    1 -> BirdRegistDataList() // firebase와 연동해서 유저 도감등록된 새 리스트만 출력
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //아무것도 선택하지 않으면 전체 화면
            }

        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                val search = arrayListOf<BirdImageData>()
                if (query.equals("")) {
                    binding.recyclerView.layoutManager = GridLayoutManager(applicationContext, 3)
                    binding.recyclerView.adapter = MyAdapter(spinnerList)
                }
                if (query != null) {
                    for (i in spinnerList) {
                        val isExist = i.birdKor!!.contains(query, ignoreCase = true)
                        if (isExist) {
                            search.add(i)
                        }
                    }
                    binding.recyclerView.layoutManager = GridLayoutManager(applicationContext, 3)
                    binding.recyclerView.adapter = MyAdapter(search)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                // 검색창에서 글자가 변경이 일어날 때마다 호출
                // onQueryTextSubmit의  query에는 빈값이나 null을 받아들이지 않는다.
                // 그래서 텍스트 입력 변경시 공백인 경우 다시 onQueryTextSubmit 를 호출하면서 인자로 빈공백을 넣어준다.
                if(newText.equals("")){
                    this.onQueryTextSubmit("");
                }

                return false
            }
        })
    }
    override fun onStart() {
        super.onStart()

        //자동 로그인 설정
        moveMainPage(auth?.currentUser)
    }
    fun moveMainPage(user: FirebaseUser?) {
        //User is Signed in
        if (user != null) {

            //startActivity(Intent(this, MainActivity::class.java))
        }
    }
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private var name: TextView = itemView.findViewById(R.id.encyclopedia_bird_name)
        private var image: ImageView = itemView.findViewById(R.id.encyclopedia_bird_image)

        fun bind(bird: BirdImageData){

            name.text = bird.birdKor
            Glide.with(itemView.context).load(bird.imageMale).centerCrop().into(image)
            // firebase의 해당 유저(uid)의 등록된 사진을 가져와 image을 교체
            // (새 이름과 이미지를 가져오고 안드로이드 내 db와 이름을 비교하여 일치하는 사진을 firebase에 있는 사진으로 교체)
            if(registDataKor.contains(bird.birdKor)){
                image.alpha = 1f

            }
            else {
                image.alpha = 0.3f
            }
            itemView.setOnClickListener{
                val intent = Intent(applicationContext, EncyclopediaBirdInforActivity::class.java)
                intent.putExtra("birdKor",bird.birdKor)
                intent.putExtra("type", type)
                intent.putExtra("otherUid", otherUid)
                startActivity(intent)
            }
        }

    }
    inner class MyAdapter(private val list:List<BirdImageData>): RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view=layoutInflater.inflate(R.layout.item_encyclopedia_bird_list, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(list[position])
        }
    }

    private fun createDatabase() {
        database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null)
    }

    fun loadAllImageData() {
        // DB에 있는 모든 새 이미지 검색
        val sql = "select * from $birdImage"
        val cursor = database?.rawQuery(sql, null)
        if (cursor != null) {
            BirdImageList.data.clear()

            for (i in 0 until cursor.count) {
                cursor.moveToNext()
                val specie_k = cursor.getString(0)
                val specie_e = cursor.getString(1)
                val image_m = cursor.getString(2)
                val image_f = cursor.getString(3)

                BirdImageList.data.add(BirdImageData(specie_k, specie_e, image_m, image_f))
            }
            cursor.close()
        }
    }
}