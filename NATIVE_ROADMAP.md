# Northstar Player — Native Android Phase

## Current baseline
- Web prototype is frozen at 0.8.
- The existing root `index.html` and GitHub Pages workflow remain separate from native development.
- Native Android code lives under `/android`.

## Product principles
- Local-first: videos remain on the device; no automatic upload or cloud dependency.
- Library-first start; playback opens only after selecting a video.
- Quality and reliability over feature count.
- Meaningful bundled releases, not cosmetic micro-releases.

## Initial technical direction
- Language: Kotlin.
- UI: native Android views for the first proof of concept; the visual system can be refined after engine validation.
- Library: MediaStore video collection through ContentResolver.
- Playback candidate: AndroidX Media3 ExoPlayer 1.11.1 (stable as of 2026-11-11).
- Build: Gradle + GitHub Actions cloud runner; APK delivered as a workflow artifact.
- Provisional application ID: `com.northstar.player` (not yet a permanent product identity).

## Why Media3 first
Android's current Media3 documentation lists Matroska as a supported progressive container. This does not guarantee every MKV file: the contained video/audio sample formats and device decoders must also be supported. Media3 exposes track selection and supports sideloaded subtitle configurations. We will validate real files before treating it as the final engine.

LibVLC remains the fallback candidate if the test corpus exposes important compatibility gaps. libmpv remains a third candidate if advanced subtitle/rendering requirements justify its greater native integration effort.

## Media access
- Android 13+: request `READ_MEDIA_VIDEO`.
- Android 14+: handle `READ_MEDIA_VISUAL_USER_SELECTED` and partial access/reselection.
- Android 12 and below: legacy `READ_EXTERNAL_STORAGE` capped at API 32.
- Query `MediaStore.Video.Media.EXTERNAL_CONTENT_URI`; do not request broad all-files access for the normal library.
- Re-query when access changes or the app resumes after permission reselection.

## First native proof of concept
1. Request video access and handle denial/partial selection.
2. Query MediaStore and show a basic list with filename, folder path, duration and size.
3. Play selected local videos through Media3.
4. Surface clear playback errors and empty/permission states.
5. Build a debug APK in GitHub Actions and install it on the Xiaomi 14.

## Playback validation corpus
- MP4 / H.264 / AAC.
- MKV / H.264 / AAC.
- MKV / HEVC with common audio variants.
- MKV with embedded SRT/ASS/SSA or image-based subtitle tracks where available.
- External SRT and VTT.
- Multiple audio and subtitle tracks.
- Portrait and landscape video.
- Long playback, seeking, resume and rotation.

Record for every sample: opens, audio, video, seek accuracy, track discovery, subtitle rendering, stability, device heat/battery observations, and any decoder errors.

## Known limitations of this first scaffold
- UI is intentionally functional, not the final Graphite Orange design.
- Folder grouping, grid/list, thumbnails, favorites, persisted playback position, subtitle UI, gestures, and background playback are not yet implemented natively.
- Partial access handling is initial; permission reselection and refresh behavior need device testing.
- A cloud build passing is not proof of playback correctness; device testing is mandatory.

## Cloud build workflow
GitHub Actions builds a debug APK from `android/` on pushes, pull requests, or manual dispatch. Download the `northstar-debug-apk` artifact from the workflow run in the repository's Actions tab. No local Android Studio or terminal is required for this first iteration.
