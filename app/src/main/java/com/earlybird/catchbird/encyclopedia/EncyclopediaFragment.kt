package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.earlybird.catchbird.Bird
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBinding
import com.earlybird.catchbird.databinding.FragmentEncyclopediaBinding


class EncyclopediaFragment : Fragment() {
    private val binding: FragmentEncyclopediaBinding by lazy {
        FragmentEncyclopediaBinding.inflate(layoutInflater)
    }
    private var dummy = ArrayList<Bird>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        var sData = resources.getStringArray(R.array.sort)
        var adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,sData)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // 전체사진, 도감등록사진 중 선택했을 때 어떤 코드 실행될지
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //
            }

        }
        binding.encyclopediaBtnRanking.setOnClickListener {
            val intent = Intent(context, EncyclopediaRankingActivity::class.java)
            startActivity(intent)
        }


        dummy.apply {
            add(
                Bird(R.drawable.dummy_bird,"참새","참새입니다.",true)
            )
            add(
                Bird(R.drawable.dummy_bird,"가나다라마바사dkdkdkdkddkdkdkdkddk","참새입니다.",false)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새123","참새입니다.",true)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새23325","참새입니다.",false)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새dd","참새입니다.",false)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새as","참새입니다.",true)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새","참새입니다.",true)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새","참새입니다.",false)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새","참새입니다.",true)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새","참새입니다.",false)
            )
            add(
                Bird(R.drawable.dummy_bird,"참새","참새입니다.",true)
            )



        }
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerView.adapter = MyAdapter(dummy)

        // Inflate the layout for this fragment
        return binding.root
    }
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private lateinit var bird: Bird
        private var name: TextView = itemView.findViewById(R.id.encyclopedia_bird_name)

        fun bind(bird: Bird){
            this.bird=bird
            name.text = this.bird.name
            if(this.bird.isRegist){
                name.setTextColor(Color.parseColor("#ff99ff"));
            }

            itemView.setOnClickListener{
                val intent = Intent(context, EncyclopediaBirdInfor::class.java)
                startActivity(intent)
            }
        }

    }
    inner class MyAdapter(private val list:List<Bird>): RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view=layoutInflater.inflate(R.layout.item_encyclopedia_bird_list, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val bird=list[position]
            holder.bind(bird)
        }
    }
}