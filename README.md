This is a Kotlin Multiplatform project targeting Web, Desktop (JVM).

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications. It contains
  several subfolders:
    - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name. For
      example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls. Similarly, if you want
      to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
      folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and
options:

- Desktop app:
    - Hot reload: `./gradlew :desktopApp:hotRun --auto`
    - Standard run: `./gradlew :desktopApp:run`
- Web app:
    - Wasm target (faster, modern browsers): `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
    - JS target (slower, supports older browsers): `./gradlew :webApp:jsBrowserDevelopmentRun`

### Packaging the desktop app

`jpackage` (which Compose Desktop drives under the hood) cannot cross-compile: each installer
format can only be produced on its own OS. Building on Linux gives you `.deb` / `.rpm`, building
on Windows gives you `.msi`, building on macOS gives you `.dmg`. Incompatible formats are skipped
automatically on the current platform.

- Native installer for the current OS: `./gradlew :desktopApp:packageDistributionForCurrentOS`
- Linux specifically: `./gradlew :desktopApp:packageDeb` / `./gradlew :desktopApp:packageRpm`
  (needs `fakeroot` + `dpkg` for `.deb`, and `rpmbuild` for `.rpm`)
- Portable build with a bundled JRE — no installer, no Java required on the target machine:
  `./gradlew :desktopApp:createDistributable`, then run
  `desktopApp/build/compose/binaries/main/app/cg.creamgod.consoleapp/bin/cg.creamgod.consoleapp`
- Try the packaged build without installing it: `./gradlew :desktopApp:runDistributable`

Override the version with `-PappVersion=1.2.3`; it must be `x.y.z`, since `jpackage` rejects
anything else for `.msi` and `.dmg`.

### Releasing

`.github/workflows/release.yml` builds all three platforms in parallel (Linux x64, Windows x64,
macOS arm64 and x64) and uploads the installers plus portable archives to a GitHub Release.

- Push a tag: `git tag v1.0.0 && git push origin v1.0.0` — publishes the Release directly.
- Or run the **Release Desktop Packages** workflow manually from the Actions tab and type the
  version; it creates a draft Release by default so you can check the artifacts first.

Release notes come from `.github/release-notes-template.md` (`{{VERSION}}` is substituted).
The artifacts are unsigned, so Windows SmartScreen and macOS Gatekeeper will warn about them.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Desktop tests: `./gradlew :shared:jvmTest`
- Web tests:
    - Wasm target: `./gradlew :shared:wasmJsTest`
    - JS target: `./gradlew :shared:jsTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web). If you face any issues, please report them
on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).