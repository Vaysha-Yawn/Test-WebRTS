package com.test.testwebrtcapp.webRTC

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import java.util.UUID

class AudioTrackManager {
    private var localAudioTrack: AudioTrack? = null
    private var localAudioSource: AudioSource? = null

    fun addAudioTrackToConnection(peerConnection:PeerConnection){
        peerConnection.addTrack(localAudioTrack)
    }

    fun createLocalAudioTrack(peerConnectionFactory: PeerConnectionFactory, mediaConstraints: MediaConstraints){
        localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack("audio_track${UUID.randomUUID()}", localAudioSource)
    }

    suspend fun addTrack(receiver: RtpReceiver) = coroutineScope{
        launch(Dispatchers.Main) {
            (receiver.track() as AudioTrack).setEnabled(true)
        }
    }

    fun close(){
        localAudioTrack?.dispose()
        localAudioSource?.dispose()
    }
}