package com.test.testwebrtcapp.webRTC

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.UUID

class VideoTrackManager {
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var localVideoSource: VideoSource? = null

    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var remoteVideoSink: VideoSink? = null
    private var localVideoSink: VideoSink? = null

    fun createLocalVideoTrack(peerConnectionFactory:PeerConnectionFactory, context:Context, eglBase:EglBase){
        createVideoCapturer(context, peerConnectionFactory, eglBase)
        localVideoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer!!.initialize(
            surfaceTextureHelper,
            context,
            localVideoSource?.capturerObserver
        )
        videoCapturer!!.startCapture(1280, 720, 30)
        localVideoTrack = peerConnectionFactory.createVideoTrack(
            "video_track${UUID.randomUUID()}",
            localVideoSource
        )
    }

    private fun createVideoCapturer(context:Context, peerConnectionFactory:PeerConnectionFactory, eglBase:EglBase) {
        val enumerator = Camera2Enumerator(context)
        videoCapturer = enumerator.deviceNames
            .firstOrNull { enumerator.isFrontFacing(it) }
            ?.let { enumerator.createCapturer(it, null) }
            ?: throw IllegalStateException("Front camera not found")

        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        localVideoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)
    }

    suspend fun addVideoMirror() = coroutineScope{
        localVideoTrack?.let {
            launch(Dispatchers.Main) {
                it.addSink(localVideoSink)
            }
        }
    }

    fun addVideoTrackToConnection(peerConnection: PeerConnection){
        peerConnection.addTrack(localVideoTrack)
    }

    fun setLocalVideoRenderer(renderer: VideoSink) {
        localVideoSink = renderer
    }

    fun setRemoteVideoRenderer(renderer: VideoSink) {
        remoteVideoSink = renderer
    }

    suspend fun addTrack(receiver: RtpReceiver) = coroutineScope{
        launch(Dispatchers.Main) {
            (receiver.track() as VideoTrack).addSink(remoteVideoSink)
        }
    }

    fun close(){
        localVideoSource?.dispose()
        localVideoTrack?.dispose()
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        surfaceTextureHelper?.dispose()
    }
}