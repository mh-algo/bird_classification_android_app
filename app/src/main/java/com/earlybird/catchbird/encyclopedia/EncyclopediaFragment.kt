package com.earlybird.catchbird.encyclopedia

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.community.LoginActivity
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.FragmentEncyclopediaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_encyclopedia.*
import kotlinx.android.synthetic.main.item_classification.view.*
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class EncyclopediaFragment : Fragment() {
    private val binding: FragmentEncyclopediaBinding by lazy {
        FragmentEncyclopediaBinding.inflate(layoutInflater)
    }
    var spinnerList = BirdImageList.data  // 전체사진, 도감 등록된 사진 구별하기 위한 변수
    val data = BirdImageList.data

    //Firebase Auth 관리 클래스
    private var auth: FirebaseAuth? = null
    private var uid : String? = null
    private var firestore: FirebaseFirestore? = null
    private var currentUserUid : String? = null
    private var registDataKor = mutableSetOf<String>()
    private var registDataAll = arrayListOf<BirdImageData>()
    private var registImageData = arrayListOf<BirdImageData>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        currentUserUid = auth?.currentUser?.uid

        val db = Firebase.firestore
        Log.d("test1","${data}")
        db.collection("birdImageData").document(currentUserUid.toString()).collection("imageInfo")
            .get()//todo list도 만들어서 새 설명창에 버튼누르면 찍은 사진 출력되게 하기
            .addOnSuccessListener { documents ->
                for (document in documents){
                    Log.d("test2","${data}")
                    registDataKor.add(document.data["bird"].toString())
                    var image = document.data["imageUri"]
                    registDataAll.add(BirdImageData(document.data["bird"].toString(),document.data["bird"].toString(),image.toString(),image.toString()))
                }
                Log.d("test3","${data}")
                binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
                binding.recyclerView.adapter = MyAdapter(data)


            }

        fun BirdDataList(){
            binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
            binding.recyclerView.adapter = MyAdapter(data)
            spinnerList = BirdImageList.data
        }
        fun BirdRegistDataList(){
            binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
            binding.recyclerView.adapter = MyAdapter(registImageData)
            spinnerList = registImageData

        }

        var sData = resources.getStringArray(R.array.sort)
        var adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,sData)
        (activity as MainActivity).loadAllImageData()
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // 전체사진, 도감등록사진 중 선택했을 때 어떤 코드 실행될지
                when(p2) {
                    0 -> {
                        registImageData.clear()
                        BirdDataList()
                    } // 전체사진
                    1 -> {
                        BirdRegistDataList() // firebase와 연동해서 유저 도감등록된 새 리스트만 출력
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // 아무것도 선택하지 않으면 전체 화면
            }

        }


        binding.encyclopediaBtnRanking.setOnClickListener {
            val intent = Intent(context, EncyclopediaRankingActivity::class.java)
            startActivity(intent)
        }


        // Inflate the layout for this fragment
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                val search = arrayListOf<BirdImageData>()
                if (query.equals("")) {
                    binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
                    binding.recyclerView.adapter = MyAdapter(spinnerList)
                }
                if (query != null) {
                    for (i in spinnerList) {
                        val isExist = i.birdKor!!.contains(query, ignoreCase = true)
                        if (isExist) {
                            search.add(i)
                        }
                    }
                    binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
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
        return binding.root


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
            Glide.with(view!!.context).load(bird.imageMale).centerCrop().into(image)
           if(registDataKor.contains(bird.birdKor)){
               image.alpha = 1f
               for(i in 0 .. registImageData.size){
                   if(i == registImageData.size){
                       registImageData.add(bird)
                       break
                   }
                   if(registImageData[i].birdKor == bird.birdKor)
                       break
               }
           }
           else {
               image.alpha = 0.3f
           }

            itemView.setOnClickListener{
                val intent = Intent(context, EncyclopediaBirdInforActivity::class.java)
                intent.putExtra("birdKor",bird.birdKor)
                startActivity(intent)
            }
        }

    }
    inner class MyAdapter(private val list: ArrayList<BirdImageData>): RecyclerView.Adapter<MyViewHolder>(){
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