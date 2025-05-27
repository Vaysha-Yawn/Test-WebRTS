package com.test.testwebrtcapp.webRTC

import android.util.Log
import com.test.testwebrtcapp.ConnectionState
import com.test.testwebrtcapp.SignalClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.VideoTrack

class MyPeerConnectionObserver(
    private val signalClient: SignalClient,
    private val connectionStateListener: ((ConnectionState) -> Unit)? = null,
    private val videoManager: VideoTrackManager,
    private val audioManager: AudioTrackManager,
    private val viewModelScope: CoroutineScope
) : PeerConnection.Observer {
    override fun onIceCandidate(candidate: IceCandidate) {
        signalClient.addIceCandidate(candidate)
    }

    override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
        Log.d("WebRTC", "Ice candidates removed: ${candidates.joinToString()}")
    }

    override fun onAddStream(stream: MediaStream) {
        Log.d("WebRTC", "Stream added: ${stream.id}")
    }

    override fun onRemoveStream(stream: MediaStream) {
        Log.d("WebRTC", "Stream removed: ${stream.id}")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d("WebRTC", "Data channel opened: ${dataChannel.label()}")
    }

    override fun onRenegotiationNeeded() {
        Log.d("WebRTC", "Renegotiation needed")
    }

    override fun onAddTrack(receiver: RtpReceiver, streams: Array<MediaStream>) {
        Log.d("WebRTC", "Track added: ${receiver.track()?.id()}")
        when (receiver.track()) {
            is VideoTrack -> {
                viewModelScope.launch {
                    videoManager.addTrack(receiver)
                }
            }

            is AudioTrack -> {
                viewModelScope.launch {
                    audioManager.addTrack(receiver)
                }
            }
        }

    }

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
        Log.d("WebRTC", "Signaling state changed: $state")
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
        connectionStateListener?.invoke(connectMapper(state))
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        Log.d("WebRTC", "Ice connection receiving change: $receiving")
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
        Log.d("WebRTC", "Ice gathering state: $state")
    }


    private fun connectMapper(state: PeerConnection.IceConnectionState): ConnectionState {
        return when (state) {
            PeerConnection.IceConnectionState.CONNECTED -> {
                ConnectionState.CONNECTED
            }

            PeerConnection.IceConnectionState.FAILED -> {
                ConnectionState.FAILED
            }

            PeerConnection.IceConnectionState.NEW -> ConnectionState.NEW
            PeerConnection.IceConnectionState.CHECKING -> {
                ConnectionState.CHECKING
            }

            PeerConnection.IceConnectionState.COMPLETED -> ConnectionState.COMPLETED
            PeerConnection.IceConnectionState.DISCONNECTED -> ConnectionState.DISCONNECTED
            PeerConnection.IceConnectionState.CLOSED -> {
                ConnectionState.CLOSED
            }
        }
    }
}