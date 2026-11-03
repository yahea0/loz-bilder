
# Luz Builder — Local Build Engine Design

The product target is an extensible on-device build studio. It is not technically correct to promise a universal compiler: every ecosystem has its own toolchain and packaging workflow.

## Architecture target
- ToolchainManager: download/version/cache signed or hash-pinned ARM64 tool bundles.
- RuntimeEnvironment: launch compatible Linux userland tools from app-private storage.
- BuildAdapter: one adapter per ecosystem (Gradle, Maven, Node, Python, CMake/Make, Cargo, Go, Flutter, .NET, PHP, Ruby).
- BuildSession: cancellable process, stdout/stderr streaming, exit status, artifact discovery.
- ProgressProvider: report only real task/byte progress; use indeterminate progress where the ecosystem does not expose a reliable percentage.
- ArtifactInstaller: hand APK/AAB output to Android's package installer using a content URI.

## Android constraints
- Desktop Linux executables are not automatically executable on Android. A tested Android-compatible runtime/PRoot bridge and ARM64 toolchain packaging are required.
- Android APKs require Android SDK build tools and often NDK; a generic language compiler does not automatically produce an APK.
- Native toolchains and SDKs are large. The base APK should stay small; install toolchains inside the app on demand.
- Offline builds only work after all required plugins and dependencies are cached.
- The app must not report build success or a percentage unless a real process/event source provides it.

## Implemented in 0.2.0
Project management, import/export, syntax-aware text editing, basic language highlighting, line numbers, search/replace, undo/redo, comment/indent helpers, save-and-return, and project deletion confirmation. Build ecosystem detection is a preliminary heuristic only.

## Not implemented yet
The actual local toolchain download/runtime/adapter execution, live build progress from a running process, APK artifact discovery/signing, and install/update pipeline.
