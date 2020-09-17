package com.example.audiorecordexample

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102

    private var recorder: AudioRecord? = null
    private var player: AudioTrack? = null
    private var recRunning = false
    private lateinit var recFile:File

    private fun requestPermission(permissionType: String, requestCode: Int) {
        val permission = ContextCompat.checkSelfPermission(this,
            permissionType)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(permissionType), requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0]
                    != PackageManager.PERMISSION_GRANTED) {

                    recordButton.isEnabled = false

                    Toast.makeText(this,
                        "Record permission required",
                        Toast.LENGTH_LONG).show()
                } else {
                    requestPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        STORAGE_REQUEST_CODE)
                }
                return
            }
            STORAGE_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0]
                    != PackageManager.PERMISSION_GRANTED) {
                    recordButton.isEnabled = false
                    Toast.makeText(this,
                        "External Storage permission required",
                        Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recFileName = "testkjs.raw"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        try {
            recFile = File(storageDir.toString() + "/" + recFileName)
        } catch (ex: IOException) {
// Error occurred while creating the File
        }
        audioSetup()

        recordButton.setOnClickListener {
            recordAudio()
            recordButton.isEnabled = false
            stopButton.isEnabled = true
        }

        stopButton.setOnClickListener {
            stopAudio()
            recordButton.isEnabled = true
            stopButton.isEnabled = false
        }

    }

      private fun recordAudio() {
            recRunning = true
            //stopButton.isEnabled = true
            //playButton.isEnabled = false
            //recordButton.isEnabled = false


            try {
                val outputStream = FileOutputStream(recFile)
                val bufferedOutputStream = BufferedOutputStream(outputStream)
                val dataOutputStream = DataOutputStream(bufferedOutputStream)

                val minBufferSize = AudioRecord.getMinBufferSize(
                    44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val aFormat = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
                val recorder = AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(aFormat)
                    .setBufferSizeInBytes(minBufferSize)
                    .build()
                val audioData = ByteArray(minBufferSize)
                recorder.startRecording()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

     private fun stopAudio(){
            //stopButton.isEnabled = false
            //playButton.isEnabled = true

            if (recRunning){
                recordButton.isEnabled = false
                recorder?.stop()
                recorder?.release()
                recorder = null
                recRunning = false
            }else{
                player?.release()
                player = null
                recordButton.isEnabled = true
            }
        }


    private fun audioSetup() {

        if (!hasMicrophone()) {
            stopButton.isEnabled = false
            playButton.isEnabled = false
            recordButton.isEnabled = false
        } else {
            playButton.isEnabled = false
            stopButton.isEnabled = false
        }

        requestPermission(Manifest.permission.RECORD_AUDIO,
            RECORD_REQUEST_CODE)

    }

    private fun hasMicrophone():Boolean{

        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

    }
}