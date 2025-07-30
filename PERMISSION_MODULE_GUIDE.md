# DodoPermission 모듈 사용 가이드

권한 취소 상황별 처리가 가능한 Android 권한 관리 모듈입니다.

## 목차
- [개요](#개요)
- [권한 취소 상황 구분](#권한-취소-상황-구분)
- [Compose 방식 사용법](#compose-방식-사용법)
- [기존 방식 (호환성)](#기존-방식-호환성)
- [권한 상수](#권한-상수)
- [실제 사용 예제](#실제-사용-예제)

## 개요

DodoPermission은 Android 권한을 관리하는 Compose 기반 모듈로, 특히 권한 거부 상황을 세분화하여 처리할 수 있는 기능을 제공합니다.

### 주요 기능
- ✅ 1번 취소, 2번 취소, 영구 거부 상황 구분
- ✅ 단일/다중/순차 권한 요청 지원
- ✅ Compose UI와 완벽 통합
- ✅ 기존 코드와의 호환성 유지

## 권한 취소 상황 구분

### PermissionDenialType
```kotlin
enum class PermissionDenialType {
    FIRST_TIME_DENIED,      // 첫 번째 거부 (1번 취소)
    SECOND_TIME_DENIED,     // 두 번째 거부 (2번 취소) 
    PERMANENTLY_DENIED      // 영구 거부 (더 이상 다이얼로그 표시 안됨)
}
```

### PermissionDenialInfo
```kotlin
data class PermissionDenialInfo(
    val permission: String,           // 권한 이름
    val denialType: PermissionDenialType,  // 거부 유형
    val denialCount: Int,            // 거부 횟수
    val canShowRationale: Boolean    // Rationale 표시 가능 여부
)
```

## Compose 방식 사용법

### 1. 향상된 단일 권한 요청 (권장)

개별 콜백으로 각 상황을 처리하고 싶을 때 사용합니다.

```kotlin
@Composable
fun CameraPermissionScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestSinglePermissionAdvanced(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                // 권한 승인됨
                println("카메라 권한이 승인되었습니다!")
                shouldRequest = false
            },
            denialCallback = object : PermissionDenialCallback {
                override fun onFirstTimeDenied(permission: String) {
                    // 첫 번째 거부 - 친근한 안내
                    showDialog("카메라 권한이 필요해요", "다시 한 번 시도해보시겠어요?")
                }
                
                override fun onSecondTimeDenied(permission: String) {
                    // 두 번째 거부 - 조금 더 상세한 설명
                    showDialog("권한이 필요한 이유", "사진 촬영 기능을 위해 카메라 권한이 꼭 필요합니다.")
                }
                
                override fun onPermanentlyDenied(permission: String) {
                    // 영구 거부 - 설정 화면 안내
                    showSettingsDialog("설정에서 권한 허용", "설정 > 앱 권한에서 카메라를 허용해주세요.")
                }
                
                override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
                    // 모든 거부 상황에서 호출됨 (추가 로깅 등)
                    logPermissionDenial(denialInfo)
                    shouldRequest = false
                }
            }
        )
    }
    
    Button(onClick = { shouldRequest = true }) {
        Text("카메라 권한 요청")
    }
}
```

### 2. 통합 콜백 방식

하나의 콜백에서 모든 상황을 처리하고 싶을 때 사용합니다.

```kotlin
@Composable
fun MicPermissionScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestSinglePermissionWithCallbacks(
            permission = PermissionConstants.RECORD_AUDIO,
            callbacks = object : PermissionCallbacks {
                override fun onGranted() {
                    println("마이크 권한 승인!")
                    shouldRequest = false
                }
                
                override fun onDenied(denialInfo: PermissionDenialInfo) {
                    when (denialInfo.denialType) {
                        PermissionDenialType.FIRST_TIME_DENIED -> {
                            showSnackbar("마이크 권한이 필요합니다 (${denialInfo.denialCount}번째)")
                        }
                        PermissionDenialType.SECOND_TIME_DENIED -> {
                            showDialog("권한 안내", "음성 기록을 위해 마이크 권한이 필요합니다")
                        }
                        PermissionDenialType.PERMANENTLY_DENIED -> {
                            navigateToSettings()
                        }
                    }
                    shouldRequest = false
                }
            }
        )
    }
}
```

### 3. 다중 권한 요청

여러 권한을 동시에 요청할 때 사용합니다.

```kotlin
@Composable
fun MediaPermissionsScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestMultiplePermissions(
            permissions = PermissionConstants.MEDIA_GROUP, // 카메라 + 마이크
            onAllGranted = {
                println("모든 미디어 권한 승인!")
                shouldRequest = false
            },
            onSomeGranted = { granted, denied ->
                println("일부 승인: $granted, 거부: $denied")
                shouldRequest = false
            },
            onAllDenied = {
                println("모든 권한 거부됨")
                shouldRequest = false
            }
        )
    }
}
```

### 4. 순차 권한 요청

권한을 하나씩 차례대로 요청할 때 사용합니다.

```kotlin
@Composable
fun SequentialPermissionsScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestSequencePermissions(
            permissions = arrayOf(
                PermissionConstants.CAMERA,
                PermissionConstants.RECORD_AUDIO,
                PermissionConstants.ACCESS_FINE_LOCATION
            ),
            onProgress = { currentPermission, index, total ->
                val name = PermissionUtils.getPermissionName(currentPermission)
                println("진행 중: $name ($index/$total)")
            },
            onAllGranted = {
                println("모든 순차 권한 승인!")
                shouldRequest = false
            },
            onSomeGranted = { granted, denied ->
                println("순차 처리 완료 - 승인: $granted, 거부: $denied")
                shouldRequest = false
            },
            onAllDenied = {
                println("모든 권한 거부됨")
                shouldRequest = false
            }
        )
    }
}
```

## 기존 방식 (호환성)

기존 코드와의 호환성을 위해 유지되는 방식입니다.

### 단일 권한 요청 (기존)

```kotlin
@Composable
fun LegacyPermissionScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestSinglePermission(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                println("권한 승인!")
                shouldRequest = false
            },
            onDenied = {
                println("권한 거부됨 (재요청 가능)")
                shouldRequest = false
            },
            onPermanentlyDenied = {
                println("권한 영구 거부됨")
                shouldRequest = false
            }
        )
    }
}
```

## 권한 상수

미리 정의된 권한 상수를 사용할 수 있습니다.

```kotlin
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
    
    // 권한 그룹
    val LOCATION_GROUP = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    val CONTACTS_GROUP = arrayOf(READ_CONTACTS, WRITE_CONTACTS)
    val STORAGE_GROUP = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    val MEDIA_GROUP = arrayOf(CAMERA, RECORD_AUDIO)
}
```

## 실제 사용 예제

### 상황별 UI 대응 예제

```kotlin
@Composable
fun SmartPermissionHandler() {
    var shouldRequestCamera by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DialogState?>(null) }
    
    // 다이얼로그 상태
    sealed class DialogState {
        object FirstDenial : DialogState()
        object SecondDenial : DialogState()
        object PermanentDenial : DialogState()
    }
    
    if (shouldRequestCamera) {
        RequestSinglePermissionAdvanced(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                // 권한 승인 - 카메라 화면으로 이동
                navigateToCamera()
                shouldRequestCamera = false
            },
            denialCallback = object : PermissionDenialCallback {
                override fun onFirstTimeDenied(permission: String) {
                    dialogState = DialogState.FirstDenial
                }
                
                override fun onSecondTimeDenied(permission: String) {
                    dialogState = DialogState.SecondDenial
                }
                
                override fun onPermanentlyDenied(permission: String) {
                    dialogState = DialogState.PermanentDenial
                }
                
                override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
                    shouldRequestCamera = false
                }
            }
        )
    }
    
    // 상황별 다이얼로그 표시
    when (val state = dialogState) {
        is DialogState.FirstDenial -> {
            AlertDialog(
                onDismissRequest = { dialogState = null },
                title = { Text("카메라 권한이 필요해요") },
                text = { Text("사진을 촬영하기 위해 카메라 권한이 필요합니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        dialogState = null
                        shouldRequestCamera = true
                    }) { Text("다시 시도") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogState = null }) { Text("취소") }
                }
            )
        }
        
        is DialogState.SecondDenial -> {
            AlertDialog(
                onDismissRequest = { dialogState = null },
                title = { Text("권한이 꼭 필요합니다") },
                text = { Text("앱의 핵심 기능인 사진 촬영을 위해 카메라 권한이 반드시 필요합니다. 허용해주시겠어요?") },
                confirmButton = {
                    TextButton(onClick = {
                        dialogState = null
                        shouldRequestCamera = true
                    }) { Text("권한 허용") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogState = null }) { Text("나중에") }
                }
            )
        }
        
        is DialogState.PermanentDenial -> {
            AlertDialog(
                onDismissRequest = { dialogState = null },
                title = { Text("설정에서 권한 허용") },
                text = { Text("카메라 권한이 거부되었습니다. 설정 > 앱 권한에서 카메라를 허용해주세요.") },
                confirmButton = {
                    TextButton(onClick = {
                        dialogState = null
                        openAppSettings()
                    }) { Text("설정 열기") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogState = null }) { Text("취소") }
                }
            )
        }
        
        null -> { /* 다이얼로그 없음 */ }
    }
    
    Button(onClick = { shouldRequestCamera = true }) {
        Text("카메라 시작")
    }
}
```

### 유틸리티 함수 활용

```kotlin
// 권한 상태 확인
val isCameraGranted = PermissionUtils.isPermissionGranted(context, PermissionConstants.CAMERA)

// 권한 이름 가져오기
val permissionName = PermissionUtils.getPermissionName(PermissionConstants.CAMERA) // "카메라"

// 거부 횟수 확인 및 관리
val denialCount = PermissionUtils.getDenialCount(PermissionConstants.CAMERA)
PermissionUtils.resetDenialCount(PermissionConstants.CAMERA) // 카운트 초기화
```

## 권장 패턴

### 1. 단계별 안내
- **1번 거부**: 간단하고 친근한 메시지
- **2번 거부**: 더 상세한 설명과 권한의 필요성 강조
- **영구 거부**: 설정 화면 안내

### 2. UX 고려사항
- 권한 요청 전에 먼저 기능 설명
- 거부 시에도 앱 사용이 가능하도록 대안 제공
- 설정 화면 이동 시 명확한 안내 제공

### 3. 테스트 방법
앱의 "취소 콜백" 탭에서 실제 동작을 테스트할 수 있습니다:
1. 권한을 1번 거부 → 첫 번째 거부 처리 확인
2. 권한을 2번 거부 → 두 번째 거부 처리 확인  
3. "다시 묻지 않음" 체크 후 거부 → 영구 거부 처리 확인

이 가이드를 참고하여 사용자 친화적인 권한 관리를 구현해보세요!