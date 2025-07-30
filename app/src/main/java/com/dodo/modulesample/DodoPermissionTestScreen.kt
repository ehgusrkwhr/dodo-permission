package com.dodo.modulesample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.dodo.permission.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DodoPermissionTestScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("단일 권한") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("다중 권한") }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("순차 권한") }
            )
            Tab(
                selected = selectedTabIndex == 3,
                onClick = { selectedTabIndex = 3 },
                text = { Text("취소 콜백") }
            )
        }
        
        when (selectedTabIndex) {
            0 -> SinglePermissionTab()
            1 -> MultiplePermissionTab()
            2 -> SequencePermissionTab()
            3 -> DenialCallbackTab()
        }
    }
}

@Composable
fun SinglePermissionTab() {
    var shouldRequestCamera by remember { mutableStateOf(false) }
    var shouldRequestMic by remember { mutableStateOf(false) }
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("결과가 여기에 표시됩니다") }
    
    // 카메라 권한 요청
    if (shouldRequestCamera) {
        RequestSinglePermission(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                result = "✅ 카메라 권한이 승인되었습니다!"
                shouldRequestCamera = false
            },
            onDenied = {
                result = "⚠️ 카메라 권한이 거부되었습니다 (재요청 가능)"
                shouldRequestCamera = false
            },
            onPermanentlyDenied = {
                result = "❌ 카메라 권한이 영구적으로 거부되었습니다 (설정 필요)"
                shouldRequestCamera = false
            }
        )
    }
    
    // 마이크 권한 요청
    if (shouldRequestMic) {
        RequestSinglePermission(
            permission = PermissionConstants.RECORD_AUDIO,
            onGranted = {
                result = "✅ 마이크 권한이 승인되었습니다!"
                shouldRequestMic = false
            },
            onDenied = {
                result = "⚠️ 마이크 권한이 거부되었습니다 (재요청 가능)"
                shouldRequestMic = false
            },
            onPermanentlyDenied = {
                result = "❌ 마이크 권한이 영구적으로 거부되었습니다 (설정 필요)"
                shouldRequestMic = false
            }
        )
    }
    
    // 위치 권한 요청
    if (shouldRequestLocation) {
        RequestSinglePermission(
            permission = PermissionConstants.ACCESS_FINE_LOCATION,
            onGranted = {
                result = "✅ 위치 권한이 승인되었습니다!"
                shouldRequestLocation = false
            },
            onDenied = {
                result = "⚠️ 위치 권한이 거부되었습니다 (재요청 가능)"
                shouldRequestLocation = false
            },
            onPermanentlyDenied = {
                result = "❌ 위치 권한이 영구적으로 거부되었습니다 (설정 필요)"
                shouldRequestLocation = false
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "단일 권한 요청 테스트",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            PermissionCard(
                title = "카메라 권한",
                description = "카메라 권한을 요청합니다",
                buttonText = "카메라 권한 요청",
                onClick = { shouldRequestCamera = true },
                enabled = !shouldRequestCamera
            )
        }
        
        item {
            PermissionCard(
                title = "마이크 권한",
                description = "마이크 권한을 요청합니다",
                buttonText = "마이크 권한 요청",
                onClick = { shouldRequestMic = true },
                enabled = !shouldRequestMic
            )
        }
        
        item {
            PermissionCard(
                title = "위치 권한",
                description = "정확한 위치 권한을 요청합니다",
                buttonText = "위치 권한 요청",
                onClick = { shouldRequestLocation = true },
                enabled = !shouldRequestLocation
            )
        }
        
        item {
            ResultCard(result = result)
        }
    }
}

@Composable
fun MultiplePermissionTab() {
    var shouldRequestMedia by remember { mutableStateOf(false) }
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var shouldRequestAll by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("결과가 여기에 표시됩니다") }
    
    // 미디어 권한 요청 (카메라 + 마이크)
    if (shouldRequestMedia) {
        RequestMultiplePermissions(
            permissions = PermissionConstants.MEDIA_GROUP,
            onAllGranted = {
                result = "🎉 모든 미디어 권한이 승인되었습니다!"
                shouldRequestMedia = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 일부 권한만 승인됨\n✅ 승인: ${grantedNames.joinToString(", ")}\n❌ 거부: ${deniedNames.joinToString(", ")}"
                shouldRequestMedia = false
            },
            onAllDenied = {
                result = "❌ 모든 미디어 권한이 거부되었습니다"
                shouldRequestMedia = false
            }
        )
    }
    
    // 위치 권한 요청 (정확한 + 대략적인)
    if (shouldRequestLocation) {
        RequestMultiplePermissions(
            permissions = PermissionConstants.LOCATION_GROUP,
            onAllGranted = {
                result = "🎉 모든 위치 권한이 승인되었습니다!"
                shouldRequestLocation = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 일부 권한만 승인됨\n✅ 승인: ${grantedNames.joinToString(", ")}\n❌ 거부: ${deniedNames.joinToString(", ")}"
                shouldRequestLocation = false
            },
            onAllDenied = {
                result = "❌ 모든 위치 권한이 거부되었습니다"
                shouldRequestLocation = false
            }
        )
    }
    
    // 모든 권한 요청
    if (shouldRequestAll) {
        RequestMultiplePermissions(
            permissions = arrayOf(
                PermissionConstants.CAMERA,
                PermissionConstants.RECORD_AUDIO,
                PermissionConstants.ACCESS_FINE_LOCATION,
                PermissionConstants.READ_CONTACTS
            ),
            onAllGranted = {
                result = "🎉 모든 권한이 승인되었습니다!"
                shouldRequestAll = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 일부 권한만 승인됨\n✅ 승인: ${grantedNames.joinToString(", ")}\n❌ 거부: ${deniedNames.joinToString(", ")}"
                shouldRequestAll = false
            },
            onAllDenied = {
                result = "❌ 모든 권한이 거부되었습니다"
                shouldRequestAll = false
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "다중 권한 요청 테스트",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            PermissionCard(
                title = "미디어 권한 그룹",
                description = "카메라 + 마이크 권한을 함께 요청합니다",
                buttonText = "미디어 권한 요청",
                onClick = { shouldRequestMedia = true },
                enabled = !shouldRequestMedia
            )
        }
        
        item {
            PermissionCard(
                title = "위치 권한 그룹",
                description = "정확한 위치 + 대략적인 위치 권한을 함께 요청합니다",
                buttonText = "위치 권한 요청",
                onClick = { shouldRequestLocation = true },
                enabled = !shouldRequestLocation
            )
        }
        
        item {
            PermissionCard(
                title = "모든 권한",
                description = "카메라, 마이크, 위치, 연락처 권한을 모두 요청합니다",
                buttonText = "모든 권한 요청",
                onClick = { shouldRequestAll = true },
                enabled = !shouldRequestAll
            )
        }
        
        item {
            ResultCard(result = result)
        }
    }
}

@Composable
fun SequencePermissionTab() {
    var shouldRequestBasic by remember { mutableStateOf(false) }
    var shouldRequestAll by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("결과가 여기에 표시됩니다") }
    
    // 기본 순차 권한 요청
    if (shouldRequestBasic) {
        RequestSequencePermissions(
            permissions = PermissionConstants.MEDIA_GROUP,
            onProgress = { current, index, total ->
                val permissionName = PermissionUtils.getPermissionName(current)
                result = "📋 진행 중: $permissionName (${index + 1}/$total)"
            },
            onAllGranted = {
                result = "🎉 모든 순차 권한이 승인되었습니다!"
                shouldRequestBasic = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 순차 권한 중 일부 승인\n✅ 승인: ${grantedNames.joinToString(", ")}\n❌ 거부: ${deniedNames.joinToString(", ")}"
                shouldRequestBasic = false
            },
            onAllDenied = {
                result = "❌ 모든 순차 권한이 거부되었습니다"
                shouldRequestBasic = false
            }
        )
    }
    
    // 전체 순차 권한 요청  
    if (shouldRequestAll) {
        RequestSequencePermissions(
            permissions = arrayOf(
                PermissionConstants.CAMERA,
                PermissionConstants.RECORD_AUDIO,
                PermissionConstants.ACCESS_FINE_LOCATION,
                PermissionConstants.READ_CONTACTS
            ),
            onProgress = { current, index, total ->
                val permissionName = PermissionUtils.getPermissionName(current)
                result = "📋 진행 중: $permissionName (${index + 1}/$total)"
            },
            onAllGranted = {
                result = "🎉 모든 순차 권한이 승인되었습니다!"
                shouldRequestAll = false
            },
            onSomeGranted = { granted, denied ->
                val grantedNames = granted.map { PermissionUtils.getPermissionName(it) }
                val deniedNames = denied.map { PermissionUtils.getPermissionName(it) }
                result = "⚠️ 순차 권한 중 일부 승인\n✅ 승인: ${grantedNames.joinToString(", ")}\n❌ 거부: ${deniedNames.joinToString(", ")}"
                shouldRequestAll = false
            },
            onAllDenied = {
                result = "❌ 모든 순차 권한이 거부되었습니다"
                shouldRequestAll = false
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "순차 권한 요청 테스트",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            PermissionCard(
                title = "기본 순차 권한",
                description = "카메라 → 마이크 순서로 하나씩 요청합니다",
                buttonText = "기본 순차 권한 요청",
                onClick = { shouldRequestBasic = true },
                enabled = !shouldRequestBasic
            )
        }
        
        item {
            PermissionCard(
                title = "전체 순차 권한",
                description = "카메라 → 마이크 → 위치 → 연락처 순서로 하나씩 요청합니다",
                buttonText = "전체 순차 권한 요청",
                onClick = { shouldRequestAll = true },
                enabled = !shouldRequestAll
            )
        }
        
        item {
            ResultCard(result = result)
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun ResultCard(result: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "결과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun DenialCallbackTab() {
    var shouldRequestWithCallback by remember { mutableStateOf(false) }
    var shouldRequestWithInterface by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("결과가 여기에 표시됩니다") }
    var denialLog by remember { mutableStateOf("") }
    
    // 향상된 콜백 방식으로 권한 요청
    if (shouldRequestWithCallback) {
        RequestSinglePermissionAdvanced(
            permission = PermissionConstants.CAMERA,
            onGranted = {
                result = "✅ 카메라 권한이 승인되었습니다!"
                denialLog = ""
                shouldRequestWithCallback = false
            },
            denialCallback = object : PermissionDenialCallback {
                override fun onFirstTimeDenied(permission: String) {
                    val permissionName = PermissionUtils.getPermissionName(permission)
                    denialLog += "🔸 첫 번째 거부: $permissionName\n"
                    result = "⚠️ 첫 번째 거부 - 다시 시도해보세요"
                }
                
                override fun onSecondTimeDenied(permission: String) {
                    val permissionName = PermissionUtils.getPermissionName(permission)
                    denialLog += "🔹 두 번째 거부: $permissionName\n"
                    result = "⚠️ 두 번째 거부 - 설정 안내가 필요할 수 있습니다"
                }
                
                override fun onPermanentlyDenied(permission: String) {
                    val permissionName = PermissionUtils.getPermissionName(permission)
                    denialLog += "❌ 영구 거부: $permissionName\n"
                    result = "❌ 영구 거부 - 설정에서 직접 변경해야 합니다"
                }
                
                override fun onDenialWithInfo(denialInfo: PermissionDenialInfo) {
                    val permissionName = PermissionUtils.getPermissionName(denialInfo.permission)
                    denialLog += "📊 거부 정보: $permissionName (${denialInfo.denialCount}번째, ${denialInfo.denialType})\n"
                    shouldRequestWithCallback = false
                }
            }
        )
    }
    
    // 인터페이스 방식으로 권한 요청
    if (shouldRequestWithInterface) {
        RequestSinglePermissionWithCallbacks(
            permission = PermissionConstants.RECORD_AUDIO,
            callbacks = object : PermissionCallbacks {
                override fun onGranted() {
                    result = "✅ 마이크 권한이 승인되었습니다!"
                    denialLog = ""
                    shouldRequestWithInterface = false
                }
                
                override fun onDenied(denialInfo: PermissionDenialInfo) {
                    val permissionName = PermissionUtils.getPermissionName(denialInfo.permission)
                    
                    when (denialInfo.denialType) {
                        PermissionDenialType.FIRST_TIME_DENIED -> {
                            denialLog += "🔸 첫 번째 거부: $permissionName (${denialInfo.denialCount}번째)\n"
                            result = "⚠️ 첫 번째 거부 - 앱의 기능을 위해 필요한 권한입니다"
                        }
                        PermissionDenialType.SECOND_TIME_DENIED -> {
                            denialLog += "🔹 두 번째 거부: $permissionName (${denialInfo.denialCount}번째)\n"
                            result = "⚠️ 두 번째 거부 - 이 권한이 없으면 일부 기능을 사용할 수 없습니다"
                        }
                        PermissionDenialType.PERMANENTLY_DENIED -> {
                            denialLog += "❌ 영구 거부: $permissionName (${denialInfo.denialCount}번째)\n"
                            result = "❌ 영구 거부 - 설정 > 앱 > 권한에서 직접 허용해주세요"
                        }
                    }
                    shouldRequestWithInterface = false
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "권한 취소 콜백 테스트",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Text(
                text = "1번 취소와 2번 취소, 영구 거부를 구분하여 처리하는 방법을 테스트합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        item {
            PermissionCard(
                title = "향상된 콜백 방식",
                description = "PermissionDenialCallback을 사용하여 거부 상황별로 다른 처리가 가능합니다.\n테스트를 위해 카메라 권한을 여러 번 거부해보세요.",
                buttonText = "카메라 권한 요청 (향상된 콜백)",
                onClick = { shouldRequestWithCallback = true },
                enabled = !shouldRequestWithCallback
            )
        }
        
        item {
            PermissionCard(
                title = "인터페이스 방식",
                description = "PermissionCallbacks을 사용하여 통합된 콜백으로 모든 정보를 받을 수 있습니다.\n테스트를 위해 마이크 권한을 여러 번 거부해보세요.",
                buttonText = "마이크 권한 요청 (인터페이스)",
                onClick = { shouldRequestWithInterface = true },
                enabled = !shouldRequestWithInterface
            )
        }
        
        item {
            ResultCard(result = result)
        }
        
        if (denialLog.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "거부 로그",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = denialLog.trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { denialLog = "" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("로그 지우기")
                            }
                            Button(
                                onClick = { 
                                    PermissionUtils.resetDenialCount(PermissionConstants.CAMERA)
                                    PermissionUtils.resetDenialCount(PermissionConstants.RECORD_AUDIO)
                                    denialLog += "🔄 거부 카운트 초기화됨\n"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text("카운트 초기화")
                            }
                        }
                    }
                }
            }
        }
    }
}