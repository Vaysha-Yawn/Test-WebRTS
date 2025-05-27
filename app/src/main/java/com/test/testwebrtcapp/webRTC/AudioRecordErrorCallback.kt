package com.test.testwebrtcapp.webRTC

import android.util.Log
import org.webrtc.audio.JavaAudioDeviceModule

object AudioRecordErrorCallback:
    JavaAudioDeviceModule.AudioRecordErrorCallback {
    override fun onWebRtcAudioRecordInitError(p0: String?) {
        Log.d("Audio", "[onWebRtcAudioRecordInitError] $p0")
    }

    override fun onWebRtcAudioRecordStartError(
        p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
        p1: String?
    ) {
        Log.d("Audio", "[onWebRtcAudioRecordInitError] $p1")
    }

    override fun onWebRtcAudioRecordError(p0: String?) {
        Log.d("Audio", "[onWebRtcAudioRecordError] $p0")
    }
}