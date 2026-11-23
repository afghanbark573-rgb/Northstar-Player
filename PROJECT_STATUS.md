# Northstar Player — Canonical Project Status

Last reconciled: 2026-11-23
Repository: https://github.com/afghanbark573-rgb/Northstar-Player

## Important branch map

- `main`: GitHub Pages web prototype and an earlier, unsigned native scaffold. Do not use its debug APK as the upgrade/release APK.
- `phase-0/release-signing`: current native integration/release branch. It contains the permanent signing configuration and signed release workflow. Native release candidates must be built from this branch until an intentional integration/merge plan is approved.
- Do not merge or replace either branch casually. The branches diverged in purpose and have different Android Gradle version/signing configurations.

## Product agreement

Northstar is a local-first Android media player, not a browser wrapper.

- Video files remain on the device. No automatic uploads, cloud library dependency, or subscription/AI bloat.
- Product priorities: reliable playback, a useful media library, simple everyday UX, privacy and stability.
- Quality over quantity; meaningful bundled releases with a Main Quest and useful Side Quests. Avoid cosmetic micro-releases.
- User works from Android phone/GitHub mobile, without desktop or terminal. Prefer cloud Android builds and complete replacement files/instructions.
- Do not claim Xiaomi testing until the user tests the APK.

## Web prototype

- Web prototype 0.8 is the frozen UX validation baseline.
- It validated local file selection, playback basics, folder scanning/navigation, list/grid, search/sort, and library-first flow.
- It cannot prove native codec support, embedded MKV subtitle handling, MediaStore behavior, battery/heat, or native background playback.
- Do not continue web releases unless a material blocker to the native transition is found.

## Native foundation and signing

- Kotlin Android application.
- Application ID / namespace: `com.northstar.player`. This identity must remain unchanged for upgrade compatibility.
- Minimum SDK: 26; compile SDK: 36; target SDK: 35.
- Playback candidate: AndroidX Media3 ExoPlayer 1.11.1. This is a candidate, not a final engine decision.
- MediaStore is the normal library discovery mechanism; do not request broad all-files access for ordinary video library scanning.
- Android 13+: READ_MEDIA_VIDEO. Android 14+: account for selected/partial video access and permission reselection. Android 12 and below: legacy READ_EXTERNAL_STORAGE capped at API 32.
- Permanent release signing was configured on `phase-0/release-signing`.
- Four GitHub Actions secrets are used by the workflow: ANDROID_KEYSTORE_BASE64, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD. Secret values must never be printed or committed.
- The release workflow restores the keystore, builds the release APK, verifies the APK signature with apksigner, and uploads the signed artifact.
- Confirmed successful CI run: https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45076363291. Both debug and signed-release jobs passed; signed release artifact was uploaded.
- The workflow's successful keystore-restore and signature-verification steps are evidence that the configured signing material works in CI. This does not prove the user has a separate offline backup of the keystore; maintain one securely.

## Version and upgrade safety

- Previously verified signed release: versionCode 2, versionName 0.1.1-native.
- Current library candidate is advanced to versionCode 3, versionName 0.1.2-native.
- Always increment versionCode for a new installable update.
- Keep the same applicationId and permanent signing certificate.
- Test installing the signed candidate over the existing app before declaring upgrade compatibility.
- Do not uninstall or clear app data during upgrade tests unless explicitly planned and backed up.

## Approved roadmap

### Phase 0 — Audit and release infrastructure
Signing and signed CI pipeline were completed and verified in CI.

### Phase 1 — Native architecture and foundations
- Confirm architecture and dependencies.
- Implement persistent data/storage strategy.
- Implement robust MediaStore scan, permission states and permission reselection.
- Implement incremental/background scanning and lifecycle-safe refresh.
- Preserve package identity and update compatibility.

### Phase 2 — Complete media library
- Library-first launch.
- By Folder as the default view.
- Folder navigation and useful folder/video categorization.
- All Videos, list/grid, search and sorting.
- Folder-level NEW indicators: colored dot on folder and labels within folders.
- No separate Recently Added section.
- No Smart Collections (explicitly removed from scope).
- Efficient thumbnails and metadata.
- Large-library performance.

### Phase 3 — Playback experience
- Validate Media3 against actual user files; compare LibVLC if required.
- Embedded and external subtitles, multiple audio/subtitle tracks.
- Seek, speed, orientation, gestures, resume position and playback reliability.
- Long-playback, memory, heat and battery testing.

### Phase 4 — Main and You interface
- Graphite Orange visual direction.
- User-oriented library/player design, not a generic browser/native demo screen.
- Clear navigation and preferences.

### Phase 5 — Private library and security
- Local-first access model, permission transparency and privacy safeguards.
- No upload or cloud dependency.

### Phase 6 — Additional features
Only after core library, playback and reliability are stable; prioritize by user value.

### Phase 7 — Testing and release
- Device regression tests, diagnostics, crash/error handling, upgrade verification and signed release process.

## Reliability agreement

Treat reliability as a first-class workstream:
1. Fast incremental/background scanning.
2. Memory and battery efficiency.
3. Large-library performance.
4. Permission management, including Android 14 partial access/reselection.
5. Error handling.
6. Crash recovery.
7. Playback and scanning independence.
8. Reliable data storage.
9. Diagnostics.
10. Startup performance.
11. Safe updates.

## Current native candidate

The current library candidate includes:
- ScrollView-backed vertically scrollable content.
- Folder grouping based on MediaStore RELATIVE_PATH.
- Folders and All Videos views.
- Search across filenames and folder paths.
- List/Grid toggle.
- Sort by recent, A–Z, and largest.
- Media3 playback retained.

Important: the current folder display is a first grouping pass, not the final nested folder-navigation UX. The simple native UI is a functional validation shell, not the approved final Graphite Orange interface.

## Current status and next actions

1. Build the versionCode 3 candidate on `phase-0/release-signing` using the permanent signing key.
2. Confirm the signed release job and apksigner verification succeed.
3. Install as an update over versionCode 2 without uninstalling.
4. User tests scrolling, folder grouping, search, list/grid and basic playback on Xiaomi 14.
5. Only after that, continue Phase 1 with permission re-selection, incremental/background scanning, persistence and architecture hardening.
6. Keep main branch web prototype separate until a deliberate merge/release strategy is chosen.

## Explicit safeguards

- Never replace the signed release workflow with the debug-only workflow.
- Never expose or commit keystore bytes, passwords, or secret values.
- Never change applicationId or signing certificate.
- Never install a lower versionCode over a higher one.
- Never claim a build passed until its actual workflow result is checked.
- Never treat CI success as proof of real-device correctness.
