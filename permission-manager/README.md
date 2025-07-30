# 🔐 DodoPermission - TedPermission 스타일 권한 관리

> 단일 파일에서 제공하는 깔끔한 Android 권한 관리 모듈

## 📁 파일 구조

```
permission-manager/src/main/java/com/dodo/permission/
└── DodoPermissionFinal.kt               # 모든 기능이 포함된 단일 파일
```

## 🚀 사용법

### 1. 단일 권한 요청

```kotlin
@Composable
fun CameraScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    
    if (shouldRequest) {
        RequestSinglePermission(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                result = "✅ 카메라 권한 승인됨"
                shouldRequest = false
                // 카메라 시작
            },
            onDenied = {
                result = "⚠️ 카메라 권한 거부됨 (재요청 가능)"
                shouldRequest = false
            },
            onPermanentlyDenied = {
                result = "❌ 카메라 권한 영구 거부됨 (설정 필요)"
                shouldRequest = false
            }
        )
    }
    
    Column {
        Button(onClick = { shouldRequest = true }) {
            Text("카메라 권한 요청")
        }
        Text(result)
    }
}
```

### 2. 다중 권한 요청

```kotlin
@Composable
fun MediaScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    
    if (shouldRequest) {
        RequestMultiplePermissions(
            permissions = PermissionConstants.MEDIA_GROUP, // 카메라 + 마이크
            onAllGranted = {
                result = "🎉 모든 권한 승인됨"
                shouldRequest = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 승인: ${grantedNames.joinToString()}\n거부: ${deniedNames.joinToString()}"
                shouldRequest = false
            },
            onAllDenied = {
                result = "❌ 모든 권한 거부됨"
                shouldRequest = false
            }
        )
    }
}
```

### 3. 순차 권한 요청

```kotlin
@Composable
fun SequentialScreen() {
    var shouldRequest by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    
    if (shouldRequest) {
        RequestSequencePermissions(
            permissions = arrayOf(
                PermissionConstants.CAMERA,
                PermissionConstants.RECORD_AUDIO,
                PermissionConstants.ACCESS_FINE_LOCATION
            ),
            onProgress = { current, index, total ->
                val name = PermissionUtils.getPermissionName(current)
                result = "📋 진행 중: $name (${index + 1}/$total)"
            },
            onAllGranted = {
                result = "🎉 모든 순차 권한 승인됨"
                shouldRequest = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 승인: ${grantedNames.joinToString()}\n거부: ${deniedNames.joinToString()}"
                shouldRequest = false
            }
        )
    }
}
```

## 📋 사용 가능한 API

### 핵심 Composable 함수들
- `RequestSinglePermission()` - 단일 권한 요청
- `RequestMultiplePermissions()` - 다중 권한 요청
- `RequestSequencePermissions()` - 순차 권한 요청

### 유틸리티
- `PermissionUtils.isPermissionGranted()` - 권한 승인 여부 확인
- `PermissionUtils.getPermissionName()` - 권한 이름 한국어 변환
- `PermissionUtils.isPermissionSystemAvailable()` - 권한 시스템 사용 가능 여부

### 권한 상수
```kotlin
// 개별 권한
PermissionConstants.CAMERA
PermissionConstants.RECORD_AUDIO
PermissionConstants.ACCESS_FINE_LOCATION
// ... 기타 권한들

// 권한 그룹
PermissionConstants.MEDIA_GROUP        // 카메라 + 마이크
PermissionConstants.LOCATION_GROUP     // 정확한 위치 + 대략적인 위치
PermissionConstants.CONTACTS_GROUP     // 연락처 읽기 + 쓰기
PermissionConstants.STORAGE_GROUP      // 저장소 읽기 + 쓰기
```

### 권한 상태
```kotlin
enum class PermissionState {
    GRANTED,                // ✅ 승인됨
    DENIED,                 // ⚠️ 거부됨 (재요청 가능)
    PERMANENTLY_DENIED      // ❌ 영구 거부됨 (설정에서만 변경 가능)
}
```

## 🎯 핵심 특징

- **📁 단일 파일**: TedPermission처럼 하나의 파일에서 모든 기능 제공
- **🎨 Compose 전용**: Jetpack Compose에 최적화된 선언적 API
- **🔧 핵심 기능만**: 불필요한 편의 함수 제거, 3가지 핵심 기능만 제공
- **📊 상세한 결과**: 승인/거부된 권한을 구체적으로 알 수 있음
- **🚦 영구 거부 감지**: `shouldShowRequestPermissionRationale()` 체크로 정확한 상태 판단
- **🌐 한국어 지원**: 권한 이름의 한국어 번역 제공
- **⚡ 안전한 처리**: ActivityResultRegistry 문제 해결

## ⚙️ 설치

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":permission-manager"))
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<!-- 필요한 권한들 추가 -->
```

```kotlin
// import 추가
import com.dodo.permission.*
```

## 🎉 완료!

이제 TedPermission처럼 단일 파일에서 깔끔하게 권한 관리를 할 수 있습니다!