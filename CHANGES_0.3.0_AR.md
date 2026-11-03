
# تغييرات Luz Builder 0.3.0

## الملفات المعدّلة

.github/workflows/build-apk.yml
BUILD_ENGINE_ROADMAP.md
ENGINE_DESIGN.md
README.md
README_AR.md
app/build.gradle
app/src/main/java/com/luzbuild/studio/BuildProfileDetector.java
app/src/main/java/com/luzbuild/studio/MainActivity.java
app/src/main/java/com/luzbuild/studio/ProjectActivity.java
app/src/main/java/com/luzbuild/studio/Ui.java

## الملفات الجديدة

app/src/main/java/com/luzbuild/studio/GlassFileIconView.java
docs/MIXED_ANDROID_PROJECT_GUIDE.md

## ما تغيّر
- إصلاح التباعد الأفقي بين أزرار الاستيراد، أزرار المشروع، وأزرار إضافة ملف/مجلد/تصدير.
- إضافة خلفيات زجاجية متدرجة وحدود خفيفة للأزرار والبطاقات.
- إضافة GlassFileIconView لرسم أيقونات مجلدات وملفات بصرياً حسب الامتداد.
- تطوير كاشف المشروع ليقرأ مجلدات المشروع بشكل محدود وآمن ويجمع لغات متعددة بدلاً من اختيار أول ملف إعداد فقط.
- إعطاء مشروع Android/Gradle أولوية كمخرج APK عند وجود مكونات Node/Python/C/C++ معه.
- إضافة تقرير يشرح متطلبات دمج Java/Kotlin وTypeScript/JavaScript وPython وC/C++ وRust وGo وغيرها.
- تحديث رقم الإصدار إلى 0.3.0.

## ملفات لم تتغير
لم نعدّل EditorActivity.java أو CodeEditorView.java أو CodeSyntaxHighlighter.java أو ProjectStore.java أو BuildProgressPanel.java في هذا الإصدار.

## حدود الإصدار
هذا الإصدار يحسّن الواجهة ويحلل المشاريع المختلطة، لكنه لا يضم بعد محرك تشغيل محلياً أو أدوات JDK/Gradle/Android SDK/NDK/Node/Python ولا ينفذ بناء APK لمشروع مستورد من داخل الهاتف. GitHub Actions يبني تطبيق Luz Builder نفسه فقط.
