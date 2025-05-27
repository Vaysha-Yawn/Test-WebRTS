package com.test.testwebrtcapp.webRTC

import android.content.Context
import com.test.testwebrtcapp.ConnectionState
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.VideoSink


interface WebRtcManagerAbstract {

    val eglBase: EglBase

    fun setOnConnectionStateChanged(listener: (ConnectionState) -> Unit)

    suspend fun initialize(context: Context)

    fun close()

    fun addIceCandidate(candidate: IceCandidate)

    fun setLocalVideoRenderer(renderer: VideoSink)

    fun setRemoteVideoRenderer(renderer: VideoSink)

    fun handleRemoteSessionDescription(session: SessionDescription)

    fun createOffer()
}