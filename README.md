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