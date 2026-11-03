
package com.luzbuild.studio;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Read-only project analyzer. It detects all ecosystems in a mixed repository
 * instead of stopping at the first manifest file. Detection is not compilation:
 * a real Android APK still requires a configured Android Gradle project and
 * the matching toolchains/plugins for its embedded components.
 */
public final class BuildProfileDetector {
    private static final int MAX_FILES = 20000;
    private static final int MAX_DEPTH = 12;

    public static final class Profile {
        public final String id;
        public final String label;
        public final String toolchain;
        public final String command;
        public final String outputLabel;
        public final String summary;
        public final String languagesText;
        public final String componentsText;
        public final String integrationNotes;
        public final String warnings;
        public final boolean canTargetAndroidApk;

        Profile(String id, String label, String toolchain, String command,
                String outputLabel, String summary, String languagesText,
                String componentsText, String integrationNotes, String warnings,
                boolean canTargetAndroidApk) {
            this.id = id;
            this.label = label;
            this.toolchain = toolchain;
            this.command = command;
            this.outputLabel = outputLabel;
            this.summary = summary;
            this.languagesText = languagesText;
            this.componentsText = componentsText;
            this.integrationNotes = integrationNotes;
            this.warnings = warnings;
            this.canTargetAndroidApk = canTargetAndroidApk;
        }
    }

    private static final class Scan {
        final Set<String> files = new LinkedHashSet<>();
        final Set<String> languages = new LinkedHashSet<>();
        final List<String> manifests = new ArrayList<>();
        int visited;
        boolean truncated;
    }

    private BuildProfileDetector() {}

    public static Profile detect(File root) {
        if (root == null || !root.isDirectory()) {
            return generic("المسار غير صالح", "تعذر فحص المشروع.");
        }

        Scan scan = new Scan();
        scanTree(root, root, 0, scan);

        boolean hasGradle = hasRootGradle(root);
        boolean hasAndroidManifest = scan.files.contains("AndroidManifest.xml");
        boolean hasAndroidPluginHint = containsGradleAndroidHint(root);
        boolean android = hasGradle && (hasAndroidManifest || hasAndroidPluginHint);
        boolean flutter = scan.files.contains("pubspec.yaml") && new File(root, "android").isDirectory();
        boolean maven = scan.files.contains("pom.xml");
        boolean node = scan.files.contains("package.json") || scan.languages.contains("JavaScript") || scan.languages.contains("TypeScript");
        boolean python = scan.files.contains("pyproject.toml") || scan.files.contains("requirements.txt") || scan.files.contains("setup.py") || scan.languages.contains("Python");
        boolean cpp = scan.files.contains("CMakeLists.txt") || scan.files.contains("Android.mk") || scan.languages.contains("C") || scan.languages.contains("C++");
        boolean rust = scan.files.contains("Cargo.toml") || scan.languages.contains("Rust");
        boolean go = scan.files.contains("go.mod") || scan.languages.contains("Go");
        boolean dotnet = hasSuffix(scan.files, ".csproj") || hasSuffix(scan.files, ".sln");
        boolean php = scan.files.contains("composer.json") || scan.languages.contains("PHP");
        boolean ruby = scan.files.contains("Gemfile") || scan.languages.contains("Ruby");

        List<String> components = new ArrayList<>();
        if (android) components.add("مشروع Android / Gradle");
        else if (flutter) components.add("مشروع Flutter");
        if (scan.languages.contains("Java") || scan.languages.contains("Kotlin")) components.add("مصادر Java / Kotlin");
        if (node) components.add("مصادر أو حزم Node.js / JavaScript / TypeScript");
        if (python) components.add("مكوّنات Python");
        if (cpp) components.add("كود C/C++ أو CMake/NDK");
        if (rust) components.add("مكوّنات Rust");
        if (go) components.add("مكوّنات Go");
        if (maven) components.add("وحدة Java / Maven");
        if (dotnet) components.add("مكوّنات .NET / C#");
        if (php) components.add("مكوّنات PHP");
        if (ruby) components.add("مكوّنات Ruby");

        List<String> hints = new ArrayList<>();
        if (android) {
            hints.add("Java وKotlin: تُبني ضمن Android Gradle Plugin.");
            if (node) hints.add("JavaScript/TypeScript: يلزم Node.js ومهمة Gradle لتثبيت الحزم وتجميع المخرجات إلى assets أو مصادر مولّدة؛ وجود package.json وحده لا يضيفها إلى APK.");
            if (python) hints.add("Python: يحتاج تكاملاً مثل Chaquopy داخل وحدة Android واحدة، مع وضع المصادر والاعتماديات وفق إعداد المشروع.");
            if (cpp) hints.add("C/C++: يحتاج Android NDK وCMake أو ndk-build وربط externalNativeBuild.");
            if (rust) hints.add("Rust: يحتاج cross-compilation لكل ABI وإنتاج مكتبات Android .so وربطها عبر JNI أو طبقة مناسبة.");
            if (go) hints.add("Go: يحتاج بناء مكتبات/مخرجات تستهدف Android ABI ودمجها كـ native libraries أو عبر gomobile عند ملاءمته.");
            if (dotnet) hints.add("C#: يحتاج مسار .NET for Android متوافقاً؛ لا يُدمج تلقائياً في مشروع Android Gradle عادي.");
            if (php || ruby) hints.add("PHP/Ruby: عادةً تُستخدم للخادم. لتشغيلها داخل التطبيق يلزم runtime مضمّن أو نقل المنطق إلى خدمة خلفية.");
        } else if (flutter) {
            hints.add("Flutter يُنتج APK عبر Flutter SDK؛ دمج Java/Kotlin أو C/C++ يحتاج إضافات Android المناسبة.");
            if (python || node || rust || go) hints.add("المكونات الأخرى تحتاج Plugins أو مكتبات Android متوافقة؛ اكتشافها لا يعني أنها مرتبطة بعملية Flutter.");
        } else {
            hints.add("لم يُكتشف جذر Android/Gradle واضح. يلزم إنشاء أو اختيار وحدة Android رئيسية قبل إخراج APK.");
        }

        List<String> warnings = new ArrayList<>();
        if (scan.truncated) warnings.add("توقف الفحص عند حد الأمان (" + MAX_FILES + " ملفاً أو عمق " + MAX_DEPTH + "); قد لا تكون كل اللغات قد اكتُشفت.");
        if (hasSuffix(scan.files, ".swift")) warnings.add("Swift ليس هدفاً أصلياً مدعوماً في Android Gradle؛ يحتاج إعادة تصميم أو نقل منطق إلى مكتبة مشتركة.");
        if (hasSuffix(scan.files, ".sh")) warnings.add("ملفات Shell تُعد سكربتات مساعدة وليست مكوّناً يُعبأ تلقائياً داخل APK.");
        if (!android && !flutter) warnings.add("هذا التحليل لا ينفّذ build؛ لا يوجد مخرج APK مضمون لهذا المشروع حتى تُجهّز وحدة Android.");

        String id;
        String label;
        String toolchain;
        String command;
        String output;
        if (android) {
            id = "android-mixed-gradle";
            label = "Android Gradle — مشروع متعدد اللغات";
            toolchain = "JDK + Gradle/AGP + Android SDK" + (cpp ? " + NDK/CMake" : "") + (node ? " + Node.js" : "") + (python ? " + Python Android plugin" : "");
            command = "./gradlew assembleDebug";
            output = "APK عبر Android Gradle";
        } else if (flutter) {
            id = "flutter-android";
            label = "Flutter / Dart";
            toolchain = "Flutter SDK + Dart + Android SDK";
            command = "flutter build apk";
            output = "APK عبر Flutter";
        } else if (maven) {
            id = "maven"; label = "Java / Maven"; toolchain = "JDK + Maven"; command = "mvn package"; output = "مخرجات Maven؛ ليس APK تلقائياً";
        } else if (node) {
            id = "node"; label = "Node.js / JavaScript / TypeScript"; toolchain = "Node.js + npm/pnpm/yarn"; command = "npm run build"; output = "مخرجات Node؛ يلزم Android wrapper لإنتاج APK";
        } else if (python) {
            id = "python"; label = "Python"; toolchain = "Python + pip + backend"; command = "python -m build"; output = "حزمة Python؛ ليس APK تلقائياً";
        } else if (cpp) {
            id = "native"; label = "C / C++"; toolchain = "Clang/GCC + CMake/Make"; command = "cmake --build build"; output = "مكتبة أو ملف تنفيذي؛ يلزم Android wrapper لإنتاج APK";
        } else {
            Profile g = generic("مشروع متعدد اللغات / غير محدد", "لم يُكتشف نظام بناء رئيسي معروف.");
            return new Profile(g.id, g.label, g.toolchain, g.command, g.outputLabel, g.summary,
                join(scan.languages), join(components), join(hints), join(warnings), false);
        }

        String summary = components.isEmpty() ? "لم تُكتشف مكونات" : components.size() + " أنواع مكونات";
        return new Profile(id, label, toolchain, command, output, summary,
            join(scan.languages), join(components), join(hints), join(warnings), android || flutter);
    }

    private static void scanTree(File root, File dir, int depth, Scan scan) {
        if (scan.truncated || depth > MAX_DEPTH || scan.visited >= MAX_FILES) {
            scan.truncated = true;
            return;
        }
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File f : children) {
            if (scan.visited++ >= MAX_FILES) { scan.truncated = true; return; }
            String name = f.getName();
            if (f.isDirectory()) {
                if (name.startsWith(".") || isIgnoredDirectory(name)) continue;
                scanTree(root, f, depth + 1, scan);
            } else {
                scan.files.add(name);
                detectLanguage(name, scan.languages);
            }
        }
    }

    private static boolean isIgnoredDirectory(String name) {
        return name.equals(".git") || name.equals(".gradle") || name.equals("build") ||
            name.equals("node_modules") || name.equals(".idea") || name.equals(".dart_tool") ||
            name.equals(".venv") || name.equals("venv") || name.equals("Pods") ||
            name.equals("target") || name.equals("dist") || name.equals("out");
    }

    private static void detectLanguage(String name, Set<String> out) {
        String n = name.toLowerCase(Locale.ROOT);
        if (n.endsWith(".java")) out.add("Java");
        else if (n.endsWith(".kt") || n.endsWith(".kts")) out.add("Kotlin");
        else if (n.endsWith(".ts") || n.endsWith(".tsx")) out.add("TypeScript");
        else if (n.endsWith(".js") || n.endsWith(".jsx") || n.endsWith(".mjs")) out.add("JavaScript");
        else if (n.endsWith(".py")) out.add("Python");
        else if (n.endsWith(".c") || n.endsWith(".h")) out.add("C");
        else if (n.endsWith(".cc") || n.endsWith(".cpp") || n.endsWith(".cxx") || n.endsWith(".hpp")) out.add("C++");
        else if (n.endsWith(".rs")) out.add("Rust");
        else if (n.endsWith(".go")) out.add("Go");
        else if (n.endsWith(".cs")) out.add("C#");
        else if (n.endsWith(".php")) out.add("PHP");
        else if (n.endsWith(".rb")) out.add("Ruby");
        else if (n.endsWith(".dart")) out.add("Dart");
        else if (n.endsWith(".swift")) out.add("Swift");
        else if (n.endsWith(".html") || n.endsWith(".htm")) out.add("HTML");
        else if (n.endsWith(".css") || n.endsWith(".scss")) out.add("CSS");
        else if (n.endsWith(".sql")) out.add("SQL");
    }

    private static boolean hasRootGradle(File root) {
        return new File(root, "settings.gradle").isFile() ||
            new File(root, "settings.gradle.kts").isFile() ||
            new File(root, "build.gradle").isFile() ||
            new File(root, "build.gradle.kts").isFile();
    }

    private static boolean containsGradleAndroidHint(File root) {
        File[] candidates = {new File(root, "build.gradle"), new File(root, "build.gradle.kts"),
            new File(root, "app/build.gradle"), new File(root, "app/build.gradle.kts")};
        for (File f : candidates) {
            if (!f.isFile() || f.length() > 2 * 1024 * 1024) continue;
            try {
                String text = new String(java.nio.file.Files.readAllBytes(f.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                if (text.contains("com.android.application") || text.contains("com.android.library")) return true;
            } catch (Exception ignored) { }
        }
        return false;
    }

    private static boolean hasSuffix(Set<String> files, String suffix) {
        for (String f : files) if (f.toLowerCase(Locale.ROOT).endsWith(suffix)) return true;
        return false;
    }

    private static String join(Iterable<String> values) {
        StringBuilder b = new StringBuilder();
        for (String value : values) {
            if (b.length() > 0) b.append("، ");
            b.append(value);
        }
        return b.toString();
    }

    private static Profile generic(String label, String warning) {
        return new Profile("generic", label, "حسب المشروع", "غير محدد",
            "لا يوجد APK معروف", "تحليل أولي", "", "", "", warning, false);
    }
}
