package com.earlybird.catchbird.map

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.FragmentMapBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kotlin.math.abs
import kotlin.math.pow


class MapFragment : Fragment(), OnMapReadyCallback {
    private val binding: FragmentMapBinding by lazy {
        FragmentMapBinding.inflate(layoutInflater)
    }
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private val markersInfo = arrayListOf<HashMap<String, String>>()
    private val activeMarkers = arrayListOf<Marker>()
    private val userName = hashMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        markersInfo.clear()
        val db = Firebase.firestore
        db.collection("profileImages")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    userName[document.id] = document.data["nickname"].toString()
                }
            }
        db.collectionGroup("imageInfo")
            .get()
            .addOnSuccessListener {documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} -> ${document.data}")
                    val markersInfoData = document.data as HashMap<String, String>
                    markersInfo.add(markersInfoData)
                    val latitude = markersInfoData["latitude"].toString()
                    val longitude = markersInfoData["longitude"].toString()
                    val location = LatLng(latitude.toDouble(), longitude.toDouble())

                    val currentPosition = getCurrentPosition(naverMap)
                    if (!withinSightMarker(currentPosition, location,naverMap.cameraPosition.zoom)) continue
                    val marker = Marker()
                    marker.position = location
                    marker.map = naverMap
                    marker.onClickListener = onClickListener(markersInfoData)
                    activeMarkers.add(marker)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase imageInfo loading fail!!!!!!")
            }

        naverMap.addOnCameraChangeListener { i, b ->
            freeActiveMarkers()
            // 정의된 마커위치들중 가시거리 내에있는것들만 마커 생성
            val currentPosition = getCurrentPosition(naverMap)
            for (markersInfoData in markersInfo) {
                val latitude = markersInfoData["latitude"].toString()
                val longitude = markersInfoData["longitude"].toString()
                val location = LatLng(latitude.toDouble(), longitude.toDouble())
                if (!withinSightMarker(currentPosition, location, naverMap.cameraPosition.zoom)) continue
                val marker = Marker()
                marker.position = location
                marker.map = naverMap
                marker.onClickListener = onClickListener(markersInfoData)
                activeMarkers.add(marker)
            }
        }

        // 지도 터키할 경우 새 정보창 닫기
        naverMap.setOnMapClickListener { pointF, latLng ->
            binding.infoLayout.visibility = View.INVISIBLE
        }
    }

    // 현재 카메라가 보고있는 위치
    fun getCurrentPosition(naverMap: NaverMap): LatLng {
        val cameraPosition = naverMap.cameraPosition
        return LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude)
    }

    // 선택한 마커의 위치가 가시거리(카메라가 보고있는 위치 반경 내)에 있는지 확인
    fun withinSightMarker(currentPosition: LatLng, markerPosition: LatLng, zoom: Double): Boolean {
        val rate = if (zoom >= 12) 5 else (2.0.pow(12-zoom)*5).toInt()
        val withinSightMarkerLat =
            abs(currentPosition.latitude - markerPosition.latitude) <= rate / 109.958489129649955
        val withinSightMarkerLng =
            abs(currentPosition.longitude - markerPosition.longitude) <= rate / 88.74
        return withinSightMarkerLat && withinSightMarkerLng
    }

    // 지도상에 표시되고있는 마커들 지도에서 삭제
    private fun freeActiveMarkers() {
        for (activeMarker in activeMarkers) {
            activeMarker.map = null
        }
        activeMarkers.clear()
    }

    // 새 정보창 열기
    private fun onClickListener(markerInfo: HashMap<String,String>):Overlay.OnClickListener {
        val listener = Overlay.OnClickListener { overlay ->
            val uid = markerInfo["uid"]
            binding.infoLayout.visibility = View.VISIBLE
            binding.birdName.text = markerInfo["bird"]
            binding.userName.text = userName[uid]
            binding.birdDate.text = markerInfo["date"] + " " + markerInfo["time"]
            val imageUri = markerInfo["imageUri"]?.toUri()
            Glide.with(requireActivity()).load(imageUri).into(binding.imageView)
            true
        }
        return listener
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}