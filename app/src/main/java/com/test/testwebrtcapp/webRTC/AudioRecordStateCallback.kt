package com.test.testwebrtcapp.webRTC

import android.util.Log
import org.webrtc.audio.JavaAudioDeviceModule

object AudioRecordStateCallback :
    JavaAudioDeviceModule.AudioRecordStateCallback {
    override fun onWebRtcAudioRecordStart() {
        Log.d("Audio", "[onWebRtcAudioRecordStart] no args")
    }

    override fun onWebRtcAudioRecordStop() {
        Log.d("Audio", "[onWebRtcAudioRecordStop] no args")
    }
}