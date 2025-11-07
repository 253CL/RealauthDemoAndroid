# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# ========== Gson 基本规则 ==========

# 保持 Gson 相关的类不被混淆
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# 保持泛型类型信息
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# ========== 序列化/反序列化相关规则 ==========

# 方法一：保持所有实体类（简单但可能会增大包体积）
# -keep class com.yourpackage.model.** { *; }

# 方法二：精确保持使用 @SerializedName 的字段（推荐）
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保持被 @SerializedName 注解的字段和方法
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName *;
}

# ========== 反射相关规则 ==========

# 保持 Gson 使用的反射相关类
-keep class com.google.gson.reflect.** { *; }

# 保持 TypeToken 类，用于处理泛型
-keep class com.google.gson.TypeToken { *; }
-keep class * extends com.google.gson.TypeToken { *; }

# ========== 特定场景配置 ==========

# 如果你使用 GsonBuilder 的 excludeFieldsWithoutExposeAnnotation()
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}

# 保持自定义 TypeAdapter
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }

# 保持自定义 JsonSerializer 和 JsonDeserializer
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }

# ========== 针对 Kotlin 的额外规则 ==========
# 如果你使用 Kotlin

-keepclassmembers class **$Fields {
    public static *;
}

# 保持 Kotlin 数据类
-keepclassmembers class ** {
    public ** copy(**);
    public ** copy$default(**);
}
# ========== 保持 IdCardEntity 及相关实体类 ==========

# 保持整个实体类及其所有成员
-keep class com.chuanglan.alive.demo.entities.IdCardEntity { *; }
-keep class com.chuanglan.alive.demo.entities.IdCardEntity$* { *; }

# 或者保持整个 entities 包下的所有类
-keep class com.chuanglan.alive.demo.entities.** { *; }

# ========== 或者更精确的配置 ==========

# 只保持必要的字段和方法
-keep class com.chuanglan.alive.demo.entities.IdCardEntity {
    public *;
    private *;
}

-keep class com.chuanglan.alive.demo.entities.IdCardEntity$DataBean {
    public *;
    private *;
}

-keep class com.chuanglan.alive.demo.entities.IdCardEntity$DataBean$FrontBean {
    public *;
    private *;
}

-keep class com.chuanglan.alive.demo.entities.IdCardEntity$DataBean$BackBean {
    public *;
    private *;
}
-keep class com.chuanglan.sdk.** { *;}
-keep class com.chuanglan.alivedetected.** { *;}