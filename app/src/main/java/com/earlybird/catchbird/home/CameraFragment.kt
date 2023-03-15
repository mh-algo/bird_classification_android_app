package com.earlybird.catchbird.home

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.FragmentCameraBinding
import java.io.*


open class CameraFragment : Fragment() {
    private val binding: FragmentCameraBinding by lazy {
        FragmentCameraBinding.inflate(layoutInflater)
    }
    private var textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

        }
    }

    private lateinit var imageDimension: Size
    private lateinit var cameraId: String
    private var cameraDevice: CameraDevice? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private val ORIENTATIONS:SparseIntArray = SparseIntArray().also {
        it.append(Surface.ROTATION_0, 90);
        it.append(Surface.ROTATION_90, 0);
        it.append(Surface.ROTATION_180, 270);
        it.append(Surface.ROTATION_270, 180);
    }

    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startBackgroundThread()

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (binding.textureView.isAvailable) {
                try {
                    openCamera()
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            } else {
                binding.textureView.surfaceTextureListener = textureListener
            }
        }
        else {
            Toast.makeText(requireContext(), "권한 설정을 확인해 주세요", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        startCamera()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (binding.textureView.isAvailable) {
            try {
                openCamera()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        } else {
            binding.textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            stopBackgroundThread()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun startCamera() {
        binding.textureView.surfaceTextureListener = textureListener
        binding.button.setOnClickListener {
            try {
                takePicture()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(InterruptedException::class)
    protected fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        mBackgroundThread?.join()
        mBackgroundThread = null
        mBackgroundHandler = null
    }


    // openCamera() 메서드는 TextureListener 에서 SurfaceTexture 가 사용 가능하다고 판단했을 시 실행된다
    private fun openCamera() {
        Log.e(TAG, "openCamera() : openCamera()메서드가 호출되었음")

        // 카메라의 정보를 가져와서 cameraId 와 imageDimension 에 값을 할당하고, 카메라를 열어야 하기 때문에
        // CameraManager 객체를 가져온다
        val manager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            // CameraManager 에서 cameraIdList 의 값을 가져온다
            // index 값이 0 이면 전면, 1이면 후면 카메라
            cameraId = manager.cameraIdList[0]

            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // SurfaceTexture 에 사용할 Size 값을 map 에서 가져와 imageDimension 에 할당해준다
            imageDimension = map!!.getOutputSizes<SurfaceTexture>(SurfaceTexture::class.java)[0]

            // 카메라를 열기전에 카메라 권한, 쓰기 권한이 있는지 확인한다
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED // 카메라 권한없음
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { // 쓰기권한 없음
                return
            }

            // CameraManager.openCamera() 메서드를 이용해 인자로 넘겨준 cameraId 의 카메라를 실행한다
            // 이때, stateCallback 은 카메라를 실행할때 호출되는 콜백메서드이며, cameraDevice 에 값을 할달해주고, 카메라 미리보기를 생성한다
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    // openCamera() 메서드에서 CameraManager.openCamera() 를 실행할때 인자로 넘겨주어야하는 콜백메서드
    // 카메라가 제대로 열렸으면, cameraDevice 에 값을 할당해주고, 카메라 미리보기를 생성한다
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "stateCallback : onOpened")

            // MainActivity 의 cameraDevice 에 값을 할당해주고, 카메라 미리보기를 시작한다
            // 나중에 cameraDevice 리소스를 해지할때 해당 cameraDevice 객체의 참조가 필요하므로,
            // 인자로 들어온 camera 값을 전역변수 cameraDevice 에 넣어 준다
            cameraDevice = camera

            // createCameraPreview() 메서드로 카메라 미리보기를 생성해준다
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "stateCallback : onDisconnected")

            // 연결이 해제되면 cameraDevice 를 닫아준다
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG, "stateCallback : onError")

            // 에러가 뜨면, cameraDevice 를 닫고, 전역변수 cameraDevice 에 null 값을 할당해 준다
            cameraDevice!!.close()
            cameraDevice = null
        }

    }

    // openCamera() 에 넘겨주는 stateCallback 에서 카메라가 제대로 연결되었으면
    // createCameraPreviewSession() 메서드를 호출해서 카메라 미리보기를 만들어준다
    private fun createCameraPreviewSession() {
        try {

            // 캡쳐세션을 만들기 전에 프리뷰를 위한 Surface 를 준비한다
            // 레이아웃에 선언된 textureView 로부터 surfaceTexture 를 얻을 수 있다
            val texture = binding.textureView.surfaceTexture

            // 미리보기를 위한 Surface 기본 버퍼의 크기는 카메라 미리보기크기로 구성
            texture?.setDefaultBufferSize(imageDimension.width, imageDimension.height)

            // 미리보기를 시작하기 위해 필요한 출력표면인 surface
            val surface = Surface(texture)

            // 미리보기 화면을 요청하는 RequestBuilder 를 만들어준다.
            // 이 요청은 위에서 만든 surface 를 타겟으로 한다
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            // 위에서 만든 surface 에 미리보기를 보여주기 위해 createCaptureSession() 메서드를 시작한다
            // createCaptureSession 의 콜백메서드를 통해 onConfigured 상태가 확인되면
            // CameraCaptureSession 을 통해 미리보기를 보여주기 시작한다
            cameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.d(TAG, "Configuration change")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    if(cameraDevice == null) {
                        // 카메라가 이미 닫혀있는경우, 열려있지 않은 경우
                        return
                    }
                    // session 이 준비가 완료되면, 미리보기를 화면에 뿌려주기 시작한다

                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

            }, null)

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    // 사진찍을 때 호출하는 메서드
    private fun takePicture() {
        try {
            val manager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            var jpegSizes: Array<Size>? = null
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG)

            val width = jpegSizes[0].width
            val height = jpegSizes[0].height

            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)

            val outputSurface = ArrayList<Surface>(2)
            outputSurface.add(imageReader.surface)
            outputSurface.add(Surface(binding.textureView.surfaceTexture))

            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader.surface)

            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            // 사진의 rotation 을 설정해준다
            val rotation = requireActivity().windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation))

            val tsLong = System.currentTimeMillis()/1000;
            val ts = tsLong.toString();

            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/pic${ts}.jpg")
            val readerListener = ImageReader.OnImageAvailableListener {
                var image : Image? = null

                try {
                    image = imageReader.acquireLatestImage()

                    val buffer = image!!.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)

                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output.write(bytes)
                    } finally {
                        output?.close()

                        val uri = Uri.fromFile(file)
                        Log.d(TAG, "uri 제대로 잘 바뀌었는지 확인 ${uri}")

                        showImageFragment(uri)
                    }

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                }
            }

            // imageReader 객체에 위에서 만든 readerListener 를 달아서, 이미지가 사용가능하면 사진을 저장한다
            imageReader.setOnImageAvailableListener(readerListener, null)

            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    /*Toast.makeText(this@MainActivity, "Saved:$file", Toast.LENGTH_SHORT).show()*/
                    Toast.makeText(requireContext(), "사진이 촬영되었습니다", Toast.LENGTH_SHORT).show()
                    createCameraPreviewSession()
                }
            }

            // outputSurface 에 위에서 만든 captureListener 를 달아, 캡쳐(사진 찍기) 해주고 나서 카메라 미리보기 세션을 재시작한다
            cameraDevice!!.createCaptureSession(outputSurface, object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {}

                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

            }, null)


        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    // 카메라 객체를 시스템에 반환하는 메서드
    // 카메라는 싱글톤 객체이므로 사용이 끝나면 무조건 시스템에 반환해줘야한다
    // 그래야 다른 앱이 카메라를 사용할 수 있다
    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private fun showImageFragment(uri: Uri) {
        val fragment = ShowImageFragment.newInstance(uri.toString())
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment).commit()
    }
}