# ColorRush ProGuard rules
# Add project-specific rules here.

# Keep Room entity and DAO class names (Room uses reflection)
-keep class com.gentleai.colorrush.data.local.db.entity.** { *; }
-keep class com.gentleai.colorrush.data.local.db.dao.** { *; }

# Keep Hilt-generated classes
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# Keep domain models (used in serialization / reflection)
-keep class com.gentleai.colorrush.domain.model.** { *; }

# Keep enums (Room stores them by name; ProGuard must not rename them)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes *Annotation*, InnerClasses
