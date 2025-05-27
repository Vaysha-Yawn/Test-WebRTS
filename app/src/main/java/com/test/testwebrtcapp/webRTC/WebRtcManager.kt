package com.test.testwebrtcapp.webRTC

import android.content.Context
import android.os.Build
import android.util.Log
import com.test.testwebrtcapp.ConnectionState
import com.test.testwebrtcapp.SignalClient
import kotlinx.coroutines.CoroutineScope
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoSink
import org.webrtc.audio.JavaAudioDeviceModule


class WebRtcManager(
    private val signalClient: SignalClient,
    private val viewModelScope: CoroutineScope,
):WebRtcManagerAbstract {
    private val mediaConstraints = buildMediaConstraints()
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    override val eglBase: EglBase = EglBase.create()
    private var connectionStateListener: ((ConnectionState) -> Unit)? = null
    private val audioManager = AudioTrackManager()
    private val videoManager = VideoTrackManager()

    private val iceServers = listOf(
        // STUN-серверы
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:freestun.net:3478").createIceServer(),
        //
        PeerConnection.IceServer.builder("turn:freestun.net:3478")
            .setUsername("free")
            .setPassword("free")
            .createIceServer(),
    )

    override fun setOnConnectionStateChanged(listener: (ConnectionState) -> Unit) {
        connectionStateListener = listener
    }

    override suspend fun initialize(context:Context) {
        initializePeerConnectionFactory(context)
        initializePeerConnection(iceServers)
        videoManager.createLocalVideoTrack(peerConnectionFactory!!, context, eglBase)
        audioManager.createLocalAudioTrack(peerConnectionFactory!!, mediaConstraints)
        addTracks()
    }

    private fun initializePeerConnectionFactory(context:Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .setAudioDeviceModule(
                JavaAudioDeviceModule
                    .builder(context)
                    .setUseHardwareAcousticEchoCanceler(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    .setUseHardwareNoiseSuppressor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    .setAudioRecordErrorCallback(AudioRecordErrorCallback)
                    .setAudioTrackErrorCallback(AudioTrackErrorCallback)
                    .setAudioRecordStateCallback(AudioRecordStateCallback)
                    .setAudioTrackStateCallback(AudioTrackStateCallback)
                    .createAudioDeviceModule().also {
                        it.setMicrophoneMute(false)
                        it.setSpeakerMute(false)
                    }
            )
            .createPeerConnectionFactory()
    }

    private fun initializePeerConnection(iceServers: List<PeerConnection.IceServer>) {

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        }

        peerConnection = peerConnectionFactory!!.createPeerConnection(
            rtcConfig,
            MyPeerConnectionObserver(signalClient, connectionStateListener, videoManager, audioManager, viewModelScope)
        )!!
    }

    private suspend fun addTracks() {
        videoManager.addVideoMirror()
        videoManager.addVideoTrackToConnection(peerConnection!!)
        audioManager.addAudioTrackToConnection(peerConnection!!)
    }

    override fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    override fun setLocalVideoRenderer(renderer: VideoSink) {
        videoManager.setLocalVideoRenderer(renderer)
    }

    override fun setRemoteVideoRenderer(renderer: VideoSink) {
        videoManager.setRemoteVideoRenderer(renderer)
    }

    override fun handleRemoteSessionDescription(session: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                if (session.type == SessionDescription.Type.OFFER) {
                    createAnswer()
                }
            }

            override fun onSetFailure(error: String) {
                Log.e("WebRTC", "Set remote description failed: $error")
            }

            override fun onCreateSuccess(sessionDescription: SessionDescription?) = Unit
            override fun onCreateFailure(error: String) = Unit
        }, session)
    }

    override fun createOffer() {
        val mediaConstraints = mediaConstraints.apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        signalClient.sendOffer(desc.description)
                    }

                    override fun onSetFailure(error: String) {
                        Log.e("WebRTC", "Set local description failed: $error")
                    }

                    override fun onCreateSuccess(sessionDescription: SessionDescription) = Unit

                    override fun onCreateFailure(error: String) = Unit
                }, desc)
            }

            override fun onSetFailure(error: String) {
                Log.e("WebRTC", "Create offer failed: $error")
            }

            override fun onSetSuccess() = Unit

            override fun onCreateFailure(error: String) = Unit
        }, mediaConstraints)
    }

    private fun createAnswer() {
        val observer = object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        signalClient.sendAnswer(desc.description)
                    }

                    override fun onSetFailure(error: String) {
                        Log.e("WebRTC", "Set answer failed: $error")
                    }

                    override fun onCreateSuccess(sessionDescription: SessionDescription) = Unit

                    override fun onCreateFailure(error: String) = Unit
                }, desc)
            }

            override fun onSetFailure(error: String) {
                Log.e("WebRTC", "Create answer failed: $error")
            }

            override fun onSetSuccess() = Unit

            override fun onCreateFailure(error: String) = Unit
        }
        peerConnection?.createAnswer(observer, mediaConstraints)
    }


    private fun buildMediaConstraints(): MediaConstraints {
        val items = listOf(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googAutoGainControl",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googHighpassFilter",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googTypingNoiseDetection",
                true.toString()
            )
        )

        return MediaConstraints().apply {
            with(optional) {
                add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
                addAll(items)
            }
        }
    }

    override fun close() {
        videoManager.close()
        audioManager.close()
        peerConnection?.dispose()
        eglBase.release()
        peerConnectionFactory?.dispose()
    }
}