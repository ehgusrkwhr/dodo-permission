# DodoPermission 라이브러리 사용하기

다른 프로젝트에서 DodoPermission 라이브러리를 사용하는 방법을 안내합니다.

## 📦 설치 방법

### 1. JitPack을 통한 설치 (권장)

#### 프로젝트 레벨 build.gradle.kts 또는 settings.gradle.kts에 JitPack 저장소 추가:

```kotlin
// settings.gradle.kts (또는 프로젝트 레벨 build.gradle.kts)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // JitPack 저장소 추가
    }
}
```

#### 또는 프로젝트 레벨 build.gradle (Groovy)에서:

```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' } // JitPack 저장소 추가
    }
}
```

#### 앱 레벨 build.gradle.kts에 의존성 추가:

```kotlin
dependencies {
    implementation("com.github.your-username:dodo-permission:1.0.0")
    
    // 필수 의존성들
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
}
```

### 2. 로컬 AAR 파일 사용

라이브러리를 AAR 파일로 빌드하여 사용하는 경우:

```bash
# 프로젝트 루트에서 AAR 빌드
./gradlew :permission-manager:assembleRelease

# 생성된 AAR 파일을 다른 프로젝트의 libs 폴더에 복사한 후
# app/build.gradle.kts에 추가:
```

```kotlin
dependencies {
    implementation(files("libs/permission-manager-release.aar"))
    
    // 필수 의존성들도 함께 추가 필요
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
}
```

## 🚀 기본 사용법

### 1. 매니페스트에 권한 선언

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 2. Compose에서 사용

```kotlin
import com.dodo.permission.*

@Composable
fun MyScreen() {
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
                    // 첫 번째 거부 처리
                    println("첫 번째 거부")
                }
                
                override fun onSecondTimeDenied(permission: String) {
                    // 두 번째 거부 처리
                    println("두 번째 거부")
                }
                
                override fun onPermanentlyDenied(permission: String) {
                    // 영구 거부 처리
                    println("영구 거부")
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

### 3. XML 레이아웃 환경에서 사용

```kotlin
import com.dodo.permission.*

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 권한 매니저 초기화
        permissionManager = PermissionManager(this)
        
        // 권한 요청
        findViewById<Button>(R.id.btn_camera).setOnClickListener {
            permissionManager.requestPermission(
                permission = PermissionConstants.CAMERA,
                denialCallback = object : PermissionDenialCallback {
                    override fun onFirstTimeDenied(permission: String) {
                        Toast.makeText(this@MainActivity, "카메라 권한이 필요해요", Toast.LENGTH_SHORT).show()
                    }
                    
                    override fun onSecondTimeDenied(permission: String) {
                        showPermissionDialog()
                    }
                    
                    override fun onPermanentlyDenied(permission: String) {
                        showSettingsDialog()
                    }
                },
                onGranted = {
                    Toast.makeText(this@MainActivity, "카메라 권한 승인!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
```

## 🔧 고급 사용법

### 다중 권한 요청

```kotlin
@Composable
fun MultiplePermissionsExample() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestMultiplePermissions(
            permissions = PermissionConstants.MEDIA_GROUP, // 카메라 + 마이크
            onAllGranted = {
                println("모든 권한 승인!")
                shouldRequest = false
            },
            onSomeGranted = { granted, denied ->
                println("일부 승인: $granted, 거부: $denied")
                shouldRequest = false
            },
            onAllDenied = {
                println("모든 권한 거부")
                shouldRequest = false
            }
        )
    }
    
    Button(onClick = { shouldRequest = true }) {
        Text("미디어 권한 요청")
    }
}
```

### 순차 권한 요청

```kotlin
@Composable
fun SequentialPermissionsExample() {
    var shouldRequest by remember { mutableStateOf(false) }
    
    if (shouldRequest) {
        RequestSequencePermissions(
            permissions = arrayOf(
                PermissionConstants.CAMERA,
                PermissionConstants.RECORD_AUDIO,
                PermissionConstants.ACCESS_FINE_LOCATION
            ),
            onProgress = { current, index, total ->
                val name = PermissionUtils.getPermissionName(current)
                println("진행 중: $name ($index/$total)")
            },
            onAllGranted = {
                println("모든 순차 권한 승인!")
                shouldRequest = false
            },
            onSomeGranted = { granted, denied ->
                println("순차 완료 - 승인: $granted, 거부: $denied")
                shouldRequest = false
            }
        )
    }
}
```

## 🔑 주요 클래스 및 함수

### 권한 요청 함수들
- `RequestSinglePermissionAdvanced()` - 향상된 단일 권한 요청
- `RequestSinglePermissionWithCallbacks()` - 통합 콜백 방식
- `RequestMultiplePermissions()` - 다중 권한 요청
- `RequestSequencePermissions()` - 순차 권한 요청
- `RequestSinglePermission()` - 기존 방식 (호환성)

### 콜백 인터페이스
- `PermissionDenialCallback` - 거부 상황별 개별 콜백
- `PermissionCallbacks` - 통합 콜백 인터페이스

### 데이터 클래스
- `PermissionDenialInfo` - 거부 정보
- `PermissionDenialType` - 거부 유형 enum

### 유틸리티
- `PermissionUtils` - 권한 관련 유틸리티 함수들
- `PermissionConstants` - 권한 상수 정의
- `PermissionManager` - XML 환경용 헬퍼 클래스

## 🔍 권한 상수 사용

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

## 🛠️ 유틸리티 함수 활용

```kotlin
// 권한 상태 확인
val isGranted = PermissionUtils.isPermissionGranted(context, PermissionConstants.CAMERA)

// 권한 이름 가져오기 (한국어)
val permissionName = PermissionUtils.getPermissionName(PermissionConstants.CAMERA) // "카메라"

// 거부 횟수 관리
val denialCount = PermissionUtils.getDenialCount(PermissionConstants.CAMERA)
PermissionUtils.resetDenialCount(PermissionConstants.CAMERA)
```

## 🎯 권장 UX 패턴

### 단계별 권한 거부 처리
```kotlin
override fun onFirstTimeDenied(permission: String) {
    // 1번 거부: 간단하고 친근한 메시지
    showToast("권한이 필요해요 😊")
}

override fun onSecondTimeDenied(permission: String) {
    // 2번 거부: 상세한 설명과 재시도 유도
    showDialog("권한이 꼭 필요한 이유", "앱의 핵심 기능을 위해 필요합니다.", onRetry = { retry() })
}

override fun onPermanentlyDenied(permission: String) {
    // 영구 거부: 설정 화면 안내
    showSettingsDialog("설정 > 앱 권한에서 허용해주세요")
}
```

## ⚠️ 주의사항

1. **최소 SDK**: Android 7.0 (API 24) 이상
2. **Compose 버전**: Compose BOM 2024.12.01 이상 권장
3. **Activity**: ComponentActivity 또는 그 하위 클래스 사용 필요
4. **권한 선언**: AndroidManifest.xml에 사용할 권한 미리 선언 필요

## 🔗 추가 리소스

- [GitHub 저장소](https://github.com/your-username/dodo-permission)
- [상세 사용 가이드](PERMISSION_MODULE_GUIDE.md)
- [예제 프로젝트](app/)

## 🤝 기여하기

버그 리포트나 기능 제안은 GitHub Issues에 올려주세요.

## 📄 라이선스

MIT License