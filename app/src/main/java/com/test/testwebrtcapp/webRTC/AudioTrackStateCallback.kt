package com.test.testwebrtcapp.webRTC

import android.util.Log
import org.webrtc.audio.JavaAudioDeviceModule

object AudioTrackStateCallback :
    JavaAudioDeviceModule.AudioTrackStateCallback {
    override fun onWebRtcAudioTrackStart() {
        Log.d("Audio", "[onWebRtcAudioTrackStart] no args")
    }

    override fun onWebRtcAudioTrackStop() {
        Log.d("Audio", "[onWebRtcAudioTrackStop] no args")
    }
}