package com.dodo.permission

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * DodoPermission - 핵심 기능만 포함한 깔끔한 권한 관리 모듈
 */

// ====== 1. 핵심 데이터 클래스들 ======
enum class PermissionState {
    GRANTED,                // ✅ 승인됨
    DENIED,                 // ⚠️ 거부됨 (재요청 가능)
    PERMANENTLY_DENIED      // ❌ 영구 거부됨 (설정에서만 변경 가능)
}

enum class PermissionDenialType {
    FIRST_TIME_DENIED,      // 첫 번째 거부 (1번 취소)
    SECOND_TIME_DENIED,     // 두 번째 거부 (2번 취소) 
    PERMANENTLY_DENIED      // 영구 거부 (더 이상 다이얼로그 표시 안됨)
}

data class PermissionResult(
    val permission: String,        // 권한 이름
    val state: PermissionState     // 권한 상태
)

data class PermissionDenialInfo(
    val permission: String,
    val denialType: PermissionDenialType,
    val denialCount: Int,
    val canShowRationale: Boolean
)

// ====== 2. 권한 취소 콜백 인터페이스 ======
interface PermissionDenialCallback {
    fun onFirstTimeDenied(permission: String) {}
    fun onSecondTimeDenied(permission: String) {}
    fun onPermanentlyDenied(permission: String) {}
    fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {}
}

interface PermissionCallbacks {
    fun onGranted() {}
    fun onDenied(denialInfo: PermissionDenialInfo) {}
}

// ====== 3. 유틸리티 함수들 ======
object PermissionUtils {
    private val denialCountMap = mutableMapOf<String, Int>()
    
    fun isPermissionGranted(context: android.content.Context, permission: String): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, 
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    fun isPermissionSystemAvailable(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
    }
    
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "카메라"
            Manifest.permission.RECORD_AUDIO -> "마이크"
            Manifest.permission.ACCESS_FINE_LOCATION -> "정확한 위치"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "대략적인 위치"
            Manifest.permission.READ_CONTACTS -> "연락처 읽기"
            Manifest.permission.WRITE_CONTACTS -> "연락처 쓰기"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "저장소 읽기"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "저장소 쓰기"
            else -> permission.substringAfterLast(".")
        }
    }
    
    fun getDenialCount(permission: String): Int {
        return denialCountMap[permission] ?: 0
    }
    
    fun incrementDenialCount(permission: String): Int {
        val newCount = (denialCountMap[permission] ?: 0) + 1
        denialCountMap[permission] = newCount
        return newCount
    }
    
    fun resetDenialCount(permission: String) {
        denialCountMap.remove(permission)
    }
    
    fun getDenialType(permission: String, canShowRationale: Boolean): PermissionDenialType {
        val denialCount = getDenialCount(permission)
        return when {
            !canShowRationale -> PermissionDenialType.PERMANENTLY_DENIED
            denialCount == 0 -> PermissionDenialType.FIRST_TIME_DENIED
            else -> PermissionDenialType.SECOND_TIME_DENIED
        }
    }
}

// ====== 4. 핵심 Compose API ======

/**
 * 단일 권한 요청 (기존 방식 - 호환성 유지)
 */
@Composable
fun RequestSinglePermission(
    permission: String,
    rationale: String? = null,
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onPermanentlyDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            PermissionUtils.resetDenialCount(permission)
            onGranted()
        } else {
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(permission) ?: false
            val denialCount = PermissionUtils.incrementDenialCount(permission)
            
            if (shouldShowRationale) {
                onDenied()
            } else {
                onPermanentlyDenied()
            }
        }
    }
    
    LaunchedEffect(permission) {
        if (PermissionUtils.isPermissionGranted(context, permission)) {
            onGranted()
        } else if (!PermissionUtils.isPermissionSystemAvailable()) {
            onGranted()
        } else {
            launcher.launch(permission)
        }
    }
}

/**
 * 향상된 단일 권한 요청 (취소 상황별 콜백 지원)
 */
@Composable
fun RequestSinglePermissionAdvanced(
    permission: String,
    rationale: String? = null,
    onGranted: () -> Unit = {},
    denialCallback: PermissionDenialCallback? = null
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            PermissionUtils.resetDenialCount(permission)
            onGranted()
        } else {
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(permission) ?: false
            val denialCount = PermissionUtils.incrementDenialCount(permission)
            val denialType = PermissionUtils.getDenialType(permission, shouldShowRationale)
            
            val denialInfo = PermissionDenialInfo(
                permission = permission,
                denialType = denialType,
                denialCount = denialCount,
                canShowRationale = shouldShowRationale
            )
            
            denialCallback?.let { callback ->
                when (denialType) {
                    PermissionDenialType.FIRST_TIME_DENIED -> callback.onFirstTimeDenied(permission)
                    PermissionDenialType.SECOND_TIME_DENIED -> callback.onSecondTimeDenied(permission)
                    PermissionDenialType.PERMANENTLY_DENIED -> callback.onPermanentlyDenied(permission)
                }
                callback.onDenialWithInfo(denialInfo)
            }
        }
    }
    
    LaunchedEffect(permission) {
        if (PermissionUtils.isPermissionGranted(context, permission)) {
            onGranted()
        } else if (!PermissionUtils.isPermissionSystemAvailable()) {
            onGranted()
        } else {
            launcher.launch(permission)
        }
    }
}

/**
 * 콜백 인터페이스를 사용한 단일 권한 요청
 */
@Composable
fun RequestSinglePermissionWithCallbacks(
    permission: String,
    rationale: String? = null,
    callbacks: PermissionCallbacks
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            PermissionUtils.resetDenialCount(permission)
            callbacks.onGranted()
        } else {
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(permission) ?: false
            val denialCount = PermissionUtils.incrementDenialCount(permission)
            val denialType = PermissionUtils.getDenialType(permission, shouldShowRationale)
            
            val denialInfo = PermissionDenialInfo(
                permission = permission,
                denialType = denialType,
                denialCount = denialCount,
                canShowRationale = shouldShowRationale
            )
            
            callbacks.onDenied(denialInfo)
        }
    }
    
    LaunchedEffect(permission) {
        if (PermissionUtils.isPermissionGranted(context, permission)) {
            callbacks.onGranted()
        } else if (!PermissionUtils.isPermissionSystemAvailable()) {
            callbacks.onGranted()
        } else {
            launcher.launch(permission)
        }
    }
}

/**
 * 다중 권한 요청
 */
@Composable
fun RequestMultiplePermissions(
    permissions: Array<String>,
    rationale: String? = null,
    onAllGranted: () -> Unit = {},
    onSomeGranted: (granted: List<String>, denied: List<String>) -> Unit = { _, _ -> },
    onAllDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.filterValues { it }.keys.toList()
        val denied = results.filterValues { !it }.keys.toList()
        
        when {
            denied.isEmpty() -> onAllGranted()
            granted.isNotEmpty() -> onSomeGranted(granted, denied)
            else -> onAllDenied()
        }
    }
    
    LaunchedEffect(permissions.contentHashCode()) {
        val currentStates = permissions.associateWith { permission ->
            PermissionUtils.isPermissionGranted(context, permission)
        }
        
        val alreadyGranted = currentStates.filterValues { it }.keys.toList()
        val needToRequest = currentStates.filterValues { !it }.keys.toList()
        
        if (needToRequest.isEmpty()) {
            onAllGranted()
        } else if (!PermissionUtils.isPermissionSystemAvailable()) {
            onAllGranted()
        } else {
            launcher.launch(needToRequest.toTypedArray())
        }
    }
}

/**
 * 순차 권한 요청
 */
@Composable
fun RequestSequencePermissions(
    permissions: Array<String>,
    rationale: String? = null,
    onAllGranted: () -> Unit = {},
    onProgress: (currentPermission: String, index: Int, total: Int) -> Unit = { _, _, _ -> },
    onSomeGranted: (granted: List<String>, denied: List<String>) -> Unit = { _, _ -> },
    onAllDenied: () -> Unit = {}
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var grantedPermissions by remember { mutableStateOf(listOf<String>()) }
    var deniedPermissions by remember { mutableStateOf(listOf<String>()) }
    var isSequenceActive by remember { mutableStateOf(true) }
    
    // 현재 권한 처리
    if (isSequenceActive && currentIndex < permissions.size) {
        val currentPermission = permissions[currentIndex]
        
        LaunchedEffect(currentIndex) {
            onProgress(currentPermission, currentIndex, permissions.size)
        }
        
        RequestSinglePermission(
            permission = currentPermission,
            rationale = rationale,
            onGranted = {
                grantedPermissions = grantedPermissions + currentPermission
                currentIndex++
            },
            onDenied = {
                deniedPermissions = deniedPermissions + currentPermission
                currentIndex++
            },
            onPermanentlyDenied = {
                deniedPermissions = deniedPermissions + currentPermission
                currentIndex++
            }
        )
    }
    
    // 시퀀스 완료 처리
    LaunchedEffect(currentIndex, permissions.size, isSequenceActive) {
        if (isSequenceActive && currentIndex >= permissions.size) {
            isSequenceActive = false
            
            when {
                deniedPermissions.isEmpty() -> onAllGranted()
                grantedPermissions.isNotEmpty() -> onSomeGranted(grantedPermissions, deniedPermissions)
                else -> onAllDenied()
            }
        }
    }
}

// ====== 4. 권한 상수들 ======
object PermissionConstants {
    // 개별 권한
    const val CAMERA = Manifest.permission.CAMERA
    const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
    const val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
    const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    
    // 권한 그룹들
    val LOCATION_GROUP = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    val CONTACTS_GROUP = arrayOf(READ_CONTACTS, WRITE_CONTACTS)
    val STORAGE_GROUP = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    val MEDIA_GROUP = arrayOf(CAMERA, RECORD_AUDIO)
}