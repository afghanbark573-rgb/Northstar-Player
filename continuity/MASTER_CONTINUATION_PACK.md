# NORTHSTAR PLAYER — MASTER CONTINUATION PACK

**Canonical checkpoint:** 23 November 2026  
**Purpose:** Preserve product decisions, technical state, evidence, risks and exact next actions across chats.

## 1. How to use this pack

Treat this as the master handover record. Update this same document at every major milestone and before moving to a new chat. Do not replace accumulated history with a summary of only the latest conversation. Preserve decisions, rationale, verified work, unresolved issues and superseded notes with clear labels.

Always distinguish:
- Product decisions
- Source implementation
- CI verification
- Signed artifact availability
- Installation/upgrade testing
- User-device acceptance

These are different evidence levels. The pack is context, not a substitute for inspecting the live repository. At session start, inspect the native branch, latest commit, source files, workflow and `PROJECT_STATUS.md`. Do not imply background work continues between turns.

## 2. Project identity and repository strategy

- Main repository: https://github.com/afghanbark573-rgb/Northstar-Player
- Native development branch: `phase-0/release-signing`
- `main` is the separate GitHub Pages web prototype / earlier native scaffold. Never casually merge it with the native branch.
- Application ID / namespace: `com.northstar.player` — immutable.
- User's test device: Xiaomi 14.
- User development environment: Android phone and GitHub mobile; no desktop or terminal.
- Web prototype 0.8 is frozen as UX reference only.
- Permanent release signing is configured through GitHub Actions secrets. Secret names only: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Never expose secrets or keystore bytes.
- Preserve package identity and signing certificate. Increment versionCode for future installable releases. Do not uninstall/clear data for upgrade testing unless explicitly planned.

## 3. Product vision and principles

Native Android, local-first, privacy-respecting media player. No automatic uploads, cloud-library dependency, subscription bloat or AI recommendations. Quality over quantity; build reliable, polished everyday workflows. Avoid cosmetic micro-releases and APKs that merely repackage existing behavior.

One coordinated product release covers all eight locked discussion areas. Internal milestones and CI artifacts are allowed; do not ship partial feature releases. Library is the entry point; player stays hidden until a video is selected. No bottom navigation. Act as a critical engineering/research partner: challenge weak assumptions and distinguish facts from proposals.

## 4. Eight locked product areas

### 4.1 Library — exactly three modes

1. **Video Folders** — default; show only folders containing accessible videos.
2. **Files Only** — show video files without folder listings.
3. **Nested View** — complete folder hierarchy, including empty folders. It is not a fourth mode or expandable inline tree.

Nested View uses normal folder-opening navigation: folder page, Back, compact/horizontally usable breadcrumb, child folders and files, Create Folder icon, unified controls and pull-to-refresh.

List/Grid applies to folders and files. Search covers filenames and folder names/paths; results identify containing folder. Sort choices: Date Added (newest first), Date Modified (newest first), A–Z, Largest. Use provider-supplied values where reliable; never invent timestamps. Folder-level NEW indicator: dot on folder and NEW labels inside. No Recently Added section. NEW state must persist and clear through a defined acknowledgement rule. Empty folders require SAF tree access; MediaStore RELATIVE_PATH cannot discover them.

### 4.2 Top navigation and You page

Top bar: Northstar title left; right-side icons in exact order Profile/You, unified Library Controls, Search. Unified controls open compact icon-and-text groups: Sort, View Mode, Layout. No separate top-bar controls and no bottom navigation.

You page contains Favorites, Watch History, Private Library and Settings. Private Library is visibly discoverable, not gesture-only. Favorites is a separate You destination; Watch History replaces Recently Played. Neither is a home-screen dashboard section.

### 4.3 Player experience

Final UI must use custom Northstar controls, not default Media3 PlayerView controls. Required: Play/Pause, Previous, Next, Rewind, Forward, seek timeline, current time/duration; tapping duration toggles remaining time. Left vertical swipe = brightness; right vertical swipe = volume. Double-tap left/right = backward/forward seek. Press/hold = temporary 2×, restoring prior speed on release/cancel. Multiple/custom playback speeds; portrait/landscape/auto rotation; fullscreen and auto-hiding controls. Embedded/external subtitles and audio-track selection, subject to measured engine support. Per-video resume. Previous/Next follows active folder or selected library/search-result context.

### 4.4 Headphone Mode and PiP — separate behaviors

Headphone icon arms/disarms audio-only background behavior. Tapping it does not close the video, hide the player, switch screens or leave the app. While in the player, video remains visible and plays normally. If user leaves video view or presses Home while armed, playback continues audio-only with no floating video.

Headphone Mode is session-only and resets OFF on next app launch. Completely closing/removing app task should end playback; engineer and test Activity backgrounding vs task removal, process death and force-stop. Android may not provide a graceful callback for every termination path.

PiP is separate. Dedicated PiP icon always works regardless of Settings. Settings controls only automatic PiP on Home press: enabled = auto PiP; disabled = leave normally. Disabling auto PiP must not hide/disable dedicated icon.

### 4.5 Private Library

Separate protected area; import individual files or full folders, create folders/subfolders; PIN and biometric authentication; encrypt stored video contents. Private items must never appear in public library, search, history, favorites or diagnostics.

Import action is **Move only**. No Copy option and no separate Delete action. Safe staged move: encrypt/write destination, verify integrity/readability, commit destination, then request source removal via Android MediaStore/SAF. Never remove source before verification. If Android denies source removal, report move incomplete and explicitly say original remains; preserve verified private data and allow retry.

Recovery: Northstar-curated question bank; user selects any 3 and answers all 3. All must be correct. No user-written questions and no recovery code/server/cloud recovery. Security design must not use plaintext answers directly as encryption keys. Key wrapping, answer normalization, rate limits and offline-guessing defenses require security review. App uninstall/data-clear loss risk must be addressed before release; encrypted export/backup design requires explicit approval.

### 4.6 Storage, scanning and reliability

MediaStore for ordinary shared/internal media; Room for app metadata/state; SAF for user-selected USB/SD trees and empty directories. No MANAGE_EXTERNAL_STORAGE for ordinary video discovery.

Handle Android 13 READ_MEDIA_VIDEO; Android 14+ selected/partial access and reselection; Android 12 and below READ_EXTERNAL_STORAGE capped at API 32. Background/incremental scanning, cancellation, last-good snapshot preservation, large-library performance, disconnect/revoked-grant handling, and playback/scan independence.

Room migrations must be explicit and tested; never clear database on upgrade.

### 4.7 Data architecture

Room entities currently include MediaItem, Folder, Favorite, PlaybackProgress, WatchHistory, ScanState, NewContentState and ExternalTreeGrant. Schema v1 JSON is generated and retained as CI artifact; as of this checkpoint it has not yet been committed into the repository.

### 4.8 Quality, privacy and release

Test permission changes, partial access, provider failures, cancellation, storage disconnect/revocation, process recreation, large libraries, playback and private-storage boundaries. Verify migrations, signed upgrade path, package identity and signing certificate. Do not claim device acceptance without real device testing.

## 5. Technical baseline

- Kotlin native Android; one app module initially.
- Existing Android Views foundation; no Compose migration without evidence.
- minSdk 26, compileSdk 36, targetSdk 35; Java 17.
- AGP 8.13.2; Kotlin 2.2.20.
- Room 2.8.5, KAPT 2.2.20; database v1, exportSchema=true.
- Coroutines 1.10.2; WorkManager 2.12.0.
- Media3 ExoPlayer 1.11.1 is an initial playback candidate only, not final selection.
- Benchmark LibVLC and mpv against the same representative media corpus.
- Current versionCode 3, versionName `0.1.2-native` (not a final release).
- Design: AMOLED black-and-white theme (supersedes stale Graphite Orange references).

## 6. Implementation completed and evidence

### 6.1 Architecture and scanner foundation

Room database/entities/DAOs, permission scope resolver (FULL/PARTIAL/DENIED), repository and application graph implemented.

MediaStore scanner streams cursor rows in batches of 400, checks cancellation per row, and stores URI/name/MIME/duration/size/provider dates/relative path. Null MediaStore cursor/query failure is treated as error. Failed/interrupted scans preserve last successful timestamp and do not claim a complete snapshot. Partial-permission scans are additive/upsert-only; they do not infer unreturned media disappeared.

Folder index derives hierarchy from RELATIVE_PATH with stable keys and subtree video counts. It cannot invent empty directories. WorkManager: unique refresh work, 12-hour periodic reconciliation, no network requirement, bounded retries. Startup and Activity/permission flows schedule refresh. Folder reconciliation runs only after successful scan. FULL replaces source folder index; PARTIAL upserts visible folders and retains unseen ones. Discovery/acknowledgement timestamps are preserved. Scanner-instance Mutex serializes overlapping requests within app process; it is not a cross-process lock.

Repository tests cover partial retention, full removal of stale folder entries and empty partial refresh retention.

### 6.2 Relevant source commits

- `0675c59d…` — Room foundation, entities/DAOs, permissions and initial tests.
- `8e6152cb…` — streaming MediaStore scanner.
- `5e2f1672…` — nested folder index from RELATIVE_PATH.
- `514121cf…` — WorkManager scan worker/scheduler.
- `0888eb2b…` — cancellation cleanup in NonCancellable context.
- `a718e722…` — Room repository reconciliation tests.
- `77df249f…` — scanner-instance Mutex.
- `d93e06ef…` — corrected nested folder projection semantics; CI passed.
- `640ea6b088c14afdde7103213a925d915642a176` — three-mode library navigation UI integration; latest recorded source milestone.

### 6.3 Latest UI integration and limitations

Commit `640ea6b` adds Video Folders, Files Only, Nested View; Video Folders default; folder navigation, Back and breadcrumb; top-bar order; unified Sort/View Mode/Layout menu; basic You page entries.

This is a functional integration shell, not the complete approved UX or a meaningful final release milestone. Known limitations:
- You destinations are Toast placeholders.
- Create Folder is placeholder.
- Pull-to-refresh absent.
- Private Library pull-and-hold absent.
- NEW indicators absent.
- Empty folders absent until SAF.
- Folder sort currently alphabetical in screen despite projection support for derived metadata.
- Player remains basic PlayerView.

## 7. CI verification checkpoint — 23 November 2026

Latest recorded CI run: https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45158767229

Run completed successfully for commit `640ea6b088c14afdde7103213a925d915642a176`.
- Debug job passed: unit tests, debug APK build, Room schema artifact upload and debug APK artifact upload.
- Release job passed: signed release APK build, apksigner signature verification and release artifact upload.
- Artifacts: northstar-room-schema (ID 14810401053), northstar-debug-apk (ID 14809134305), northstar-release-apk (ID 14809971481).
- Recorded expiry: 21 February 2027.

This verifies JVM unit tests/build and CI signing/packaging for this exact commit. It does **not** establish runtime correctness, upgrade compatibility or Xiaomi 14 acceptance. No current Xiaomi 14 device test is claimed. No install-over-existing signed update test is confirmed.

## 8. Storage engineering audit and next priority

This was a read-only audit; no application source code was changed during it.

### Exists
- ExternalTreeGrantEntity and ExternalTreeGrantDao in Room, with tree URI, display name, persisted flags, grant time and availability state.
- Media and folder records carry source identity fields.
- MediaStore scanning and folder indexing for ordinary indexed media.

### Missing
- SAF tree picker/grant workflow.
- Recursive SAF document-tree scanner/indexer.
- Empty-directory indexing.
- USB/SD source browsing and disconnected/revoked tree-grant UI.
- Folder creation through provider-supported SAF APIs. Current Create Folder control is placeholder.

### Required sequence
1. Design SAF tree selection, persisted URI permission capture/release, source identity and grant reconciliation.
2. Implement recursive SAF indexing of directories and video documents, including empty directories and nested hierarchy.
3. Keep MediaStore and SAF identities distinct; handle duplicate filenames and possible duplicate content appearances.
4. Handle disconnected storage, revoked permissions and provider errors without corrupting internal library data or last known-good snapshot.
5. Add tests for nested/empty folders, duplicate names, permission persistence/revocation, unavailable sources and reconciliation.
6. Implement folder creation only after tree access and provider capability behavior are reliable.
7. Then integrate pull-to-refresh, NEW-state lifecycle and Private Library pull-and-hold gesture with authentication.

MediaStore and SAF providers may expose different date/size metadata guarantees. Missing metadata must remain unknown; do not fabricate dates or sizes.

## 9. Remaining roadmap and release gates

### Library/storage
SAF picker, persisted grants, recursive tree scan, empty-folder discovery, USB/SD browsing, folder creation. Define folder aggregate size/date semantics across MediaStore and SAF; schema migration only after design, with exported schema and migration tests. Add pull-to-refresh, NEW acknowledgement lifecycle, private quick-access gesture with clear threshold feedback/authentication. Implement functional Favorites, Watch History, Private Library and Settings destinations.

### Player
Benchmark Media3 vs LibVLC vs mpv using representative MP4/MKV/HEVC, audio, subtitle and long-video corpus. Choose final engine based on measured codec/subtitle/track support, startup, seek, stability, memory, heat, battery, app size, licensing and maintenance. Implement playback service/session, custom UI, contextual queue, resume/history, gestures, subtitle/audio selection, Headphone Mode and PiP.

### Private Library/security
Security review of cryptographic key hierarchy and three-answer recovery. Implement encrypted file/folder storage, authentication gates, staged Move-only flow, interruption/recovery, uninstall/data-clear loss disclosure and approved backup/export design.

### Quality/release
Room migration tests and committed schema snapshot. Scanner tests for batching, null cursor, cancellation, failure timestamp preservation, permission reselection and concurrency. Storage provider/disconnect/revocation matrix; process recreation and large-library performance. Signed upgrade installation over existing app without changing package/signing identity. Full integrated automated/instrumentation/device acceptance; Xiaomi 14 test by user.

## 10. Historical corrections and superseded material

An earlier 23-Nov continuation document used obsolete Graphite Orange theme and two-mode/older library wording. Superseded by approved AMOLED black-and-white direction and exactly three modes. Earlier records saying implementation awaited approval are superseded; user approved integrated plan and implementation is active. Earlier status lists saying Room/background scanning had not started are historical and superseded by implementation record above.

Do not claim 0.1.2 introduced visible library features: user compared screenshots and found earlier app already showed essentially the same behavior. Exact original text of all eight discussions was not recovered verbatim. This pack records recovered decisions and current canonical specification; do not claim it is a verbatim transcript.

## 11. Required engineering workflow

At session start, read this pack and live `PROJECT_STATUS.md`, then inspect current branch/head, recent commits, source files and CI workflow. Identify last user-verified baseline and compare proposed work against existing behavior.

Before implementation, state Main Quest, bundled Side Quests, affected files, risks, acceptance criteria and regression tests. Do not change Room schema casually; add migration and migration tests before version change. Do not claim a build passed without current-head CI evidence; inspect debug tests/build, schema artifact and signed release/apksigner jobs. Do not claim Xiaomi 14 testing unless user installs and tests integrated APK. Do not prepare/deliver partial APKs unless explicitly requested. Keep one coordinated release strategy.

Update native `PROJECT_STATUS.md` and this master continuation pack at every major milestone and before moving to a new chat.

## 12. Canonical links

- Main repository: https://github.com/afghanbark573-rgb/Northstar-Player
- Native branch: https://github.com/afghanbark573-rgb/Northstar-Player/tree/phase-0/release-signing
- Latest recorded source commit: https://github.com/afghanbark573-rgb/Northstar-Player/commit/640ea6b088c14afdde7103213a925d915642a176
- Latest recorded CI run: https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45158767229
- Product specification: https://github.com/afghanbark573-rgb/Northstar-Player/blob/phase-0/release-signing/NORTHSTAR_PRODUCT_SPEC.md
- Integrated implementation plan: https://github.com/afghanbark573-rgb/Northstar-Player/blob/phase-0/release-signing/NORTHSTAR_IMPLEMENTATION_PLAN.md
- Project status: https://github.com/afghanbark573-rgb/Northstar-Player/blob/phase-0/release-signing/PROJECT_STATUS.md

## 13. Next-chat opening instruction

Continue Northstar Player from this Master Continuation Pack and the live GitHub repository. Do not restart or assume this checkpoint is still latest. First inspect `phase-0/release-signing`, its latest commit, `PROJECT_STATUS.md`, source tree and CI.

Last recorded source milestone is `640ea6b`; CI run `45158767229` passed debug tests/build, schema artifact upload, signed release build and apksigner verification. No Xiaomi 14 acceptance or signed upgrade test is claimed.

Next approved engineering priority is SAF tree access and recursive indexing for USB/SD and empty directories. Inspect existing ExternalTreeGrant Room model, then design and implement SAF selection, persisted grants, recursive directory/video indexing, source identity, disconnect/revocation handling and tests. Preserve all eight locked product areas, AMOLED black-and-white design, three library modes, permanent package/signing identity, local-first privacy and one coordinated product release. Update `PROJECT_STATUS.md` and this master pack at next major checkpoint.

---
**END OF CHECKPOINT — 23 NOVEMBER 2026**


---

# CURRENT CHECKPOINT UPDATE — 24 NOVEMBER 2026

This section supersedes earlier "latest state" statements above wherever they conflict. Historical records remain for traceability.

## Repository and branch state

- Native project repository: https://github.com/afghanbark573-rgb/Northstar-Player
- Active native branch: `phase-0/release-signing`
- Native branch HEAD at checkpoint: `07b96f31a4cbab9cfcad1e0ccfa4f81232142960` — **Run SAF test provider in debug app process**.
- Continuity archive branch: `continuity/master-pack` (documentation-only; does not alter native app branch).
- Product spec and integrated implementation plan remain authoritative. Eight approved product areas remain one coordinated public release. No intermediate APK distribution.

## Recent SAF test work

The production SAF folder-creation policy seam and Android `DocumentsContract` integration test have been added. The integration test invokes the production `AndroidSafFolderProvider` adapter against an in-memory test DocumentsProvider. The test checks creating a directory beneath a nested provider document and finding it in the child listing.

CI run [#55 / 45408437533](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45408437533), commit `0c50b3bbc14573c1ba327cbcdb3e9534f3553493`:
- JVM unit tests and debug APK build passed.
- Signed release APK build and `apksigner` verification passed.
- The Room migration instrumentation test passed.
- The SAF DocumentsProvider integration test failed and the instrumentation process subsequently crashed.
- The diagnostics artifact was downloaded and inspected. The log showed provider startup timeouts / `timeout publishing content providers` and provider-process termination. No actionable SAF assertion stack trace was produced. This is evidence of an emulator/test-provider process startup problem, not proof that production SAF folder creation is defective.

Mitigation committed in `07b96f31a4cbab9cfcad1e0ccfa4f81232142960`:
- Moved the in-memory test provider from `src/androidTest` into `src/debug`.
- Registered it in the debug manifest. It is excluded from release builds.
- Removed the old androidTest provider source and androidTest manifest.
- Kept production SAF adapter, policy and integration assertions unchanged.
- Updated native `PROJECT_STATUS.md` with the diagnosis and rationale.

## Current CI run — NOT YET COMPLETE

Run [#56 / 45411300973](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45411300973), exact HEAD `07b96f31a4cbab9cfcad1e0ccfa4f81232142960`:
- Release job completed successfully: signed release APK built, signature verified, artifact uploaded.
- Debug job passed JVM unit tests and debug APK build.
- At the latest check for this checkpoint, the debug job remained in progress at **Run database migration instrumentation tests**. The GitHub Actions run was still `in_progress`; no final debug-job conclusion or instrumentation result was available.
- Only the release APK artifact was listed at that poll. Do not infer test completion from the successful release job.
- Check run #56 again before any further status claim. If it is still running, inspect the live job. If it fails, wait for and inspect the diagnostics artifact/report/logcat before changing source. If it passes, verify both instrumentation tests and all jobs before marking this milestone validated.

## Immediate next actions

1. Poll run #56 and record its final result against the exact HEAD.
2. If instrumentation fails, inspect the newly uploaded report, XML/textproto results and logcat; identify the exact failure before changing code.
3. If instrumentation passes, record that the in-memory DocumentsProvider adapter integration test passed in the emulator. This still does not prove vendor USB/SD provider behavior or the real picker/persisted-grant lifecycle.
4. Keep physical USB/SD testing and Xiaomi 14 acceptance explicitly outstanding for the user to perform.
5. After SAF integration validation is resolved, resume the previously planned Library Browser metadata aggregation/sorting work, with regression tests for source separation, nested descendants, empty folders, unknown dates, equal tie-breaks, root mode, path normalization and video sorting. Do not begin it while this SAF test gate remains unresolved.
6. Continue to protect the one-coordinated-release strategy; no partial APK distribution.

## Verified versus unverified at this checkpoint

**Verified:** JVM unit tests and debug build for run #56; signed release APK build, signature verification and artifact upload for run #56; earlier run #55 Room migration instrumentation pass; CI evidence that the SAF integration test executes far enough to be discovered and run in run #55.

**Not verified:** Run #56 instrumentation result; SAF DocumentsProvider integration success; complete real-world USB/SD support; Android picker and persisted grant lifecycle on device; Xiaomi 14 acceptance; signed install-over-existing upgrade test; complete eight-area public release.



## CURRENT CHECKPOINT UPDATE — 24 NOVEMBER 2026 (RUN #56 DIAGNOSED; MANIFEST FIX COMMITTED)

### Source and test evidence
- Native branch source fix: `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b` ([commit](https://github.com/afghanbark573-rgb/Northstar-Player/commit/3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b)). The debug-only `TestSafDocumentsProvider` manifest now declares `android:permission="android.permission.MANAGE_DOCUMENTS"`, which Android requires for a `DocumentsProvider`.
- Native status documentation update: `8f0a0b19730b9700fde0233816e180c39732832e`.
- Run #56: [45411300973](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45411300973), tested commit `07b96f31a4cbab9cfcad1e0ccfa4f81232142960`, completed with overall failure.
- Verified passing in run #56: JVM unit tests/debug APK build; signed release APK build; `apksigner` verification; release artifact upload.
- Instrumentation failure root cause is now exact: Android crashed the target app while installing the debug test provider, before any instrumentation tests ran. Exception: `SecurityException: Provider must be protected by MANAGE_DOCUMENTS` from `DocumentsProvider.attachInfo`. The instrumentation report said zero tests started. This is a test-provider manifest defect; it is not evidence of a production SAF folder-creation failure.
- Corrective manifest change was committed as above. Provider remains debug-only; production SAF adapter, policy and assertions are unchanged.

### Immediate next action
1. Poll the CI run triggered by the manifest fix and confirm its tested HEAD is `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b` (or a later documentation-only descendant if workflow semantics indicate otherwise).
2. Verify both Room migration and SAF DocumentsProvider instrumentation tests actually execute and pass. A green job without test counts/results is insufficient.
3. If it fails, inspect the exact new diagnostics before editing code.
4. Only after the SAF instrumentation gate passes, resume Library Browser metadata aggregation/sorting and its planned regression suite.
5. Keep physical USB/SD provider behavior, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, and signed install-over-existing upgrade testing separate and outstanding until actually tested.

### Release and checkpoint safeguards
- No intermediate APK distribution; the eight approved product areas remain one coordinated public release.
- No production SAF changes were made in this correction.
- Keep the package/signing identity and local-first privacy requirements intact.
- From this checkpoint onward, after each successful engineering milestone, update native `PROJECT_STATUS.md` and this continuity archive before starting the next milestone. Record failures with exact evidence as well.


## CI run #57 — active after SAF manifest correction
- New run: [45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513), exact tested source HEAD `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`.
- At the latest poll both debug and release jobs were still running. The debug instrumentation step had not started, so there is no result yet. No artifacts were available at that poll.
- Next: poll run #57; confirm the debug-only DocumentsProvider now installs and both instrumentation tests execute and pass. Do not start the Library Browser milestone until then.


## CURRENT CHECKPOINT UPDATE — 24 NOVEMBER 2026 (RUN #57 DIAGNOSED; TREE DESCENDANT FIX)

### Run #57 evidence
- Run [45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513) tested exact source commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b` and completed with failure.
- JVM unit tests/debug APK build passed. Room migration instrumentation passed (1/1). The SAF DocumentsProvider integration test ran and failed (1/1) with `SecurityException: Document nested-parent is not a descendant of test-root` from `DocumentsProvider.enforceTree`, via `DocumentsContract.createDocument` and production `AndroidSafFolderProvider.createDirectory`.
- This is an exact in-memory test-provider contract defect. The provider represented parent IDs and child listings but did not implement `DocumentsProvider.isChildDocument`; Android consequently rejected a nested document as outside the requested tree. This is not an emulator crash and does not establish a production SAF implementation defect.
- Signed release job passed: keystore restore, release APK build, `apksigner` verification and artifact upload.

### Corrective test-only change
- Native commit [35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4](https://github.com/afghanbark573-rgb/Northstar-Player/commit/35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4) implements synchronized ancestor-chain traversal in the debug-only `TestSafDocumentsProvider.isChildDocument(parentDocumentId, documentId)`.
- It reports true for direct and deeper descendants, false for missing/broken parent chains. No production SAF adapter/policy, database or integration assertions were changed.
- Native `PROJECT_STATUS.md` records the run #57 evidence and source correction.
- A fresh CI run must validate this correction. SAF gate remains open until both Room migration and SAF DocumentsProvider tests pass on the new commit.

### Immediate next actions
1. Poll the CI run triggered by `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`; verify its exact tested SHA and both instrumentation results/test counts.
2. If the integration test fails, inspect new report/logcat and make an evidence-led correction.
3. Only after the SAF integration test passes, begin Library Browser metadata aggregation/sorting with the previously defined regression suite.
4. Keep physical USB/SD providers, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade test as separate outstanding device gates.
5. No intermediate APK distribution; all eight product areas remain one coordinated public release.

### Ongoing documentation rule
After each successful engineering milestone, update native `PROJECT_STATUS.md` and this continuity branch's master pack, changelog, and next-chat instructions before beginning the next milestone. Record failures with exact evidence as well.
