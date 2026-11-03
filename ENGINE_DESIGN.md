
# Luz Builder — Build Engine Architecture

## Product goal
A mobile project workspace with an extensible, on-device build engine. A mixed-language Android repository should be treated as one Android Gradle build graph whose tasks produce and package language-specific outputs.

## Mixed project model
- Android Gradle Plugin owns the final APK/AAB packaging graph.
- Java and Kotlin compile through normal Android source sets and Kotlin Gradle Plugin tasks.
- C/C++ compile through Android NDK plus CMake or ndk-build; Gradle packages ABI-specific shared libraries.
- Python is embedded through a supported Android integration such as Chaquopy, or another explicit Python runtime strategy.
- TypeScript/JavaScript are transpiled/bundled by Node tasks, React Native, or an explicit WebView asset pipeline; the generated output must be wired into Gradle before packaging.
- Rust and Go require Android-target cross-compilation for each ABI and a defined JNI/FFI integration. Their source files are not automatically APK components.
- Server-side languages such as PHP/Ruby normally belong on a backend unless the app intentionally embeds a compatible runtime.

## Engine layers
1. ProjectScanner: bounded recursive scan, ignored generated directories, detects all languages and build manifests (implemented as heuristics in 0.3.0).
2. BuildPlanner: selects Android/Flutter as output orchestrator and lists per-component toolchains and Gradle integration requirements (initial report in 0.3.0).
3. ToolchainManager: versioned downloads, checksums/signatures, disk-space checks, resumable download, licenses and per-project toolchain selection (not implemented).
4. RuntimeHost: Android-compatible process launcher / ARM64 Linux userland integration, process isolation, environment setup and cancellation (not implemented).
5. BuildAdapters: Gradle/AGP, Flutter, Node, Python, CMake/NDK, Cargo, Go and .NET-specific task adapters (not implemented).
6. BuildEventParser: streams stdout/stderr and task events; only reports measurable percentage when a reliable total is available (not implemented).
7. ArtifactManager: locate APK/AAB, validate package metadata, hand APK to Android Package Installer through content:// URI and handle signing keystores (not implemented).

## Honest progress policy
A percentage is displayed only when a build adapter can measure completed work against a known total. Otherwise show an indeterminate progress bar and live logs. Never simulate 11%, 25%, or 100%.

## Safety and reliability
- Never execute build scripts automatically on import. Require explicit user confirmation.
- Warn before running untrusted Gradle, npm, pip, shell, or CMake scripts.
- Use app-private toolchain storage, checksum verification, archive path validation and storage quotas.
- Do not place signing passwords in logs or plain-text project files.
- Stop child processes on cancellation and surface exit code plus relevant log tail.

## Current implementation status
0.3.0 includes UI spacing fixes, glass file/folder icons, broader project-language scanning and a mixed-project integration report. The on-device compiler toolchains, runtime host, actual build adapters and APK installation pipeline are still future work. GitHub Actions currently builds Luz Builder itself only.
