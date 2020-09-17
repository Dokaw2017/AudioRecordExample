package com.example.audiorecordexample

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class AudioRecorder private constructor(){


    companion object{
        private const val TAG:String = "OnlyAudioRecorder"
        private const val AudioSource = MediaRecorder.AudioSource.MIC//Student source
        private const val SampleRate = 16000//sampling rate
        private const val Channel = AudioFormat.CHANNEL_IN_MONO//Mono channel
        private const val EncodingType = AudioFormat.ENCODING_PCM_16BIT//data format
        private val PCMPath = Environment.getExternalStorageDirectory().path.toString()+"/zzz/RawAudio.pcm"
        private val WAVPath = Environment.getExternalStorageDirectory().path.toString()+"/zzz/FinalAudio.wav"
        //Single example of double check
        val instance:AudioRecorder by lazy (mode = LazyThreadSafetyMode.SYNCHRONIZED){
            AudioRecorder()
        }
    }

    private var bufferSizeInByte:Int = 0//Minimum recording buffer
    private var audioRecorder: AudioRecord? = null//Recording object
    private var isRecord = false

    private fun initRecorder() {//Initializing the audioRecord object

        bufferSizeInByte = AudioRecord.getMinBufferSize(SampleRate, Channel, EncodingType)
        audioRecorder = AudioRecord(AudioSource, SampleRate, Channel,
            EncodingType, bufferSizeInByte)
    }

    fun startRecord():Int {

        if (isRecord) {
            return -1
        } else{

            audioRecorder?: initRecorder()
            audioRecorder?.startRecording()
            isRecord = true

            AudioRecordToFile().start()
            return 0
        }
    }

    fun stopRecord() {

        audioRecorder?.stop()
        audioRecorder?.release()
        isRecord = false
        audioRecorder = null
    }

    private fun writeDateTOFile() {

        var audioData = ByteArray(bufferSizeInByte)
        val file = File(PCMPath)
        if (!file.parentFile.exists()) {

            file.parentFile.mkdirs()
        }
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val out = BufferedOutputStream(FileOutputStream(file))
        var length = 0
        while (isRecord && audioRecorder!=null) {
            length = audioRecorder!!.read(audioData, 0, bufferSizeInByte)//Get audio data
            if (AudioRecord.ERROR_INVALID_OPERATION != length) {
                out.write(audioData, 0, length)//write file
                out.flush()
            }
        }
        out.close()
    }

    private inner class AudioRecordToFile : Thread() {

        override fun run() {
            super.run()

            writeDateTOFile()
           //copyWaveFile(PCMPath, WAVPath)
        }
    }
}