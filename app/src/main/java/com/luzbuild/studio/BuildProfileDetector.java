
package com.luzbuild.studio;

import java.io.File;

public final class BuildProfileDetector {
    public static class Profile {
        public final String id, label, toolchain, command;

        Profile(String id, String label, String toolchain, String command) {
            this.id = id;
            this.label = label;
            this.toolchain = toolchain;
            this.command = command;
        }
    }

    private BuildProfileDetector() {}

    public static Profile detect(File root) {
        if (exists(root, "pubspec.yaml"))
            return new Profile("flutter", "Flutter / Dart",
                "Flutter SDK + Dart + Android SDK عند بناء APK",
                "flutter build apk");

        if (exists(root, "gradlew") || exists(root, "build.gradle") || exists(root, "build.gradle.kts"))
            return new Profile("gradle", "Gradle / Android أو JVM",
                "JDK + Gradle + Android SDK إذا كان المشروع Android",
                "./gradlew assembleDebug أو gradle build");

        if (exists(root, "pom.xml"))
            return new Profile("maven", "Java / Maven",
                "JDK + Maven", "mvn package");

        if (exists(root, "package.json"))
            return new Profile("node", "Node.js / JavaScript / TypeScript",
                "Node.js + npm أو pnpm أو yarn", "npm run build");

        if (exists(root, "Cargo.toml"))
            return new Profile("rust", "Rust",
                "Rustup + Cargo + target toolchain", "cargo build --release");

        if (exists(root, "go.mod"))
            return new Profile("go", "Go",
                "Go toolchain", "go build ./...");

        if (exists(root, "CMakeLists.txt"))
            return new Profile("cmake", "C / C++ عبر CMake",
                "CMake + Ninja/Make + Clang/GCC + NDK عند Android",
                "cmake --build build");

        if (exists(root, "Makefile") || exists(root, "makefile"))
            return new Profile("make", "مشروع Make",
                "Make + المترجم المطلوب", "make");

        if (exists(root, "pyproject.toml") || exists(root, "requirements.txt") || exists(root, "setup.py"))
            return new Profile("python", "Python",
                "Python + pip والاعتماديات", "python -m build أو أمر المشروع");

        if (exists(root, "*.csproj") || exists(root, "*.sln"))
            return new Profile("dotnet", ".NET / C#",
                ".NET SDK", "dotnet build");

        if (exists(root, "composer.json"))
            return new Profile("php", "PHP / Composer",
                "PHP + Composer", "composer install");

        if (exists(root, "Gemfile"))
            return new Profile("ruby", "Ruby",
                "Ruby + Bundler", "bundle install");

        return new Profile("generic", "مشروع متعدد اللغات / غير محدد",
            "تُحدد بيئة البناء حسب المترجم وملف إعداد المشروع",
            "يُحدد حسب المشروع");
    }

    private static boolean exists(File root, String name) {
        if (name.startsWith("*.")) {
            File[] files = root.listFiles();
            if (files == null) return false;

            String suffix = name.substring(1);

            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(suffix))
                    return true;
            }

            return false;
        }

        return new File(root, name).exists();
    }
}
