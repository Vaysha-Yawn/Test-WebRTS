package com.test.testwebrtcapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.test.testwebrtcapp.ConnectionState
import com.test.testwebrtcapp.webRTC.WebRtcManagerAbstract
import org.webrtc.SurfaceViewRenderer


@Composable
fun Content(
    connectionState: ConnectionState,
    onEndCall: () -> Unit,
    webRtcManager: WebRtcManagerAbstract,
) {

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).background(Color.White), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {

        VideoView(true, webRtcManager)
        Empty()
        VideoView(false, webRtcManager)

        ButtonS("Закрыть вызов", true, onClick = onEndCall)
    }
}

@Composable
private fun VideoView(isLocal: Boolean, webRtcManager: WebRtcManagerAbstract, ) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                setEnableHardwareScaler(true)
                init(webRtcManager.eglBase.eglBaseContext, null)
                if (isLocal){
                    setMirror(true)
                    webRtcManager.setLocalVideoRenderer(this)
                }else{
                    webRtcManager.setRemoteVideoRenderer(this)
                }
            }},
        modifier = Modifier
            .fillMaxWidth(0.5f).aspectRatio(0.75f)
    )
}


@Composable
fun Empty() {
    Spacer(Modifier.height(20.dp))
}

@Composable
fun ButtonS(text: String, isActive: Boolean, onClick: () -> Unit) {
    Button(onClick, enabled = isActive, colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black, disabledContainerColor = Color.Black, disabledContentColor = Color.Cyan)) {
        Text(text)
    }
}