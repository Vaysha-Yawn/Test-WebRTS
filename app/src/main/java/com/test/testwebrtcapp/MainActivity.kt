package com.test.testwebrtcapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.test.testwebrtcapp.ui.Content
import com.test.testwebrtcapp.ui.theme.TestWebRTCAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val connectionState by viewModel.connectionState.collectAsState()
            PermissionHandler()
            TestWebRTCAppTheme {
                Content(
                    connectionState = connectionState,
                    onEndCall = viewModel::endCall,
                    webRtcManager = viewModel.webRtcManager,
                )
            }

            LaunchedEffect(connectionState) {
                if (connectionState == ConnectionState.CONNECTED) {
                    Toast.makeText(this@MainActivity, "CONNECTED", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.endCall()
        super.onDestroy()
    }
}