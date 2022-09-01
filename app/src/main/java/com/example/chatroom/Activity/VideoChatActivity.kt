package com.example.chatroom.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.chatroom.Utils.MImageUtil
import com.example.chatroom.databinding.ActivityVideoChatBinding
import com.example.chatroom.model.Common
import com.example.chatroom.model.Content
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.properties.Delegates


class VideoChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoChatBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var isBack=false
    private lateinit var mRecorder:AudioRecord
    private lateinit var mAudioTrack: AudioTrack
    private lateinit var imageAnalysis:ImageAnalysis
    private var bufferSize=0
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Common.handler_videoImgUpdate->{
                    binding.previewViewOther.setImageBitmap(msg.obj as Bitmap)
                }
                Common.handler_voiceData->{
                    if(mAudioTrack!=null) {
                        mAudioTrack.write(msg.obj as ByteArray, 0, bufferSize)
                    }
                }
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoChatBinding.inflate(layoutInflater)
        Content.videoChatHandler=handler
        setContentView(binding.root)
        getSupportActionBar()?.title= Content.idNameRecord[Content.videoChatID]
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        //切换前后摄像头
        binding.buttonChangeCamera.setOnClickListener(){
            Toast.makeText(this,"切换前后摄像头", Toast.LENGTH_SHORT).show()
            if(isBack){
                bindPreview(cameraProvider, binding.previewViewSelf,CameraSelector.DEFAULT_BACK_CAMERA)
                cameraProvider.bindToLifecycle(this as LifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis, preview)
            }
            else{
                bindPreview(cameraProvider, binding.previewViewSelf,CameraSelector.DEFAULT_FRONT_CAMERA)
                cameraProvider.bindToLifecycle(this as LifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, imageAnalysis, preview)
            }
            isBack=!isBack
        }
        val sampleRateInHz = 44100;
        val channelConfig = AudioFormat.CHANNEL_IN_MONO;
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        setUpCamera(binding.previewViewSelf)


        Thread{
            checkPermission()
            mRecorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize)
            mAudioTrack= AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, bufferSize,AudioTrack.MODE_STREAM)
            mAudioTrack.play()
            mRecorder.startRecording()
            var sendData = ByteArray(4096)
            var receiveData = ByteArray(4096)
            while(Content.isVoice){
                mRecorder.read(sendData, 0, sendData.size);
                Content.voiceOutputStream.write(sendData)
            }
        }
    }

    @SuppressLint("RestrictedApi", "UnsafeOptInUsageError")
    private fun setUpCamera(previewView: PreviewView) {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider, previewView,CameraSelector.DEFAULT_FRONT_CAMERA)
                val  executor = Executors.newFixedThreadPool(2);
                imageAnalysis = ImageAnalysis.Builder()
                    // enable the following line if RGBA output is needed.
                    //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { imageProxy ->
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val imageBytes=MImageUtil.imageToJpegByteArray(imageProxy)
                    writeImage(imageBytes.rotate(rotationDegrees))

                    imageProxy.close()
                })
                cameraProvider.bindToLifecycle(this as LifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, imageAnalysis, preview)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
        cameraSelector : CameraSelector
    ) {
        //解除所有绑定，防止CameraProvider重复绑定到Lifecycle发生异常
        cameraProvider.unbindAll()
        preview = Preview.Builder().build()
        camera = cameraProvider.bindToLifecycle(
            this,cameraSelector
            , preview
        )
        preview?.setSurfaceProvider(previewView.surfaceProvider)
    }

    private fun writeImage(byteArray: ByteArray) {
        try {
            val size = ByteBuffer.allocate(4).putInt(byteArray.size).array()
            val data = ByteArray(size.size +byteArray.size)
            System.arraycopy(size, 0, data, 0, 4)
            System.arraycopy(byteArray, 0, data, 4, data.size - 4)
            Content.videoOutputStream.write(data)
            Content.videoOutputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Content.videoChatHandler=null
        Content.videoOutputStream=null
        Content.voiceOutputStream=null
        Content.isVideo=false
        Content.isVoice=false
    }

    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun Bitmap.toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(this.byteCount)
        this.copyPixelsToBuffer(byteBuffer)
        byteBuffer.rewind()
        return byteBuffer.array()
    }

    fun ByteArray.rotate(angle: Int): ByteArray {
        Log.d("labot_log_info", "CameraActivity: Inside rotateImage")
        var bmp = BitmapFactory.decodeByteArray(this, 0, this.size, null)
        val mat = Matrix()
        mat.postRotate(angle.toFloat())
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, mat, true)
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    //检查权限，获取权限
    public fun checkPermission() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        val requireList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) requireList.add(permission)
        }
        if (requireList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }
}
