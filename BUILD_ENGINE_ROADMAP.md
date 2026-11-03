
# خارطة طريق محرك البناء — Luz Builder 0.3.0

## المرحلة الحالية
- تحسين توزيع الأزرار ومنع تلامس عناصر الصفوف.
- إضافة أيقونات زجاجية للملفات والمجلدات.
- تحليل عودي متعدد اللغات بدلاً من إيقاف الفحص عند أول ملف إعداد.
- تمييز مشروع Android/Gradle كهدف APK حتى عند وجود حزم JavaScript أو Python أو C/C++ معه.
- عرض متطلبات الربط لكل مكوّن مكتشف.

## هدف المشروع المختلط
المستخدم يستورد مستودعاً واحداً قد يحتوي على Java وKotlin وTypeScript وJavaScript وPython وC/C++ وغيرها. إذا كان المشروع يملك Android app module، يكون Gradle هو منسق البناء النهائي. كل لغة تُدمج بالطريقة التي تناسبها، وليس عن طريق محاولة تمرير جميع الملفات إلى مترجم واحد.

## محولات الدمج
- Java/Kotlin: Android Gradle Plugin وKotlin Gradle Plugin.
- TypeScript/JavaScript: Node.js task أو React Native/حزمة WebView، ويجب توصيل ناتج bundling إلى Gradle assets أو source sets.
- Python: Chaquopy أو runtime Android متوافق، مع ضبط ABI والاعتماديات.
- C/C++: NDK وCMake أو ndk-build وexternalNativeBuild.
- Rust/Go: cross-compile إلى Android ABI ثم ربط المكتبات عبر JNI/FFI.
- .NET: مسار .NET for Android مستقل، لا يُعامل كملف مصدر إضافي داخل AGP العادي.
- PHP/Ruby: عادةً خدمات backend، إلا إذا أضيف runtime مضمّن صراحةً.

## المراحل المتبقية لمحرك محلي حقيقي
1. ToolchainManager لتنزيل JDK وGradle وAndroid SDK/Build Tools وNDK/CMake وNode وحزم اللغات بحسب الحاجة، مع SHA-256 ومساحة تخزين ومتابعة التنزيل.
2. RuntimeHost متوافق مع Android/ARM64 لتشغيل الأدوات المحلية داخل مساحة التطبيق؛ لا يكفي استدعاء Gradle من Activity.
3. BuildAdapters تربط كل مكوّن بمهامه وتكشف الاعتماديات الناقصة قبل البناء.
4. BuildSession لتشغيل العملية وعرض stdout/stderr والإلغاء ورمز الخروج.
5. ProgressProvider يعرض نسبة فقط عندما تتوفر وحدة قياس موثوقة؛ وإلا يعرض شريطاً غير محدد وسجل البناء.
6. ArtifactManager لاكتشاف APK والتحقق من وجوده وتقديمه لمثبت Android عبر content URI وإدارة توقيع release.

## الأمان
- لا تشغّل ملفات Gradle أو npm أو pip أو shell تلقائياً عند استيراد ZIP.
- اطلب موافقة المستخدم قبل تنفيذ سكربتات مشروع غير موثوقة.
- تحقق من هاش حزم الأدوات، وتجنب المسارات الخارجة عن مجلد الاستخراج، وحدد مساحة الملفات المؤقتة.
- لا تخزن كلمات مرور keystore في ملفات نصية أو سجلات البناء.

## حدود 0.3.0
المحلل والواجهة يكتشفان المكونات ويقدمان خطة دمج. لم تُضمّن بعد أدوات البناء، أو runtime محلي، أو محولات تنفيذ، أو بناء APK للمشاريع المستوردة. GitHub Actions يبني APK الخاص بلوز بيلدر نفسه فقط.
