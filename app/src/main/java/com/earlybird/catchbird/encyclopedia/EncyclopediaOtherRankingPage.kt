package com.earlybird.catchbird.encyclopedia

import android.content.Intent
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

class EncyclopediaOtherRankingPage : AppCompatActivity() {
    private val binding: ActivityEncyclopediaOtherRankingPageBinding by lazy {
        ActivityEncyclopediaOtherRankingPageBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var uid: String? = null
    var currentUserUid: String? = null
    var spinnerList = BirdImageList.data  // 전체사진, 도감 등록된 사진 구별하기 위한 변수
    val data = BirdImageList.data
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.encyclopediaBtnOk.setOnClickListener {
            finish()
        }
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        uid = intent.getStringExtra("otherUid")

        // todo 받아온 otherUid와 현재 로그인된 uid를 비교하여 같다면 나의 도감 페이지로 이동
        if(currentUserUid.equals(uid)) {
            MainActivity().ChangePage(R.id.navigation_encyclopedia)
        }
        fun BirdDataList(){
            binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.recyclerView.adapter = MyAdapter(data)
            spinnerList = BirdImageList.data
        }
        fun BirdRegistDataList(){
            val regist = arrayListOf<BirdImageData>()
            // firebase에 있는 도감등록 새 이름과 BirdImageList.data의 새 이름과 비교하여 일치하는 새 들만 regist 배열에 추가
            binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.recyclerView.adapter = MyAdapter(regist)
            spinnerList = regist
            Log.d("my", "유저 도감등록된 새 리스트 출력함수")
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
                    0 -> BirdDataList() // 전체사진
                    1 -> BirdRegistDataList() // firebase와 연동해서 유저 도감등록된 새 리스트만 출력
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //아무것도 선택하지 않으면 전체 화면
            }

        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = MyAdapter(data)

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
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private var name: TextView = itemView.findViewById(R.id.encyclopedia_bird_name)
        private var image: ImageView = itemView.findViewById(R.id.encyclopedia_bird_image)

        fun bind(bird: BirdImageData){

            name.text = bird.birdKor
            Glide.with(itemView.context).load(bird.imageMale).centerCrop().into(image)
            // firebase의 해당 유저(uid)의 등록된 사진을 가져와 image을 교체
            // (새 이름과 이미지를 가져오고 안드로이드 내 db와 이름을 비교하여 일치하는 사진을 firebase에 있는 사진으로 교체)

            itemView.setOnClickListener{
                val intent = Intent(applicationContext, EncyclopediaBirdInforActivity::class.java)
                intent.putExtra("birdKor",bird.birdKor)
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
}