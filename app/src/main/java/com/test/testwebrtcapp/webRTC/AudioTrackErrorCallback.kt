package com.test.testwebrtcapp.webRTC

import android.util.Log
import org.webrtc.audio.JavaAudioDeviceModule

object AudioTrackErrorCallback :
    JavaAudioDeviceModule.AudioTrackErrorCallback {
    override fun onWebRtcAudioTrackInitError(p0: String?) {
        Log.d("Audio", "[onWebRtcAudioTrackInitError] $p0")
    }

    override fun onWebRtcAudioTrackStartError(
        p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
        p1: String?
    ) {
        Log.d("Audio", "[onWebRtcAudioTrackStartError] $p0")
    }

    override fun onWebRtcAudioTrackError(p0: String?) {
        Log.d("Audio", "[onWebRtcAudioTrackError] $p0")
    }
}