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
- AMOLED black-and-white visual direction (latest user-approved direction; supersedes older Graphite Orange notes).
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

Important: the current folder display is a first grouping pass, not the final nested folder-navigation UX. The simple native UI is a functional validation shell, not the approved final AMOLED black-and-white interface.

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


## Recovery notes — 2026-11-23

### Recovered from earlier conversation context (not a full transcript)
- On 2026-11-22 around 20:01 UTC, the user explicitly accepted:
  - permanent application ID `com.northstar.player`;
  - permanent release signing key;
  - Room/SQLite as the local persistence direction;
  - AndroidX Media3 ExoPlayer 1.11.1 as the initial playback engine.
- On 2026-11-22 around 20:04 UTC, the user approved the roadmap, instructed us to begin Phase 0, and said to make no unrelated changes during that phase.
- The complete exact text of the user's "eight locked discussions" has not yet been recovered from available saved files or conversation retrieval. Do not claim that this document reproduces all eight verbatim. Continue searching for the original conversation/export if the user supplies it or it becomes accessible.
- Additional recovered library decisions:
  - Default view is By Folder.
  - Player stays hidden until a video is selected.
  - Player must have a distinctive custom UI, not Chrome/browser controls.
  - Folder navigation, embedded/external subtitle support, and Next/Previous playback are required.
  - Scan-first, file-manager-like library.
  - Compact receipt-style menus.
  - List/Grid applies to both folders and videos.
  - Folder-level NEW indicator: colored dot on the folder, with labels inside the folder.
  - No separate Recently Added section.
  - Smart Collections were explicitly removed from scope.
- The approved reliability requirements and seven-phase roadmap above are recovered summaries, not a substitute for the missing full eight-discussion transcript.

### Corrected assessment of native 0.1.2 candidate
- The user installed the signed 0.1.2-native APK after uninstalling the previous debug-signed app.
- The user supplied a screenshot of the previous app's library showing 238 videos, Folders / All videos, Grid, Recent, search, folder grouping and a scrollable list.
- The user then supplied a screenshot after installing 0.1.2 showing essentially the same visible features.
- Therefore the 0.1.2 library screen must NOT be described as a new feature milestone. Those visible capabilities already existed in the previous app. The latest native commit is a basic implementation/port and has not demonstrated a meaningful user-visible improvement over the prior baseline.
- Do not claim that 0.1.2 added these features. First obtain and compare the actual prior APK/source against the current branch. The older APK file is not the same as the installed app's source and may not include source history.
- The user's report that the screen looks unchanged is valid. Treat it as evidence that the candidate did not deliver the expected new functionality, not as a testing misunderstanding.

### Process correction
- Before every Northstar coding session:
  1. Read this PROJECT_STATUS.md.
  2. Inspect GitHub branch, recent commits, workflows, and current source.
  3. Identify the last user-verified baseline.
  4. Compare planned work against already-existing behavior.
  5. Make no code changes until the Main Quest and bundled Side Quests are grounded in the approved roadmap.
- Never create a new APK merely to repackage existing functionality.
- Preserve the user's agreement: meaningful bundled releases only; no cosmetic or duplicate-feature micro-releases.
- Keep Phase 0 constraints: no unrelated changes.


## Latest approved product specification — 2026-11-23

The user completed and approved all eight Northstar product discussions on 2026-11-23. The complete durable record is now maintained in **[NORTHSTAR_PRODUCT_SPEC.md](./NORTHSTAR_PRODUCT_SPEC.md)** on this branch.

This latest specification supersedes conflicting older UX proposals in this file, especially:
- Visual theme is now **AMOLED black and white**, not Graphite Orange.
- Main navigation is at the top; the bottom area remains empty (no bottom navigation).
- The top profile icon opens the **You** page, which contains Settings and all secondary options.
- Library has exactly three modes: Video Folders, Files Only, and Nested View. Nested View is the all-folders hierarchy mode, not a fourth mode.
- Sort, View Mode, and Layout are grouped in one simple icon-and-text controls area.
- Favorites are a separate section reached through You, not a home-screen section.
- Watch History replaces a separate Recently Played section.
- Private Library supports importing complete folders and creating folders/subfolders.
- Connected USB and SD storage should be browsable where Android/device access permits.
- **All eight discussion areas are intended for one coordinated product release.** Internal development and test milestones are allowed; do not ship one discussion/feature area as a separate public release.

Before implementation, review the complete product specification, resolve its listed implementation details, compare it with the live source, and obtain approval of the detailed integrated implementation plan.


## Integrated implementation plan — 2026-11-23

The eight approved discussions have been translated into **[NORTHSTAR_IMPLEMENTATION_PLAN.md](./NORTHSTAR_IMPLEMENTATION_PLAN.md)**.

- Status: drafted and saved for user review; not yet approved for implementation.
- The plan records the fresh source inspection, current gaps, proposed architecture, data model, library/player/private-storage/external-storage design, acceptance criteria, test strategy, and one coordinated release gates.
- No application source code was changed and no APK was built while preparing the plan.
- Seven genuine product decisions remain listed in Section 14 of the plan. Do not silently decide destructive private import/recovery behavior or user-visible interaction details.
- The next step is user review/approval of the implementation plan and resolution of those product decisions. Coding must not begin until then.
- `NORTHSTAR_PRODUCT_SPEC.md` remains the authority for the eight locked product discussions. This implementation plan translates those requirements; it does not replace them.
- `NATIVE_ROADMAP.md` contains older statements that conflict with the current product specification/status. Treat the latest product spec and this status file as authoritative until the roadmap is reconciled.
