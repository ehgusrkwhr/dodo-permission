package com.dodo.permission

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.Fragment

/**
 * XML 레이아웃 환경에서 권한을 관리하기 위한 헬퍼 클래스
 * 
 * Compose 기반의 권한 요청 함수들을 XML 환경에서 사용할 수 있도록 래핑합니다.
 * 
 * @param activity 권한을 요청할 ComponentActivity
 */
class PermissionManager(private val activity: ComponentActivity) {
    
    /**
     * 단일 권한 요청 (향상된 콜백 방식)
     * 
     * @param permission 요청할 권한
     * @param denialCallback 거부 상황별 콜백
     * @param onGranted 권한 승인 시 콜백
     */
    fun requestPermission(
        permission: String,
        denialCallback: PermissionDenialCallback? = null,
        onGranted: () -> Unit = {}
    ) {
        activity.setContent {
            var shouldRequest by remember { mutableStateOf(true) }
            
            if (shouldRequest) {
                RequestSinglePermissionAdvanced(
                    permission = permission,
                    onGranted = {
                        onGranted()
                        shouldRequest = false
                    },
                    denialCallback = object : PermissionDenialCallback {
                        override fun onFirstTimeDenied(permission: String) {
                            denialCallback?.onFirstTimeDenied(permission)
                        }
                        override fun onSecondTimeDenied(permission: String) {
                            denialCallback?.onSecondTimeDenied(permission)
                        }
                        override fun onPermanentlyDenied(permission: String) {
                            denialCallback?.onPermanentlyDenied(permission)
                        }
                        override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
                            denialCallback?.onDenialWithInfo(denialInfo)
                            shouldRequest = false
                        }
                    }
                )
            }
        }
    }
    
    /**
     * 단일 권한 요청 (통합 콜백 방식)
     * 
     * @param permission 요청할 권한
     * @param callbacks 통합 콜백 인터페이스
     */
    fun requestPermissionWithCallbacks(
        permission: String,
        callbacks: PermissionCallbacks
    ) {
        activity.setContent {
            var shouldRequest by remember { mutableStateOf(true) }
            
            if (shouldRequest) {
                RequestSinglePermissionWithCallbacks(
                    permission = permission,
                    callbacks = object : PermissionCallbacks {
                        override fun onGranted() {
                            callbacks.onGranted()
                            shouldRequest = false
                        }
                        override fun onDenied(denialInfo: PermissionDenialInfo) {
                            callbacks.onDenied(denialInfo)
                            shouldRequest = false
                        }
                    }
                )
            }
        }
    }
    
    /**
     * 다중 권한 요청
     * 
     * @param permissions 요청할 권한 배열
     * @param onAllGranted 모든 권한 승인 시 콜백
     * @param onSomeGranted 일부 권한만 승인 시 콜백
     * @param onAllDenied 모든 권한 거부 시 콜백
     */
    fun requestMultiplePermissions(
        permissions: Array<String>,
        onAllGranted: () -> Unit = {},
        onSomeGranted: (granted: List<String>, denied: List<String>) -> Unit = { _, _ -> },
        onAllDenied: () -> Unit = {}
    ) {
        activity.setContent {
            var shouldRequest by remember { mutableStateOf(true) }
            
            if (shouldRequest) {
                RequestMultiplePermissions(
                    permissions = permissions,
                    onAllGranted = {
                        onAllGranted()
                        shouldRequest = false
                    },
                    onSomeGranted = { granted, denied ->
                        onSomeGranted(granted, denied)
                        shouldRequest = false
                    },
                    onAllDenied = {
                        onAllDenied()
                        shouldRequest = false
                    }
                )
            }
        }
    }
    
    /**
     * 순차 권한 요청
     * 
     * @param permissions 요청할 권한 배열 (순서대로)
     * @param onProgress 진행 상황 콜백
     * @param onAllGranted 모든 권한 승인 시 콜백
     * @param onSomeGranted 일부 권한만 승인 시 콜백
     * @param onAllDenied 모든 권한 거부 시 콜백
     */
    fun requestSequencePermissions(
        permissions: Array<String>,
        onProgress: (currentPermission: String, index: Int, total: Int) -> Unit = { _, _, _ -> },
        onAllGranted: () -> Unit = {},
        onSomeGranted: (granted: List<String>, denied: List<String>) -> Unit = { _, _ -> },
        onAllDenied: () -> Unit = {}
    ) {
        activity.setContent {
            var shouldRequest by remember { mutableStateOf(true) }
            
            if (shouldRequest) {
                RequestSequencePermissions(
                    permissions = permissions,
                    onProgress = onProgress,
                    onAllGranted = {
                        onAllGranted()
                        shouldRequest = false
                    },
                    onSomeGranted = { granted, denied ->
                        onSomeGranted(granted, denied)
                        shouldRequest = false
                    },
                    onAllDenied = {
                        onAllDenied()
                        shouldRequest = false
                    }
                )
            }
        }
    }
    
    /**
     * 기존 방식 권한 요청 (호환성)
     * 
     * @param permission 요청할 권한
     * @param onGranted 권한 승인 시 콜백
     * @param onDenied 권한 거부 시 콜백 (재요청 가능)
     * @param onPermanentlyDenied 권한 영구 거부 시 콜백
     */
    fun requestPermissionLegacy(
        permission: String,
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {},
        onPermanentlyDenied: () -> Unit = {}
    ) {
        activity.setContent {
            var shouldRequest by remember { mutableStateOf(true) }
            
            if (shouldRequest) {
                RequestSinglePermission(
                    permission = permission,
                    onGranted = {
                        onGranted()
                        shouldRequest = false
                    },
                    onDenied = {
                        onDenied()
                        shouldRequest = false
                    },
                    onPermanentlyDenied = {
                        onPermanentlyDenied()
                        shouldRequest = false
                    }
                )
            }
        }
    }
}

/**
 * Fragment에서 사용할 수 있는 확장 함수들
 */
fun Fragment.createPermissionManager(): PermissionManager {
    return PermissionManager(requireActivity() as ComponentActivity)
}


