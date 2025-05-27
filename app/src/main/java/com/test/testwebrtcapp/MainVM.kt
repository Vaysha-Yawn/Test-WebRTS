package com.test.testwebrtcapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.test.testwebrtcapp.webRTC.WebRtcManager
import com.test.testwebrtcapp.webRTC.WebRtcManagerAbstract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.webrtc.SessionDescription

class MainVM(application: Application) : AndroidViewModel(application) {

    private val signalClient = SignalClient()
    val webRtcManager: WebRtcManagerAbstract = WebRtcManager(signalClient, viewModelScope)
    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    init {
        viewModelScope.launch(Dispatchers.Default) {
            webRtcManager.setOnConnectionStateChanged { _connectionState.value = it }
            webRtcManager.initialize(application)
            startSignaling()
            _connectionState.asStateFlow().stateIn(viewModelScope).collect {
                if (it == ConnectionState.FAILED || it == ConnectionState.DISCONNECTED) {
                    endCall()
                    webRtcManager.initialize(application)
                }
            }
        }
    }

    private fun startSignaling() {
        signalClient.checkOffers { description ->
            if (description == null) {
                //offer
                webRtcManager.createOffer()
                signalClient.listenForAnswers { answdescription ->
                    webRtcManager.handleRemoteSessionDescription(
                        SessionDescription(
                            SessionDescription.Type.ANSWER,
                            answdescription
                        )
                    )
                    signalClient.listenForIce { iceCandidate ->
                        webRtcManager.addIceCandidate(iceCandidate)
                    }
                }
            } else {
                //answer
                webRtcManager.handleRemoteSessionDescription(
                    SessionDescription(
                        SessionDescription.Type.OFFER,
                        description
                    )
                )
                signalClient.listenForIce { iceCandidate ->
                    webRtcManager.addIceCandidate(iceCandidate)
                }
            }
        }
    }

    fun endCall() {
        signalClient.close()
        webRtcManager.close()
    }

    override fun onCleared() {
        endCall()
    }
}

