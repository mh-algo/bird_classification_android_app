package com.earlybird.catchbird.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.FragmentMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource

class MapFragment : Fragment(), OnMapReadyCallback {
    private val binding: FragmentMapBinding by lazy {
        FragmentMapBinding.inflate(layoutInflater)
    }
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var location: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bundle = arguments
        location = bundle?.getString("location")
        Log.d("MapFragment", "Location Info SUCCESS!!")

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true

        // test (다중 마커는 다른 방법)
        val marker = Marker()
        marker.position = LatLng(37.5201, 126.9416)
        marker.map = naverMap

        val marker2 = Marker()
        marker2.position = LatLng(37.5214, 126.9412)
        marker2.map = naverMap

        val listener = Overlay.OnClickListener { overlay ->
            binding.infoLayout.visibility = View.VISIBLE
            true
        }

        marker.onClickListener = listener
        marker2.onClickListener = listener

        naverMap.setOnMapClickListener { pointF, latLng ->
            binding.infoLayout.visibility = View.INVISIBLE
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        fun newInstance(location: String): com.earlybird.catchbird.map.MapFragment {
            val fragment = com.earlybird.catchbird.map.MapFragment()

            val bundle = Bundle()
            bundle.putString("location", location)
            fragment.arguments = bundle

            return fragment
        }
    }
}