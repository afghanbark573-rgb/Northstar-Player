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


## CI run #58 — descendant fix validation in progress
- Run [45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) tests exact source commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
- At latest poll, debug and release jobs were running; unit/debug build and signed release build steps were active. Android instrumentation had not started; no artifacts were available.
- Native `PROJECT_STATUS.md` records this state. Poll run #58 and verify both instrumentation tests plus signature verification before proceeding.


## 2026-11-24 — durable continuity / chat-independence checkpoint

### Canonical-source-of-truth agreement
- The GitHub repository is the durable project record. Chat history, assistant memory and downloadable handover documents are convenience layers only; deleting old chats must not erase project decisions or engineering state.
- Native source of truth: branch `phase-0/release-signing`, especially `NORTHSTAR_PRODUCT_SPEC.md`, `NORTHSTAR_IMPLEMENTATION_PLAN.md`, `PROJECT_STATUS.md`, the Android source tree, Gradle configuration, migrations/schema exports, and `.github/workflows/`.
- Continuity source of truth: branch `continuity/master-pack`, especially `continuity/MASTER_CONTINUATION_PACK.md`, `continuity/CHANGELOG.md`, `continuity/NEXT_CHAT_INSTRUCTIONS.md`, and `continuity/README.md`. This branch is documentation-only and must not be confused with the app source/release branch.
- Authority order when records conflict: current native source and current product spec; then current implementation plan and `PROJECT_STATUS.md`; then latest dated continuity checkpoint; then historical changelog/older notes. Historical entries are preserved as history, not treated as current instructions when superseded.
- A new chat must inspect the live repository and current branch heads before coding. Never assume this pack alone proves GitHub has not changed. Record exact commit SHAs, CI run IDs, test counts, artifact identities/digests and device-test status.
- After each successful engineering milestone, update native `PROJECT_STATUS.md` and continuity master pack/changelog/next-chat instructions/README before starting the next milestone. Record failures and their evidence too. Keep device verification separate from CI evidence.
- Never put signing secrets, keystore bytes, private media, passwords, recovery answers or other credentials in tracked documentation.

### Current exact engineering checkpoint — CI run #58
- Workflow: [#58 / 45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837); tested source SHA: `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
- Latest poll: overall workflow still `in_progress`. Debug JVM unit tests and debug APK build completed successfully. Room migration + SAF DocumentsProvider instrumentation step is still running; no instrumentation result may yet be claimed.
- Release job completed successfully: release keystore restored, signed release APK built, `apksigner` signature verification passed, and signed artifact uploaded. Artifact: `northstar-release-apk`, artifact ID `14944179159`, SHA-256 `f9a71cc8718d67485a41c3663a194a8dcfd08d68d3548b94e45925fe124dc0eb`. This is a CI artifact only; do not distribute it as an intermediate user release.
- SAF gate remains OPEN until the debug job finishes and both Room migration and SAF DocumentsProvider integration tests are confirmed with exact counts/results. If failure, inspect diagnostics before changing code.
- Do not start Library Browser aggregation/sorting until the SAF integration test passes. Once passed, document the verified milestone and then proceed with the planned folder-metadata/sorting work and regression suite.
- Physical USB/SD provider behavior, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, and signed install-over-existing upgrade remain distinct unverified device gates.

### Product agreements that must survive chat deletion
- Northstar is a local-first native Android media player, not a browser wrapper. Keep user media local; no automatic cloud upload or cloud-library dependency.
- Eight approved product areas ship as one coordinated public release; no intermediate APK distribution.
- AMOLED black-and-white design. Exactly three library modes: Video Folders (default), Files Only, Nested View.
- Preserve package/application ID `com.northstar.player`, permanent signing identity and release credentials. Never commit or reveal secrets.
- Product spec is authoritative. Before each coding milestone, state Main Quest, bundled Side Quests, affected files, risks, acceptance criteria and regression tests. No casual Room schema changes: migration, tests and schema artifact are required.
- User development environment: Xiaomi 14 / Android / GitHub mobile; no desktop or terminal. Use cloud CI and mobile-friendly instructions. Never claim physical-device acceptance without the user's actual test.

### Exact next-chat restart
1. Read `continuity/README.md`, then the final/current checkpoint in this file and `continuity/NEXT_CHAT_INSTRUCTIONS.md`.
2. Inspect current refs/heads and recent commits for both `phase-0/release-signing` and `continuity/master-pack`; fetch native `PROJECT_STATUS.md`, product spec and implementation plan.
3. Poll run #58 above; verify exact SHA, debug job, instrumentation test counts/results, release signature verification and artifact details. If it is still running, continue polling; do not treat this checkpoint as a completed test.
4. If run #58 fails, inspect its diagnostics and exact exception before modifying code. If it passes, update all continuity records with the final evidence, then begin the already-planned Library Browser metadata aggregation/sorting milestone.
5. Continue the milestone update cadence and preserve all product/release/device-testing guardrails above.

Standard restart phrase: **“Let's continue the Northstar Player project from the last checkpoint.”** This phrase is a convenience only; all required context is now stored in GitHub.

 
## CURRENT CHECKPOINT UPDATE — 25 NOVEMBER 2026 — CI run #58 passed

### Exact verified source and workflow
- Native source tested: `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4` on `phase-0/release-signing`.
- Workflow [run #58 / 45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) completed **success**.
- Debug JVM tests/build and connected Android instrumentation passed. Diagnostics artifact ID `14946877392` reports **2 tests, 0 failures, 0 errors, 0 skipped** on Android emulator API 35:
  1. Room migration `migrationFromV1PreservesLibraryAndCreatesIsolatedStagingTable` — passed.
  2. SAF nested DocumentsProvider integration `createsAndListsDirectoryInsideNestedDocumentProviderFolder` — passed.
- The instrumentation test log explicitly reports `OK (2 tests)`. Gradle XML, HTML report, and textproto agree.
- Signed release build and `apksigner` verification passed. Release artifact ID `14944179159`, ZIP SHA-256 `f9a71cc8718d67485a41c3663a194a8dcfd08d68d3548b94e45925fe124dc0eb`. Debug artifact ID `14948455778`; Room schema artifact ID `14948316137`.
- These are CI artifacts, not an intermediate release. No APK was distributed.

### What this closes — and what it does not
- The debug test-provider descendant-chain fix is verified; the CI SAF integration gate and Room migration gate are now closed for this tested source commit.
- This is emulator instrumentation evidence only. It does not establish real USB/SD provider behavior, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, or signed install-over-existing upgrade compatibility.
- Keep those device gates open until actual device testing is completed. Do not claim user-device acceptance.

### Next engineering step
- The next planned internal milestone is Library Browser folder metadata aggregation and sorting, after a fresh live-source inspection.
- Before coding, identify Main Quest, bundled Side Quests, exact affected files, risks, acceptance criteria and regression tests. Preserve the three approved library modes, folder hierarchy, empty-folder semantics, unknown-date handling, stable tie-breaks, source separation and existing behavior.
- No casual Room schema changes. Any schema change requires a migration, automated tests and checked-in schema artifact.
- Eight approved product areas remain one coordinated public release. No intermediate APK distribution.

The previous 24 November checkpoint below is retained as history; its "run #58 in progress" statements are superseded by this verified result.


## CURRENT CHECKPOINT UPDATE — 25 NOVEMBER 2026 — Library Browser metadata/sorting CI passed

### Exact tested source and CI evidence
## Library Browser metadata and sorting hardening — CI run #60 passed — 2026-11-25

- Native source commit: `9b84ea24a1f11671aca54c10a339dc161307473d` on `phase-0/release-signing`.
- Workflow [run #60 / 45479496288](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45479496288) completed with overall conclusion **success** on that exact HEAD.
- Debug job passed `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:connectedDebugAndroidTest`. The unit-test task includes the LibraryBrowser regression suite; its ten LibraryBrowser test cases cover the existing modes/folder behavior plus unknown dates, deterministic equal-value tie-breaks, normalized separators, and source isolation.
- Instrumentation diagnostics artifact `northstar-instrumentation-diagnostics`, ID `14962565694`, ZIP SHA-256 `41e5f659cf75907379bc4152fe297ee18912c90b3486f1665e78895d9e6e0043`, confirms **2 tests, 0 failures, 0 errors, 0 skipped** on Android emulator API 35:
  1. `NorthstarDatabaseMigrationTest.migrationFromV1PreservesLibraryAndCreatesIsolatedStagingTable` — PASSED.
  2. `SafFolderProviderIntegrationTest.createsAndListsDirectoryInsideNestedDocumentProviderFolder` — PASSED.
  The XML report reports `tests="2" failures="0" errors="0" skipped="0"`; textproto marks both cases PASSED and test log ends `OK (2 tests)`.
- Release job passed release keystore restoration, signed release APK build, `apksigner` signature verification, and artifact upload. Artifact `northstar-release-apk`, ID `14960903935`, ZIP SHA-256 `086e153dbfdaddd2aa0478e9fc2e160e6ffc145e0141dc41088d32b43fc899c5`.
- Debug artifact `northstar-debug-apk`, ID `14962885359`, ZIP SHA-256 `45a813a101cc58d6e5e0d0483b0825fdf3196f002cf5461678a2e316c09e58c6`.
- Room schema artifact ID `14961903353`, ZIP SHA-256 `bd3f21aa0d42ab1a5d15220f16d916d60d7200d5755cc0c900baee527ed5a239`. No Room schema migration was introduced by this Library Browser change.
- Earlier run [#59 / 45479490916](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45479490916) also completed successfully for the intermediate production-code commit `609f6a1028a66a97f6480d69895769fdce82e996`; run #60 is authoritative because it tests the final commit including the regression-test additions.
- The production change is limited to `LibraryBrowser.kt` and `LibraryBrowserTest.kt`: folder metrics are aggregated in one normalized path-segment traversal rather than rescanning all videos for each folder; sorting has deterministic name/identity tie-breaks; unknown dates remain after known dates; source separation and the three approved library modes are preserved.
- CI evidence is emulator/JVM evidence only. Real USB/SD provider behavior, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, and signed install-over-existing upgrade remain **unverified device gates**.
- CI artifacts are for engineering verification only. **No APK was distributed**; all eight approved product areas remain one coordinated public release.

### Next-step guardrail
The folder metadata/sorting subtask is complete for this tested commit. Re-inspect the live implementation plan, product spec, status, and source before choosing the next internal task. The broader Library and Storage foundation still includes background/incremental MediaStore scanning, permission-scope handling, SAF grants/revocation, and folder hierarchy integration; do not select or implement one until its exact existing code state, dependencies, affected files, risks, and acceptance tests are confirmed. No casual Room schema change.

### Next chat / engineering sequence
1. Inspect live native and continuity branch heads; do not assume this checkpoint is still the latest state.
2. Read the current product spec, implementation plan, and latest native `PROJECT_STATUS.md`; inspect the Library Browser source and its adjacent repository/permission/storage code.
3. Select the next internal task only after mapping current implementation to the plan. Candidate areas from the broader Library and Storage foundation include background/incremental MediaStore scanning, full/partial/denied/reselection permission states, persisted SAF grant lifecycle and revocation, and folder hierarchy integration. Do not assume any candidate remains untouched.
4. Before coding, declare Main Quest, bundled Side Quests, exact affected files, risks, acceptance criteria, and regression tests. Keep Room schema unchanged unless a genuine requirement is identified and migration/tests/schema export are planned.
5. Maintain one coordinated public release for all eight approved product areas. No intermediate APK distribution. Keep physical device gates separate from CI.
 

## CURRENT CHECKPOINT UPDATE — 25 NOVEMBER 2026 — Folder NEW-state lifecycle CI passed


## Folder NEW-state lifecycle — CI run #68 passed — 2026-11-25

- Native source commit: `d00f920399d699981532985c5e07a9c4b23035fe` on `phase-0/release-signing`.
- Workflow [run #68 / 45483698896](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45483698896) completed with overall conclusion **success** on that exact commit.
- Debug job passed `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:connectedDebugAndroidTest`. The unit-test task includes six focused `FolderNewStateTest` cases and the repository discovery/acknowledgement regression test.
- Emulator API 35 instrumentation diagnostics artifact ID `14964397667`, SHA-256 `606acb2774245aa223605da8217ebf4dcf77067ff421894bb2da20443bd087ab`, confirms **2 tests, 0 failures, 0 errors, 0 skipped**: Room migration/acknowledgement assertion passed; nested SAF DocumentsProvider create/list integration passed. XML reports `tests="2" failures="0" errors="0" skipped="0"`; test log ends `OK (2 tests)`.
- Release job passed release keystore restoration, `:app:assembleRelease`, `apksigner` verification and signed artifact upload. Release artifact ID `14963031377`, SHA-256 `58f2c260ec92394e8b6a3c93b523925a699c297353a207f9ad5b6026d9a253bb`.
- Debug artifact ID `14964815811`, SHA-256 `bc15724c02fc50bf63354fd1ab47d31d059a1fa094c6dfe685d5600dce0f4ccc`. Room schema artifact ID `14964681631`, SHA-256 `b18dad2293c8469ac18a972f1b0fb00245848ed3944e354fd032307ae24dd6ce`.
- Implementation reuses existing `discoveredAtMillis` and `acknowledgedAtMillis` fields. Shared `FolderNewState` logic is used by MediaStore and SAF; new descendant videos or child folders advance discovery state; opening a folder acknowledges it. Same-millisecond discoveries are handled without losing the NEW state. No Room schema migration was introduced.
- Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade remain **unverified device gates**. CI artifacts are engineering evidence only. **No APK was distributed**; all eight approved product areas remain one coordinated public release.

### Next-step guardrail
The folder NEW-state lifecycle is complete for the tested commit. Before choosing another task, inspect the current implementation plan, product spec, native status, and live source. Keep the distinct pull-to-refresh and Private Library long-pull gesture requirements separate; do not implement a gesture that opens Private Library without still requiring authentication. Also assess the remaining background/incremental scanning and permission/SAF lifecycle work against actual code before selecting the next milestone. No casual Room schema changes.


## 2026-11-26 — Library pull-to-refresh implementation verified in CI

- Native source commit: `7925d39b466897c23b74d76c9a8f08112185ec68` on `phase-0/release-signing` (corrective import for `LibraryRefreshTargets`; pull-to-refresh implementation is included in the tested source).
- Workflow [run #74 / 45494565813](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45494565813) completed with overall conclusion **success** on that exact source SHA, retry attempt 2.
- Debug job passed `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:connectedDebugAndroidTest`. The four focused `LibraryRefreshTargetsTest` cases are included in the successful JVM unit-test task:
  - `mainLibraryRefreshSelectsAllAvailableSafTrees`
  - `safFolderRefreshSelectsOnlyItsOwningTree`
  - `mediaStoreFolderRefreshDoesNotRescanSafTrees`
  - `externalStoragePageRefreshesAllAvailableTrees`
- Emulator API 35 instrumentation diagnostics artifact ID `15089184886`, SHA-256 `7c9731584722b1bf4a1bbf18974eabd6a1669acc9259c38131dbd287225088f3`, confirms **2 tests, 0 failures, 0 errors, 0 skipped**. Room v1 migration passed 1/1; nested SAF DocumentsProvider create/list integration passed 1/1. XML reports `tests="2" failures="0" errors="0" skipped="0"`; test log ends `OK (2 tests)`.
- Debug APK artifact ID `15089374232`, SHA-256 `1df67ac1d0387179eba3f5d27fae1253ed9e9f3d46783284f7c086146a6c703b`. Room schema artifact ID `15090132138`, SHA-256 `c968ed5b4188c5a74a723daf50ebba810280e48177b8611325e236b61fac8c34`.
- Signed release job passed keystore restoration, release build, `apksigner` verification and upload in the original attempt; it was not rerun because that job had already succeeded. Release artifact ID `14967587841`, SHA-256 `c06460e0db3e8d132be0eac7adc0cd136c42014f183a8089cb9f8f03f4ea4dba`. The overall retry run is green on the same exact source SHA.
- Implementation wraps the library ScrollView in `SwipeRefreshLayout`; refresh scans MediaStore unless viewing external storage, refreshes available SAF grants, selects relevant SAF trees by current page/source, avoids rescanning unrelated SAF trees for a MediaStore folder, keeps the spinner active through scan work, and reports failures without adding a destructive clear-before-scan path. The current folder and selected view mode remain represented by the existing activity state/observers.
- First attempt failed before instrumentation because the CI emulator/ADB repeatedly reported `device offline` and broken-pipe errors. Retry attempt 2 completed successfully; no application-code change was needed to address that infrastructure failure.
- This is CI validation, not physical-device acceptance. Pull gesture behavior, real USB/SD providers, picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade remain unverified device gates. No APK was distributed. All eight approved product areas remain one coordinated public release.
- Next: inspect the live implementation plan and current source before selecting the next internal Library/Storage task. Keep the Private Library pull-past-threshold-and-hold shortcut separate from ordinary refresh; authentication remains mandatory. No casual Room schema changes.



## CURRENT CHECKPOINT UPDATE — 26 NOVEMBER 2026 — Permission-aware MediaStore reconciliation CI passed

### Permission-aware MediaStore reconciliation — CI run #45764030633 passed

- Native source commit: `87183de1d23ebede848d6c59eae9106358e9093e` on `phase-0/release-signing`.
- Workflow [run #45764030633](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45764030633) completed with overall conclusion **success** on that exact SHA. Both debug and signed release jobs passed.
- JVM unit tests and debug APK build passed. Emulator API 35 instrumentation reports **4 passed, 0 failures, 0 errors, 0 skipped**. The four tests cover atomic MediaStore snapshot publication preserving metadata for missing rows, discarding abandoned staging without changing the visible snapshot, Room v1 migration, and nested SAF DocumentsProvider create/list integration. Exact XML: `tests="4" failures="0" errors="0" skipped="0"`.
- Diagnostics artifact ID `15092115234`, ZIP SHA-256 `8c8888439932a14614e81e50368eb0c909492b4dace52ca880d500e43024e3be`. Test XML/HTML reports were downloaded and inspected.
- Signed release build restored the permanent signing key, built the APK and passed `apksigner` verification. Release artifact ID `15091590357`, ZIP SHA-256 `f5589bc6578fb8aae8c39699b15d674e9520a1387090e51a1d3cf615ea0aafff`; extracted APK SHA-256 `b35e9a7cf6fa1d3e18c890e5086d6c76bb9b05ec334d63954a2c92135a558c56`. Debug artifact ID `15092045614`, ZIP SHA-256 `b81a04a8a0167dedbf1b702462fa635302bc8300e946ac0078e347200ad8fefd`. Room schema artifact ID `15092055600`, ZIP SHA-256 `b3a12163e43dcc0b7701c0e9ed6f822634dc6477a864c33a4aa1b36d332fc528`.
- The scanner stages query results in batches and publishes only after a complete query. Failed/cancelled scans discard staging and preserve the last successful visible snapshot. Denied permission marks MediaStore unavailable without deleting historical records or user metadata. Stale folders are hidden/deactivated while identity and NEW-state history are retained. Permission changes are reconciled on permission callbacks and app foreground; SAF remains source-isolated. No Room schema migration was added.
- CI evidence is not physical-device acceptance. USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing upgrade, and manual pull gesture behavior remain outstanding. **No APK was distributed**; the eight approved product areas remain one coordinated public release.

### Next chat / engineering sequence
1. Confirm the live native branch and source HEAD; this checkpoint validates exact SHA `87183de1d23ebede848d6c59eae9106358e9093e`.
2. Read current product spec, integrated implementation plan, and native `PROJECT_STATUS.md`; inspect the current source before selecting the next task.
3. Map remaining background/incremental scanning, SAF persisted-grant/revocation lifecycle, and hierarchy integration to actual implementation. Do not assume these are outstanding without checking.
4. Before coding, declare Main Quest, bundled Side Quests, affected files, risks, acceptance criteria, and regression tests. Avoid casual Room schema changes.
5. Preserve local-first privacy, package ID `com.northstar.player`, permanent signing identity, AMOLED black-and-white direction, exactly three library modes, and one coordinated public release for all eight product areas. No intermediate APK distribution.
6. Physical USB/SD, real picker/persisted grants, Xiaomi 14 acceptance, signed install-over-existing upgrade and manual pull gesture remain device gates until tested on real hardware.




## CURRENT CHECKPOINT UPDATE — 26 NOVEMBER 2026 — AUTOMATIC SAF GRANT RECONCILIATION VERIFIED

### Exact source and CI
- Native branch: `phase-0/release-signing`
- Exact final source HEAD tested: `e37544cd19294774846e19dbd72657cb4307d7d4`
- CI run [#97 / 45775813217](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45775813217) completed **success** on the exact final commit. Live native branch HEAD was confirmed to match.
- Debug job passed: `:app:testDebugUnitTest :app:assembleDebug`; emulator instrumentation completed successfully; diagnostic, Room schema and debug APK artifacts uploaded.
- Release job passed: signed release APK built, `apksigner` signature verification passed, release artifact uploaded.

### Instrumentation evidence
Diagnostics artifact ID `15096549503`, ZIP SHA-256 `8c8888439932a14614e81e50368eb0c909492b4dace52ca880d500e43024e3be`.
XML report: **4 tests, 0 failures, 0 errors, 0 skipped**:
1. `mediaStoreSnapshotPublicationHidesMissingRowsWithoutDeletingTheirMetadata`
2. `abandonedMediaStoreStageCanBeDiscardedWithoutChangingVisibleSnapshot`
3. `migrationFromV1PreservesLibraryAndCreatesIsolatedStagingTable`
4. `createsAndListsDirectoryInsideNestedDocumentProviderFolder`

The SAF reconciliation policy's four JVM unit tests cover new disconnection, persistent unavailability, reconnection-triggered rescan, and avoiding recursive rescans of continuously mounted trees. The `:app:testDebugUnitTest` task passed.

### Build artifacts
- Signed release artifact ID `15096530730`; ZIP SHA-256 `7efceb79324d0d250afc019f1dcd1f4d932d6ff49a8b7414b3dd9dd33328fe01`; extracted `app-release.apk` SHA-256 `3c714faaadfa1ffd73aa9b004dba6569b6ba64f1cdd5ddf8a2cd260c0232bfb2`.
- Debug artifact ID `15096619296`; ZIP SHA-256 `a56c5dedb43426b841d0c86bac2d8abb5c70951ff5bba07a948afbb58b02f19f`.
- Room schema artifact ID `15097206733`; ZIP SHA-256 `890eaf76897b3ea5e926b684bfe5999db2ceca6c15165d2aa8f9d279873e5f97`. No Room schema change/migration was introduced.

### Implemented behavior
- Reconcile persisted SAF read grants by checking provider-root reachability.
- Mark revoked/unreachable sources unavailable while preserving historical media metadata and folder identity.
- When a previously unavailable source becomes reachable, perform a full SAF rescan to restore its current snapshot.
- Do not recursively rescan a continuously reachable tree during background/foreground availability checks.
- Preserve SAF/MediaStore source isolation; no Room schema change.

### Remaining verification gates
Physical USB/SD provider behavior, actual Android picker and persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing, and manual pull gesture behavior remain unverified until tested on the physical device. CI success does not close these gates. No APK distributed. All eight approved product areas remain one coordinated public release.

### Immediate next-chat instruction
Confirm live native and continuity branch heads, read the product spec and integrated implementation plan, inspect current source and status, then choose the next internal milestone based on evidence. Before coding, state Main Quest, bundled Side Quests, affected files, risks, acceptance criteria and regression tests. Preserve package/signing identity, local-first privacy, AMOLED black-and-white direction and the no-intermediate-APK rule.


---

## HISTORICAL CHECKPOINT — 26 NOVEMBER 2026 — GENERATION-AWARE INCREMENTAL MEDIASTORE SCANNING (PENDING AT THE TIME; SUPERSEDED BY VERIFIED CHECKPOINT BELOW)

### Repository and exact source under test
- Native repository: https://github.com/afghanbark573-rgb/Northstar-Player
- Native branch: `phase-0/release-signing`
- Latest source commit: `49a73222f29b28fba8924210aef5a8b10fe17fdf`
- CI workflow [run #45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016), run number 111, is testing that exact SHA.
- At checkpoint creation, the workflow overall status is **in_progress**. The `release-apk` job completed successfully: signed release APK built, `apksigner` signature verification passed, and release artifact uploaded. The `debug-apk` job passed `:app:testDebugUnitTest` and `:app:assembleDebug`; database migration instrumentation is still running. Do not mark this milestone CI-verified until the entire workflow completes successfully and the instrumentation report is inspected.
- Release artifact ID: `15098767237`; ZIP SHA-256: `cf039350a15537da866882e36ec0ecb4c8e9fcc26456b328b78c07b0b404ad49`. This is a CI artifact only; do not distribute it.

### Main Quest implemented
Generation-aware incremental MediaStore reconciliation is implemented in source, pending final CI verification:
- Room database version advanced from 2 to 3, with `MIGRATION_2_3` adding nullable MediaStore version and generation checkpoint columns to scan state. Existing v1→v2 migration remains registered.
- Added a scan policy that permits incremental queries only on API 30+, with full permission scope, matching MediaStore version, a valid previous checkpoint, nondecreasing generation, and no forced-full request.
- Incremental query uses `GENERATION_MODIFIED > previousGeneration AND <= capturedCurrentGeneration`. Capturing the upper generation before the query ensures later changes remain eligible for the next scan.
- Full and incremental results are staged in batches of 400 and published with the checkpoint in a Room transaction. Failed/cancelled delta scans discard partial staging and fall back to a full snapshot (except cancellation); failed scans must preserve the last known-good visible library.
- Full snapshot fallback is used for older Android, partial permissions, permission-scope transitions, MediaStore version changes, missing checkpoints, generation rollback, and forced-full scans.
- Manual refresh defaults to a full reconciliation. The 12-hour periodic worker explicitly forces a full scan; ordinary one-time foreground work can use incremental scanning.
- SAF remains isolated from MediaStore. Denied MediaStore access preserves historical records/checkpoint while reconciling availability.
- Room migration/instrumentation and schema v3 validation are part of the current CI gate. The generated schema artifact has not yet been downloaded/inspected or checked into the repository.

### Important known limitation — deletions
Android's broad MediaStore generation query does not provide generic deleted-row tracking for this supported implementation. Incremental scans upsert new/modified rows; deletions are detected by complete manual and 12-hour periodic full snapshots. Do not describe incremental runs as immediately detecting deletions. `MediaStore.queryDeletedFiles` is not a general fallback for the supported API range (it requires newer S Extensions/library support and is limited to external volumes).

### Immediate next-chat actions
1. Inspect live native HEAD and poll [CI run #45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016). Confirm it tests exact SHA `49a73222f29b28fba8924210aef5a8b10fe17fdf`.
2. Check debug job `139780302055` and release job `139780302340`. At checkpoint time, release was successful; debug instrumentation was still running.
3. If CI fails, inspect exact job logs and instrumentation diagnostics before changing code. If CI succeeds, inspect XML/HTML reports and confirm test counts, download and inspect Room schema v3, and check in the exact generated v2 and v3 schema snapshots under the configured Room schema path.
4. Record artifact IDs and SHA-256 values separately for ZIPs and extracted APKs; do not confuse them.
5. Only after exact-HEAD CI passes and schema artifacts are reconciled, update native `PROJECT_STATUS.md` and revise this pending checkpoint to a verified milestone in all four continuity records.
6. Do not begin another engineering milestone until that validation/documentation work is complete.

### Unchanged product and release guardrails
- Package/application ID: `com.northstar.player`; preserve permanent signing identity.
- Local-first; no automatic cloud upload.
- AMOLED black-and-white design; exactly three library modes.
- All eight approved product areas ship together in one coordinated public release. No intermediate APK distribution.
- Physical USB/SD provider behavior, actual Android picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing, and manual pull gesture behavior remain unverified device gates. CI is not physical-device acceptance.
- No signing secrets or keystore bytes in source or continuity files.
## VERIFIED CHECKPOINT — 26 NOVEMBER 2026 — GENERATION-AWARE INCREMENTAL MEDIASTORE SCANNING

### Exact source and CI result
- Native branch: `phase-0/release-signing`
- Exact source SHA tested: `49a73222f29b28fba8924210aef5a8b10fe17fdf`
- GitHub Actions [run #45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016) is now **success**, testing that exact SHA.
- Debug job `139780302055` and release job `139780302340` both completed successfully.
- Debug unit tests/build and emulator migration instrumentation passed. Instrumentation XML confirms **5 passed, 0 failures, 0 errors, 0 skipped**. Tests cover delta update/checkpoint commit, v1-to-v3 migration, complete snapshot publication, abandoned staging cleanup, and nested SAF DocumentsProvider directory create/list.
- Diagnostics artifact ID `15099592389`, ZIP SHA-256 `341b2cd1186dad0e68c97d5959457cdb69b45e6891629360be75db305935b7e3`.
- Signed release build and `apksigner` verification passed. Release artifact ID `15098767237`, ZIP SHA-256 `cf039350a15537da866882e36ec0ecb4c8e9fcc26456b328b78c07b0b404ad49`. Debug artifact ID `15099692177`, ZIP SHA-256 `0beac279091ab7d8914222e09fc73dd709542e4fbbda77ef99090da21052ca4d`.
- Room schema v3 artifact ID `15099991005`, ZIP SHA-256 `f0e05b986fde24e0efab95f0cafe1518e4edd4cdb1bf639b6c6e368176bd03fb`; prior v2 schema artifact ID `15097206733`. Both snapshots were downloaded and parsed. v3 adds nullable `mediaStoreVersion` and `mediaStoreGeneration` checkpoint columns to `scan_states`.
- **Schema snapshots still need to be checked into the native repository** at `android/app/schemas/com.northstar.player.core.database.NorthstarDatabase/2.json` and `3.json`. Neither path currently exists on the native branch. The exact v2 snapshot is in artifact `15097206733`; exact v3 is in artifact `15099991005`. Preserve these artifacts as source evidence; do not reconstruct schema v2.

### Verified implementation and limitation
Generation-aware incremental MediaStore scanning is now CI-verified. Incremental eligibility requires supported API, FULL permission scope, matching MediaStore version, valid checkpoint, nondecreasing generation and no force-full request. The delta query is bounded by a captured current generation; staged changes and checkpoint publish atomically. Unsafe/unsupported states fall back to full scans. Manual refresh and 12-hour periodic reconciliation force full scans.

**Deletion limitation:** generation-based deltas do not generically detect deleted rows. Deletions are found by complete manual and 12-hour periodic full snapshots. Do not claim immediate deletion detection.

### Immediate next task
1. Check in the exact generated schema v2 and v3 JSON snapshots at the paths above.
2. Update native `PROJECT_STATUS.md` and verify native/continuity branch heads after the schema commit.
3. Do not start another engineering milestone until the schema files and status records are reconciled.

### Unchanged gates
No APK was distributed. Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing and manual pull gesture behavior remain unverified device gates. Preserve package ID/signing identity, local-first privacy, AMOLED black-and-white theme, exactly three Library modes, all eight areas in one coordinated public release, and no intermediate APK distribution.
