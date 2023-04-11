package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import android.os.Bundle
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
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.FragmentEncyclopediaBinding
import kotlinx.android.synthetic.main.fragment_encyclopedia.*
import kotlinx.android.synthetic.main.item_classification.view.*


class EncyclopediaFragment : Fragment() {
    private val binding: FragmentEncyclopediaBinding by lazy {
        FragmentEncyclopediaBinding.inflate(layoutInflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        var sData = resources.getStringArray(R.array.sort)
        var adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,sData)
        (activity as MainActivity).loadAllImageData()
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // 전체사진, 도감등록사진 중 선택했을 때 어떤 코드 실행될지
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // 아무것도 선택하지 않으면 전체 화면
            }

        }
        binding.encyclopediaBtnRanking.setOnClickListener {
            val intent = Intent(context, EncyclopediaRankingActivity::class.java)
            startActivity(intent)
        }
        val data = BirdImageList.data
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerView.adapter = MyAdapter(data)

        // Inflate the layout for this fragment
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                val search = arrayListOf<BirdImageData>()
                if(query != null) {
                    for (i in data) {
                        val isExist = i.birdKor!!.contains(query, ignoreCase = true)
                        if(isExist){
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
                if(newText == ""){
                    binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
                    binding.recyclerView.adapter = MyAdapter(data)
                }

                return true
            }
        })
        return binding.root


    }
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){

        private var name: TextView = itemView.findViewById(R.id.encyclopedia_bird_name)
        private var image: ImageView = itemView.findViewById(R.id.encyclopedia_bird_image)

        fun bind(bird: BirdImageData){

            name.text = bird.birdKor
            Glide.with(view!!.context).load(bird.imageMale).centerCrop().into(image)


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