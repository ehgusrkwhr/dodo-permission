# Permission Manager Library Consumer Rules
# Keep all public classes and methods
-keep public class com.dodo.permission.** { *; }

# Keep permission manager classes
-keep class com.dodo.permission.core.PermissionManager { *; }
-keep class com.dodo.permission.core.PermissionState { *; }
-keep class com.dodo.permission.core.PermissionResult { *; }

# Keep extension functions
-keep class com.dodo.permission.extensions.** { *; }