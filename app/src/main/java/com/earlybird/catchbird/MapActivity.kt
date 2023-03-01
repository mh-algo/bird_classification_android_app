package com.earlybird.catchbird

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.earlybird.catchbird.databinding.ActivityMapBinding
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding: ActivityMapBinding by lazy {
        ActivityMapBinding.inflate(layoutInflater)
    }

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // test (다중 마커는 다른 방법)
        val marker = Marker()
        marker.position = LatLng(37.5201, 126.9416)
        marker.map = naverMap

        val marker2 = Marker()
        marker2.position = LatLng(37.5214, 126.9412)
        marker2.map = naverMap
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}