package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.earlybird.catchbird.*
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdInforBinding
import com.earlybird.catchbird.map.MapFragment


class EncyclopediaBirdInforActivity : AppCompatActivity(),ConfirmDialogInterface {
    private val binding: ActivityEncyclopediaBirdInforBinding by lazy {
        ActivityEncyclopediaBirdInforBinding.inflate(layoutInflater)
    }
    private var dummy = ArrayList<Infor>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.encyclopediaBtnBirdInforOk.setOnClickListener {
            finish()
        }
        binding.encyclopediaBirdLocation.setOnClickListener {
            val dialog = CustomDialog(this)
            dialog.isCancelable = true
            dialog.show(this.supportFragmentManager,"ConfirmDialog")
            /*binding.infoLayout.visibility = View.INVISIBLE
            binding.showMapFragment.visibility = View.VISIBLE
            val location = "위치 좌표"  // db에서 위치좌표 받아와야 함
            showMapFragment(location) // 위치확인 버튼 누르면 해당 새 위치정보화면 출력*/
        }
        binding.encyclopediaBirdInforText2.text="◈ 부리는 짧고 단단해서 곡식을 쪼아 먹기에 알맞다.\n" +
                "◈ 꽁지깃은 날 때 방향을 잡는 역할을 한다.\n" +
                "◈ 여름에는 해로운 곤충을 잡아먹어 사람에게 도움을 주지만, 가을에는 농작물에 피해를 주기도 한다.\n" +
                "◈ 모래와 물을 이용해 목욕하는 것을 좋아한다. 부리로 물을 쪼아 몸에 바르기도 하고 물구나무서기를 하는 등 목욕을 통해 몸에 붙어 있는 진드기, 먼지, 비듬 등을 털어낸다.\n" +
                "◈ 두 발로 뛰면서 땅 위에 내려와 먹이를 찾거나 농작물의 알곡을 먹는다.\n" +
                "◈ 두 발로 뛰면서 땅 위에 내려와 먹이를 찾거나 농작물의 알곡을 먹는다.\n" +
                "◈ 두 발로 뛰면서 땅 위에 내려와 먹이를 찾거나 농작물의 알곡을 먹는다.\n" +
                "◈ 두 발로 뛰면서 땅 위에 내려와 먹이를 찾거나 농작물의 알곡을 먹는다.\n" +
                "◈ 두 발로 뛰면서 땅 위에 내려와 먹이를 찾거나 농작물의 알곡을 먹는다.\n" +
                "◈ 두 발로 뛰면서 땅 위에 내려와 먹이를 찾거나 농작물의 알곡을 먹는다.\n" +
                "◈ 한쪽 눈으로 먹이를 찾아 낸 다음 양쪽 눈을 사용해 먹이를 보며 쪼아 먹는다.\n" +
                "◈ 번식이 끝나고 가을이 되면 무리를 이루어 집단으로 겨울을 난다.\n" +
                "◈ 참새는 산림성 조류를 관찰할 때, 발견한 새의 크기를 비교하는 ‘자’와 같은 역할을 한다고 하여 ‘자새’라고도 불린다.\n" +
                "[네이버 지식백과] 참새 [Eurasian Tree Sparrow] - 우리나라 사람들과 가장 가깝게 살고 있는 대표적인 텃새 (국립중앙과학관 - 우리나라 텃새)"
        binding.encyclopediaBirdInforImage2.setImageResource(R.drawable.dummy_bird) // 새 사진 눌렀을 때 해당 새 사진, 정보 출력
    }

    private fun showMapFragment(location: String) {
        val fragment = MapFragment.newInstance(location)
        supportFragmentManager.beginTransaction().replace(R.id.show_map_fragment, fragment).commit()
    }

    override fun onYesButtonClick(num: Int, theme: Int) {
        TODO("Not yet implemented")
    }
}