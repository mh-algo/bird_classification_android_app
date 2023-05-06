package com.earlybird.catchbird

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.earlybird.catchbird.databinding.DialogLayoutBinding
import com.earlybird.catchbird.map.MapFragment

class CustomDialog(
    private val encyclopediaBird: String?,
    private val type: String?,
    private val otherUid: String?
) : DialogFragment() {

    // 뷰 바인딩 정의
    private var _binding: DialogLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.CENTER_VERTICAL)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLayoutBinding.inflate(inflater, container, false)
        val view = binding.root

        showMapFragment()
        //dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMapFragment() {
        val fragment = MapFragment.newInstance(type, encyclopediaBird, otherUid)
        childFragmentManager.beginTransaction().replace(R.id.show_map_fragment, fragment).commit()
    }
}