
# خارطة محرك البناء متعدد اللغات — Luz Builder 0.2.0

## ما هو موجود في 0.2.0
- محرر نصوص برمجية مع ترقيم أسطر، تمييز ألوان أساسي حسب امتداد الملف، بحث واستبدال، تراجع/إعادة، تعليق/إلغاء تعليق، مسافة بادئة، انتقال إلى سطر.
- حفظ آمن نسبياً عبر ملف مؤقت، ثم إغلاق المحرر والعودة للمشروع عند نجاح الحفظ.
- تأكيد قبل حذف مشروع كامل أو عنصر من داخله.
- كشف أولي لنوع المشروع من ملفات إعداد معروفة.

## حقيقة البناء متعدد اللغات
لا يوجد Compiler واحد قادر على بناء كل لغات البرمجة. يحتاج كل نوع مشروع إلى سلسلة أدوات خاصة به، كما أن إنشاء APK من Python أو JavaScript أو C++ ليس مجرد تشغيل مترجم؛ بل يحتاج طبقة تغليف/منصة Android مناسبة.

## محولات البناء التي ينبغي دعمها
| المحول | مؤشرات المشروع | الأدوات المطلوبة |
|---|---|---|
| Android / Gradle | settings.gradle, build.gradle | JDK, Gradle, Android SDK, Build Tools |
| Java / Maven | pom.xml | JDK, Maven |
| Node / TypeScript | package.json | Node.js, npm/pnpm/yarn |
| Python | pyproject.toml, requirements.txt | Python, pip, build backend |
| C / C++ | CMakeLists.txt, Makefile | Clang/GCC, CMake/Ninja/Make؛ وNDK لهدف Android |
| Rust | Cargo.toml | Rustup, Cargo, target standard library |
| Go | go.mod | Go toolchain |
| Flutter / Dart | pubspec.yaml | Flutter SDK, Dart, Android SDK عند APK |
| .NET | *.csproj, *.sln | .NET SDK |
| PHP / Ruby | composer.json / Gemfile | PHP+Composer / Ruby+Bundler |

## نسب التقدم الحقيقية
- تنزيل حزمة أدوات: يمكن حساب النسبة بدقة من bytesDownloaded / contentLength عند وجود Content-Length موثوق.
- بناء Gradle/Maven: يجب ربط أحداث المهام من أداة البناء أو تحليل سجل صريح؛ سطر BUILD SUCCESSFUL وحده لا يعطي نسبة تدريجية.
- الأدوات التي لا توفر أحداث تقدم: يعرض التطبيق شريطاً غير محدد (indeterminate) وسجل المخرجات، ولا يخترع نسبة 11% أو 80%.

## المراحل التالية قبل ادعاء أن البناء يعمل
1. ToolchainManager لتنزيل نسخ ARM64 محددة والتحقق من SHA-256.
2. بيئة Linux userland وآلية تشغيل متوافقة مع Android، مع اختبارها على ARM64.
3. BuildAdapter مستقل لكل نظام بناء، مع إظهار الأمر والبيئة والاعتماديات.
4. بث stdout/stderr وإدارة إلغاء البناء وحالة العملية.
5. ProgressProvider لكل Adapter؛ وإلا يبقى العرض غير محدد.
6. اكتشاف المخرجات وتوقيع APK/تسليمه إلى مثبت Android.

هذه النسخة لا تتضمن بعد محرك تنفيذ محلياً؛ زر البناء يعرض تشخيصاً صريحاً ولا يوهم المستخدم بأن عملية بناء بدأت.
