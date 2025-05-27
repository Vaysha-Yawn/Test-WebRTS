package com.test.testwebrtcapp

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler() {
    val permission = rememberMultiplePermissionsState(listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    LaunchedEffect(Unit) {
        if (!permission.allPermissionsGranted) {
            permission.launchMultiplePermissionRequest()
        }
    }
}