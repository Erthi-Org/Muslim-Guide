# Muslim Guide — Erthi foundation and localization

Status: architecture and design requirements, not implemented features.

## Identity
A free, open-source Islamic educational Android app by Erthi | إرثي, independent from Nexvary commercial software.

## One app, integrated modules
Muslim Child, Learn Prayer, Hisn Al-Muslim, Prophets' Stories, Manners and Ethics, Simplified Creed, Seerah, Supplications, Zakat and Charity, Fasting and Ramadan, Hajj and Umrah, plus search, library, favorites, progress and settings.

## Internationalization from day one
- Android Kotlin, Jetpack Compose, Material 3; string resources, locale-aware formats and no hard-coded UI copy.
- Initial locales: ar, en, fr, tr, ur, id, de, es.
- Arabic and Urdu RTL; other locales LTR. Mirror navigation where appropriate but not Quran script or media.
- Language selection at onboarding and in settings, independent of system language.
- UI design reference: deep royal/navy blue, restrained gold, readable Arabic typography, real illustrated icons, responsive cards.
- Test long translations, font scaling, TalkBack, color contrast and bidirectional mixed-script content.

## Religious integrity
- No generated or altered Quran verses, fabricated hadith or unsourced rulings.
- Separate provenance, licenses and scholarly review for Quran text, hadith, translations, recitations, fonts and images.
- Label unverified draft content and do not ship it as authenticated religious instruction.

## Engineering milestones
1. Compose app shell and language switching with RTL/LTR previews.
2. Navigation and module placeholders with local sample UI copy only.
3. Source registry and reviewed content pipeline.
4. Offline tests, accessibility checks, CI and signed release process.
5. Scholarly and legal review before publication.

No working APK, tests or translations are claimed by this document.
