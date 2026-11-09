# دليل المسلم | Muslim Guide

Erthi | إرثي — Free & Open-Source Islamic Software
https://erthi.org · admin@erthi.org

Android foundation alpha with 11 navigable learning modules, localized module search, on-device favorites and learning goals, library review state and settings. Screens are implemented; reviewed religious lessons are not bundled yet. Learning goals are not completed lessons.

## Test status and scope
This is 0.1.0-alpha, for technical testing. No scholarly certification, production readiness or physical-device validation is claimed. Erthi is a personal free/open-source initiative; the GitHub organization is not asserted to be an independent legal entity. No legal HifzAI IP assignment is claimed.

- Kotlin / Jetpack Compose / Material 3 / ViewModel, Navigation and private local persistence.
- Arabic-first resources plus English, French, Turkish, Urdu, Indonesian, German and Spanish. AppCompat app-language settings recreate safely; RTL/LTR follows locale. Native-speaker review remains pending.
- Dark/light/system appearance, high contrast, large system fonts, ordered full-width controls and accessible labels. Real TalkBack testing remains outstanding.
- No Internet, microphone, location or broad storage permission; Android backup disabled. AndroidX may declare its app-local signature permission for protected dynamic receivers.
- [Privacy](PRIVACY.md), [sources](docs/CONTENT_REGISTRY.json), [notices](NOTICE.md), [dependency policy](docs/DEPENDENCIES.md), [remaining release gates](docs/RELEASE_CHECKLIST.md).

## Build and checks
Use Java 17 and Android SDK 35:

```sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest :app:dependencyInventory
python3 tools/verify_foundation.py
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. It uses a disposable Debug signature, not a production release key. GitHub Actions uploads a test-only APK with SHA-256 only after the build job succeeds. Emulator tests run separately on API 26 and 35. Always check the exact commit/run and test reports before attributing results. Production packaging is blocked pending review.

## Next priorities
Add sourced, licensed and human-reviewed lessons module by module, followed by real lesson progress, offline content packs and child-focused usability testing. The Canva reference was readable and informed the module structure; no paid assets were imported.
