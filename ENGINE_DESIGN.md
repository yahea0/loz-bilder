# Local Build Engine — Design Notes

## Goal

Run real Gradle Android builds on-device without requiring the user to install or open Termux. Toolchain payloads are installed and managed by Luz Builder itself.

## Proposed execution model

- A dedicated BuildEnvironmentManager owns the app-private toolchain directory.
- The first-run setup downloads a versioned ARM64 rootfs/toolchain archive over HTTPS.
- The app verifies the archive digest/signature before extraction.
- A small native execution bridge launches the rootfs environment using an Android-compatible PRoot build.
- The command runs from the imported project's root, for example:
  ./gradlew --offline assembleDebug
  when the wrapper and dependencies are cached, or:
  gradle --no-daemon assembleDebug
  when the project uses the provisioned Gradle distribution.
- Standard output and error are streamed to a build console; the process exit code determines success or failure.
- The output APK is discovered under the module's build/outputs/apk/ directory and passed to Android's package installer using a content:// URI.

## Important constraints

1. A full JDK + Gradle + Android SDK toolchain is large. The app's initial install should stay small; toolchain setup is a separate one-time in-app download.
2. Gradle is not a normal Android app library. The build must run in a compatible Linux userland with Android-specific binaries, not by calling an ordinary desktop Gradle executable directly from the app process.
3. Projects can use different Android Gradle Plugin, Gradle, Kotlin, NDK, and Maven dependency versions. Compatibility detection and clear diagnostics are required.
4. Offline builds work only after the needed plugins and dependencies have been cached. A project with new external dependencies still needs network access once.
5. The native launcher and toolchain packaging must be tested on the actual phone architecture and Android version. This source prototype has not yet done that.
6. Signing configuration must be stored securely; passwords should never appear in build logs.

## Build stages

- Stage A: project importer, local file explorer, text editor, ZIP export (implemented in 0.1.0).
- Stage B: toolchain installer and integrity checks.
- Stage C: native execution bridge and build log streaming.
- Stage D: APK discovery, install/update handoff, build variants.
- Stage E: keystore management, incremental build tuning, dependency diagnostics.
