# DodoPermission 모듈

권한 취소 상황별 처리가 가능한 Android 권한 관리 모듈입니다.

## ✨ 주요 기능

- 🎯 **권한 거부 상황 구분**: 1번 취소, 2번 취소, 영구 거부를 명확히 구분
- 🎨 **Compose 우선 설계**: Jetpack Compose와 완벽 통합
- 🔄 **다양한 요청 방식**: 단일/다중/순차 권한 요청 지원
- 🛡️ **안전한 처리**: ActivityResultRegistry 문제 없음
- 🇰🇷 **한국어 지원**: 권한 이름 자동 번역
- ✅ **기존 코드 호환**: 기존 방식과 새로운 방식 모두 지원
- 🧪 **완전한 테스트 앱**: 모든 기능을 실제로 테스트 가능

## 🛠️ 모듈 구조

```
permission-manager/
└── src/main/java/com/dodo/permission/
    └── DodoPermissionFinal.kt     # 모든 기능이 통합된 단일 파일
```

### 주요 구성 요소

- **PermissionDenialType**: 거부 상황 구분 enum (1번/2번 취소, 영구 거부)
- **PermissionDenialInfo**: 거부 정보 데이터 클래스
- **PermissionDenialCallback**: 거부 상황별 개별 콜백 인터페이스
- **PermissionCallbacks**: 통합 콜백 인터페이스
- **PermissionUtils**: 유틸리티 함수들 (거부 카운트 관리 포함)
- **PermissionConstants**: 권한 상수 정의

## 🚀 빠른 시작

### 1. 의존성 추가

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":permission-manager"))
}
```

### 2. 매니페스트 권한 선언

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 3. 권한 취소 상황별 처리

```kotlin
@Composable
fun CameraScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestSinglePermissionAdvanced(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                println("카메라 권한 승인!")
                shouldRequest = false
            },
            denialCallback = object : PermissionDenialCallback {
                override fun onFirstTimeDenied(permission: String) {
                    // 첫 번째 거부 - 친근한 안내
                    showToast("카메라 권한이 필요해요 😊")
                }
                
                override fun onSecondTimeDenied(permission: String) {
                    // 두 번째 거부 - 상세한 설명
                    showDialog("권한이 꼭 필요한 이유", "사진 촬영을 위해 필요합니다")
                }
                
                override fun onPermanentlyDenied(permission: String) {
                    // 영구 거부 - 설정 안내
                    showSettingsDialog()
                }
                
                override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
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

## 📋 권한 거부 상황 구분

| 상황 | 설명 | 권장 대응 |
|------|------|----------|
| **FIRST_TIME_DENIED** | 첫 번째 거부 (1번 취소) | 간단하고 친근한 메시지 |
| **SECOND_TIME_DENIED** | 두 번째 거부 (2번 취소) | 권한 필요성에 대한 상세한 설명 |
| **PERMANENTLY_DENIED** | 영구 거부 (설정 필요) | 설정 화면으로 안내 |

## 💡 사용 예제

### 향상된 콜백 방식
```kotlin
RequestSinglePermissionAdvanced(
    permission = PermissionConstants.CAMERA,
    onGranted = { /* 승인 처리 */ },
    denialCallback = object : PermissionDenialCallback {
        override fun onFirstTimeDenied(permission: String) { /* 1번 거부 */ }
        override fun onSecondTimeDenied(permission: String) { /* 2번 거부 */ }
        override fun onPermanentlyDenied(permission: String) { /* 영구 거부 */ }
    }
)
```

### 통합 콜백 방식
```kotlin
RequestSinglePermissionWithCallbacks(
    permission = PermissionConstants.RECORD_AUDIO,
    callbacks = object : PermissionCallbacks {
        override fun onGranted() { /* 승인 */ }
        override fun onDenied(denialInfo: PermissionDenialInfo) {
            when (denialInfo.denialType) {
                PermissionDenialType.FIRST_TIME_DENIED -> { /* 1번 거부 */ }
                PermissionDenialType.SECOND_TIME_DENIED -> { /* 2번 거부 */ }
                PermissionDenialType.PERMANENTLY_DENIED -> { /* 영구 거부 */ }
            }
        }
    }
)
```

### 다중 권한 요청
```kotlin
RequestMultiplePermissions(
    permissions = PermissionConstants.MEDIA_GROUP, // 카메라 + 마이크
    onAllGranted = { /* 모두 승인 */ },
    onSomeGranted = { granted, denied -> /* 일부 승인 */ },
    onAllDenied = { /* 모두 거부 */ }
)
```

### 기존 방식 (호환성)
```kotlin
RequestSinglePermission(
    permission = PermissionConstants.CAMERA,
    onGranted = { /* 승인 */ },
    onDenied = { /* 거부 */ },
    onPermanentlyDenied = { /* 영구 거부 */ }
)
```

## 📱 XML 레이아웃 환경에서 사용법

### Activity에서 사용

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 권한 매니저 초기화
        permissionManager = PermissionManager(this)
        
        // 버튼 클릭 시 권한 요청
        findViewById<Button>(R.id.btn_camera).setOnClickListener {
            requestCameraPermission()
        }
    }
    
    private fun requestCameraPermission() {
        permissionManager.requestPermission(
            permission = PermissionConstants.CAMERA,
            denialCallback = object : PermissionDenialCallback {
                override fun onFirstTimeDenied(permission: String) {
                    // 첫 번째 거부 - 친근한 토스트
                    Toast.makeText(
                        this@MainActivity, 
                        "카메라 권한이 필요해요 😊", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                override fun onSecondTimeDenied(permission: String) {
                    // 두 번째 거부 - 상세 설명 다이얼로그
                    showPermissionDialog(
                        title = "권한이 꼭 필요합니다",
                        message = "사진 촬영 기능을 위해 카메라 권한이 반드시 필요합니다.",
                        onRetry = { requestCameraPermission() }
                    )
                }
                
                override fun onPermanentlyDenied(permission: String) {
                    // 영구 거부 - 설정 안내
                    showSettingsDialog()
                }
                
                override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
                    // 로깅 또는 분석
                    logPermissionDenial(denialInfo)
                }
            },
            onGranted = {
                // 권한 승인됨 - 카메라 기능 시작
                startCameraActivity()
            }
        )
    }
    
    private fun showPermissionDialog(title: String, message: String, onRetry: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("다시 시도") { _, _ -> onRetry() }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("설정에서 권한 허용")
            .setMessage("카메라 권한이 거부되었습니다.\n설정 > 앱 권한에서 카메라를 허용해주세요.")
            .setPositiveButton("설정 열기") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
```

### Fragment에서 사용

```kotlin
class CameraFragment : Fragment() {
    private lateinit var permissionManager: PermissionManager
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 권한 매니저 초기화 (Fragment용)
        permissionManager = PermissionManager(requireActivity())
        
        view.findViewById<Button>(R.id.btn_request_permission).setOnClickListener {
            requestMicrophonePermission()
        }
    }
    
    private fun requestMicrophonePermission() {
        // 통합 콜백 방식 사용
        permissionManager.requestPermissionWithCallbacks(
            permission = PermissionConstants.RECORD_AUDIO,
            callbacks = object : PermissionCallbacks {
                override fun onGranted() {
                    // 권한 승인 - UI 업데이트
                    updateUI(granted = true)
                }
                
                override fun onDenied(denialInfo: PermissionDenialInfo) {
                    when (denialInfo.denialType) {
                        PermissionDenialType.FIRST_TIME_DENIED -> {
                            // 스낵바로 간단한 안내
                            Snackbar.make(
                                requireView(),
                                "마이크 권한이 필요합니다 (${denialInfo.denialCount}번째)",
                                Snackbar.LENGTH_LONG
                            ).setAction("재시도") {
                                requestMicrophonePermission()
                            }.show()
                        }
                        
                        PermissionDenialType.SECOND_TIME_DENIED -> {
                            // 더 상세한 설명
                            showDetailDialog()
                        }
                        
                        PermissionDenialType.PERMANENTLY_DENIED -> {
                            // 설정 화면 안내
                            showSettingsSnackbar()
                        }
                    }
                }
            }
        )
    }
    
    private fun showDetailDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("마이크 권한 안내")
            .setMessage("음성 기록 기능을 위해 마이크 권한이 필요합니다.\n권한을 허용해주시겠어요?")
            .setPositiveButton("권한 허용") { _, _ ->
                requestMicrophonePermission()
            }
            .setNegativeButton("나중에", null)
            .show()
    }
    
    private fun showSettingsSnackbar() {
        Snackbar.make(
            requireView(),
            "설정에서 마이크 권한을 허용해주세요",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("설정") {
            openAppSettings()
        }.show()
    }
}
```

### 다중 권한 요청 (XML 환경)

```kotlin
class MediaActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)
        
        permissionManager = PermissionManager(this)
        
        findViewById<Button>(R.id.btn_media_permissions).setOnClickListener {
            requestMediaPermissions()
        }
    }
    
    private fun requestMediaPermissions() {
        permissionManager.requestMultiplePermissions(
            permissions = PermissionConstants.MEDIA_GROUP, // 카메라 + 마이크
            onAllGranted = {
                // 모든 권한 승인됨
                Toast.makeText(this, "미디어 권한이 모두 승인되었습니다!", Toast.LENGTH_SHORT).show()
                startMediaFeature()
            },
            onSomeGranted = { granted, denied ->
                // 일부만 승인됨
                val grantedNames = granted.joinToString { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.joinToString { PermissionUtils.getPermissionName(it) }
                
                AlertDialog.Builder(this)
                    .setTitle("일부 권한만 승인됨")
                    .setMessage("승인: $grantedNames\n거부: $deniedNames")
                    .setPositiveButton("확인", null)
                    .show()
            },
            onAllDenied = {
                // 모든 권한 거부됨
                Toast.makeText(this, "미디어 기능을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
```

### PermissionManager 클래스 구현

XML 환경에서 사용할 수 있도록 하는 헬퍼 클래스입니다:

```kotlin
class PermissionManager(private val activity: ComponentActivity) {
    
    fun requestPermission(
        permission: String,
        denialCallback: PermissionDenialCallback,
        onGranted: () -> Unit
    ) {
        // Compose 함수를 Activity에서 사용할 수 있도록 래핑
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
                            denialCallback.onFirstTimeDenied(permission)
                        }
                        override fun onSecondTimeDenied(permission: String) {
                            denialCallback.onSecondTimeDenied(permission)
                        }
                        override fun onPermanentlyDenied(permission: String) {
                            denialCallback.onPermanentlyDenied(permission)
                        }
                        override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
                            denialCallback.onDenialWithInfo(denialInfo)
                            shouldRequest = false
                        }
                    }
                )
            }
        }
    }
    
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
    
    fun requestMultiplePermissions(
        permissions: Array<String>,
        onAllGranted: () -> Unit,
        onSomeGranted: (granted: List<String>, denied: List<String>) -> Unit,
        onAllDenied: () -> Unit
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
}
```

## 🎮 테스트 앱

프로젝트에는 실제 동작을 확인할 수 있는 테스트 앱이 포함되어 있습니다:

- **단일 권한**: 기본적인 권한 요청
- **다중 권한**: 여러 권한 동시 요청
- **순차 권한**: 권한을 하나씩 차례대로 요청
- **취소 콜백**: 거부 상황별 처리 테스트 ⭐

앱을 실행하고 "취소 콜백" 탭에서 실제로 권한을 여러 번 거부해보며 동작을 확인할 수 있습니다.

## 🔧 유틸리티 함수

```kotlin
// 권한 상태 확인
val isGranted = PermissionUtils.isPermissionGranted(context, PermissionConstants.CAMERA)

// 권한 이름 가져오기 (한국어)
val name = PermissionUtils.getPermissionName(PermissionConstants.CAMERA) // "카메라"

// 거부 횟수 관리
val count = PermissionUtils.getDenialCount(PermissionConstants.CAMERA)
PermissionUtils.resetDenialCount(PermissionConstants.CAMERA)
```

## 🏗️ 권한 상수

```kotlin
object PermissionConstants {
    // 개별 권한
    const val CAMERA = Manifest.permission.CAMERA
    const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    
    // 권한 그룹
    val MEDIA_GROUP = arrayOf(CAMERA, RECORD_AUDIO)
    val LOCATION_GROUP = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
}
```

## 📖 상세 가이드

자세한 사용법과 예제는 [PERMISSION_MODULE_GUIDE.md](PERMISSION_MODULE_GUIDE.md)를 참고하세요.

### 순차 권한 요청

```kotlin
RequestSequencePermissions(
    permissions = arrayOf(
        PermissionConstants.CAMERA,
        PermissionConstants.RECORD_AUDIO,
        PermissionConstants.ACCESS_FINE_LOCATION
    ),
    onProgress = { current, index, total ->
        println("진행 중: ${PermissionUtils.getPermissionName(current)} ($index/$total)")
    },
    onAllGranted = { println("모든 권한 승인!") },
    onSomeGranted = { granted, denied -> 
        println("승인: ${granted.size}개, 거부: ${denied.size}개")
    }
)
```

## 🚦 권장 UX 패턴

### 1. 단계별 안내
- **1번 거부**: 간단하고 친근한 메시지 
- **2번 거부**: 더 상세한 설명과 권한의 필요성 강조
- **영구 거부**: 설정 화면 안내

### 2. 구현 예시
```kotlin
override fun onFirstTimeDenied(permission: String) {
    showToast("카메라 권한이 필요해요 😊")
}

override fun onSecondTimeDenied(permission: String) {
    showDialog("권한 설명", "사진 촬영을 위해 카메라 권한이 반드시 필요합니다.")
}

override fun onPermanentlyDenied(permission: String) {
    showSettingsDialog("설정 > 앱 권한에서 허용해주세요")
}
```

## 🤝 기여하기

버그 리포트나 기능 제안은 Issues에 올려주세요.

## 📄 라이선스

MIT License

---

**🎉 사용자 친화적인 권한 관리로 더 나은 UX를 만들어보세요!**