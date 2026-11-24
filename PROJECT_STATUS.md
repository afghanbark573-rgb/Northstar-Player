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
- Sort by Date Added, Date Modified, A–Z, and Largest; Date Added and Date Modified are separate newest-first options.
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
- The top-navigation and background-audio/PiP product decisions have now been resolved and recorded in the plan. Private import behavior (Move only), Private Library recovery experience (three selected questions from a Northstar-provided list), Nested View navigation/toolbar, separate Date Added/Date Modified sorting, player gestures, and Private Library quick-access gesture are resolved.
- The next step is user review/approval of the implementation plan and resolution of those product decisions. Coding must not begin until then.
- `NORTHSTAR_PRODUCT_SPEC.md` remains the authority for the eight locked product discussions. This implementation plan translates those requirements; it does not replace them.
- `NATIVE_ROADMAP.md` contains older statements that conflict with the current product specification/status. Treat the latest product spec and this status file as authoritative until the roadmap is reconciled.


## Approved Private Library import decision — 2026-11-23

- The user approved **Move only** as the sole Private Library import action.
- There must be no Copy option and no separate Delete action in the import workflow.
- The intended outcome is that the item leaves its former public location and exists in the protected Private Library.
- Engineering must use a safe staged transfer: encrypt/write the private destination, verify integrity/readability, commit it, then request removal of the source using the relevant Android/SAF flow.
- If Android denies or cannot perform source removal, report the move as incomplete and explicitly state that the original remains. Never claim success or remove the source before verifying the private destination.
- This decision is recorded in `NORTHSTAR_PRODUCT_SPEC.md` and `NORTHSTAR_IMPLEMENTATION_PLAN.md`. It does not authorize application-code changes; the implementation plan still requires the user's overall approval before coding.


## Approved Private Library recovery experience — 2026-11-23

- The user selected a question-and-answer recovery experience instead of a user-generated recovery code.
- Northstar will provide a curated list of recommended security questions. During setup, the user chooses any three and supplies answers to all three. All three answers are required for forgotten-PIN recovery.
- Questions are selected from Northstar's list; users do not create their own questions.
- This is a user-experience decision, not permission to use weak answers directly as encryption keys. The cryptographic key-recovery design, answer handling/normalization, rate limiting, and protection against offline guessing require a security-reviewed engineering design.
- Recovery remains local-first; no server-based recovery or automatic cloud upload.
- The decision is recorded in the master product specification and integrated implementation plan. No application code has been changed; overall plan approval remains a prerequisite to coding.


## Approved Nested View navigation and toolbar — 2026-11-23

- Nested View uses normal folder-opening navigation, not an expandable/inline tree.
- Tapping a folder opens a folder page showing its child folders and files, preserving the real hierarchy and including empty folders.
- Provide a Back control and a breadcrumb path near the top, showing the current location and allowing navigation to ancestor folders. Keep breadcrumbs compact and usable on narrow screens.
- Provide a Create Folder icon.
- Provide one compact controls icon that opens the unified Sort, View Mode, and Layout options. Do not scatter these options across separate toolbar buttons.
- Support pull-to-refresh on the folder page.
- This decision is recorded in the master product specification and integrated implementation plan. No application code has been changed; overall plan approval remains a prerequisite to coding.


## Approved library sorting options — 2026-11-23

- Provide separate **Date Added** and **Date Modified** sort options; do not merge them into a generic Recent option.
- Both date-based options sort newest first.
- Date Added uses the storage provider's added/indexed date where available; Date Modified uses the file's last-modified date where available.
- Also retain A–Z and Largest.
- If a provider does not expose a reliable date, handle the item consistently without inventing a timestamp. Folder NEW-indicator state remains separate from sorting.
- This decision is recorded in the master product specification and implementation plan. No application code has been changed.


## Approved player gesture arrangement — 2026-11-23

- Left-side vertical swipe controls brightness.
- Right-side vertical swipe controls volume.
- Double-tap the left side to seek backward; double-tap the right side to seek forward.
- Press and hold for temporary 2× playback; restore the previous speed on release or cancellation.
- Gesture zones and conflict handling must prevent accidental triggers and be tested on the user's Xiaomi 14. The separate proposed folder-screen pull gesture for Private Library access has not been approved and is not part of this locked player gesture decision.


## Approved Private Library quick-access gesture — 2026-11-23

- On the main library and folder pages, a normal pull-down followed by release refreshes the current page.
- Pulling farther than the normal refresh threshold and holding briefly activates a distinct Private Library access gesture.
- The gesture opens the Private Library authentication screen only; PIN or biometric authentication remains mandatory.
- Provide clear threshold/hold feedback and tune gesture arbitration to avoid accidental activation during ordinary refresh, scrolling, and navigation.
- This gesture is available on the main library and folder pages, not inside the video player.
- Recorded in the master product specification and integrated implementation plan. No application code has been changed.


## Private Library discoverability in You page — 2026-11-23

- The You/profile page must visibly list Private Library as a normal, directly accessible destination alongside Favorites, Watch History, and Settings.
- The pull-and-hold gesture is an additional quick-access shortcut only. It must never be the only way users can discover or open Private Library.
- Selecting the You-page Private Library entry still requires PIN or biometric authentication.
- Recorded in the master product specification and integrated implementation plan. No application code has been changed.


## Approved top navigation and unified library controls — 2026-11-23

- The top bar places the Northstar title on the left.
- On the right, the icons appear in this exact order: Profile/You, unified Library Controls, Search.
- The unified Library Controls icon sits between Profile/You and Search.
- That single icon opens one compact menu containing all three groups and all their options:
  - **Sort:** Date Added, Date Modified, A–Z, Largest.
  - **View Mode:** Video Folders, Files Only, Nested View.
  - **Layout:** List, Grid.
- Use simple icon-and-text labels within the menu. Do not add separate top-bar buttons for Sort, View Mode, or Layout.
- No bottom navigation.
- This resolves the remaining top-navigation product choice. Detailed visual styling and menu interaction polish remain design/implementation tasks.
- Recorded in the master product specification and integrated implementation plan. No application code has been changed.


## Approved background audio and Picture-in-Picture behavior — 2026-11-23

- Add a headphone control/icon in the player for audio-only background playback. This lets the user continue listening when leaving the app without keeping the video in a floating window.
- Support Picture-in-Picture (PiP) as a separate video experience.
- Implement both PiP activation methods: a dedicated PiP icon in the player and activation by pressing the Android Home button/leaving the app.
- Settings controls only automatic PiP on Home-button press (on/off). The dedicated PiP icon remains visible and always works regardless of this setting.
- Do not conflate the headphone/audio-only action with PiP.
- Validate Android lifecycle/API behavior, playback continuity, PiP controls/aspect ratio/return-to-app behavior, and device compatibility during implementation and Xiaomi 14 testing.
- This resolves the background-playback product decision. No application code has been changed.


## Clarification: headphone behavior and automatic Home-button PiP — 2026-11-23

- The headphone control does not immediately close the video, close the player, or leave the app. It arms audio-only background behavior while the video remains visible and plays normally in the player.
- If the user goes to the Android Home screen or otherwise leaves/exits the video view while headphone mode is armed, playback continues as audio only, without a floating video window.
- Audio-only continuation remains active until the user exits that video or stops playback. The player must clearly indicate the armed state and provide a clear way to turn it off while still in the video.
- The dedicated PiP icon remains visible/available in the player and always enters PiP when tapped.
- Settings controls only whether PiP starts automatically when the user presses Home: enabled = automatically enter PiP; disabled = leave the app normally without automatic PiP.
- Disabling automatic Home-button PiP must never hide or disable the dedicated PiP icon.
- Audio-only background behavior and PiP remain distinct modes.
- This clarification supersedes the earlier wording that Settings selected between the Home and icon PiP activation methods. No application code has been changed.


## Final clarification: session-based Headphone Mode — 2026-11-23

- Headphone Mode is a temporary toggle for the current app session, not a persistent user preference.
- Tapping the headphone icon arms audio-only background behavior; tapping it again while the video is open disarms it.
- While armed, leaving the video or going to Android Home continues audio-only playback without floating video.
- Completely closing/terminating the app ends Headphone Mode and its background playback. The app must not restore the mode on the next launch; it starts off, and the user must activate it again with the headphone icon.
- This lifecycle rule is part of the approved product behavior and supersedes any earlier wording that could imply the mode stays armed until merely exiting a video or stopping playback. PiP remains a separate feature with unchanged dedicated-icon and automatic-Home Settings behavior.
- No application code has been changed by recording this clarification; implementation-plan approval remains required before coding.


## Implementation-readiness review — 2026-11-23

- Rechecked the live `phase-0/release-signing` branch. Current HEAD at review: `81bc9d467dd4c9fccd99a60521ec9dd9366ac7b0` (documentation-only commit recording the final Headphone Mode lifecycle rule).
- Re-inspected `MainActivity.kt`, app Gradle configuration, Android manifest, root Gradle configuration, settings, and GitHub Actions workflow. The app remains a small prototype: one Activity owns UI, permission requests, synchronous MediaStore query, filtering/sorting/grouping, and Media3 playback; the standard PlayerView is used; `onStop()` pauses playback and `onDestroy()` releases it.
- The current manifest has media permissions and a launcher Activity, but no declared playback service or PiP support. The app Gradle file has no Room, coroutine, WorkManager, or test dependencies. These are source observations, not assumptions about features in historical APKs.
- The signed GitHub Actions workflow exists and its previously recorded successful run is still the last successful release evidence found. No new build was triggered during this review.
- Review found stale documentation: the product spec's Section 12 still listed Private import and recovery experience as unresolved; the implementation plan's Section 14 heading/approval checklist treated already-resolved decisions as pending; and the plan had duplicate subsection numbering in Private Library. These have been corrected in the updated documents.
- Added structural screen-by-screen wireframes and permission/discovery, playback/Headphone Mode, and Private move flow diagrams to the implementation plan. They are design artifacts, not implemented UI.
- Remaining gates are engineering: player-engine benchmark, cryptographic recovery/security review, Room schema and migrations, scan reconciliation/NEW-state rules, external-storage edge cases, lifecycle/PiP validation, and automated/device acceptance coverage.
- No application source code was changed and no APK was built. The integrated plan is ready for user review; implementation must wait for explicit approval.


## Engineering implementation update — 2026-11-23 (supersedes the older "Current status and next actions" list above)

The approved implementation plan is now in active development on `phase-0/release-signing`. The earlier statement that Phase 1 work had not started is obsolete.

### Implemented in source (CI/device verification must be tracked separately)

- Room 2.8.5 database and entities for media, folders, favorites, playback progress, watch history, scan state, NEW-content state and external tree grants.
- Room-backed media repository and Android video permission-scope resolver (FULL / PARTIAL / DENIED).
- MediaStore scanner streams video rows into Room in bounded batches of 400 and checks coroutine cancellation while scanning.
- Partial-permission scans are additive/upsert-only; they do not infer that unreturned items were deleted.
- Nested folder index builder creates stable parent/child folder keys and counts from MediaStore RELATIVE_PATH. It does not invent empty directories; those require a separately granted SAF tree.
- WorkManager 2.12.0 worker and scheduler: unique refresh work, periodic reconciliation every 12 hours, no network requirement, bounded retry policy.
- Application startup schedules periodic reconciliation; Activity start and successful permission grant schedule a refresh.
- Visible library now observes the Room media Flow rather than querying MediaStore synchronously in the Activity. Its current screen layout is still the earlier functional validation shell, not the approved final UX.
- Folder reconciliation runs only after a scan completes. FULL access replaces the source's folder index; PARTIAL access only upserts visible folders and retains previously indexed folders. Existing folder discovery/acknowledgement timestamps are preserved.

### Build and verification record

- Earlier verified CI runs for commits before the background-worker layer remain valid for those commits only.
- CI run `45152804949` failed in Gradle script configuration because a literal escaped newline was inserted in the WorkManager dependency declaration. It did not reach Kotlin compilation.
- Follow-up source repairs corrected the Gradle dependency formatting and the escaped-newline formatting in `NorthstarApplication.kt` and `MainActivity.kt`; the permission callback now schedules refresh after a grant.
- The current folder reconciliation and Room Flow integration are committed after those repairs. A successful build for the current head has **not yet been confirmed**. Do not label these commits build-verified until the GitHub Actions debug and signed-release jobs both complete successfully.
- No current-head APK has been delivered for user testing. No Xiaomi 14 runtime test has been performed on this implementation.

### Immediate engineering gates

1. Obtain a fresh GitHub Actions run for the current branch head; inspect debug build, signed release build, and apksigner verification. Fix any compile or runtime packaging errors before proceeding.
2. Confirm Room schema JSON is generated and committed under the configured schema directory; add migration tests before any schema version change.
3. Add automated coverage for scanner batching/cancellation/error timestamp preservation and FULL-versus-PARTIAL folder reconciliation.
4. Review first-scan and empty-library behavior, permission reselection, scan concurrency, and cancellation during Room writes.
5. Continue final library UX (three approved modes, actual nested folder navigation, controls, NEW indicators) only after the Room-backed data path is stable.
6. Keep the signed release identity, application ID, branch separation and coordinated-release policy unchanged.


## Continuation update — 2026-11-23 (plan approval and repository recheck)

- The user explicitly approved the integrated implementation plan in the project conversation. `NORTHSTAR_IMPLEMENTATION_PLAN.md` has been corrected to record approval and active implementation; older statements in this file saying the plan still awaited approval are historical and superseded.
- Rechecked the live `phase-0/release-signing` source. `MainActivity.kt` observes `RoomMediaLibraryRepository.observeAvailableVideos()` through a lifecycle coroutine and schedules background refreshes. `NorthstarApplication.kt` wires Room, the repository, permission checker, scanner and WorkManager scheduler. This confirms that the Room-backed work described above is present on this branch.
- Rechecked `NorthstarDatabase.kt`: Room database version is 1 and `exportSchema = true`; Gradle config points Room schema output to `android/app/schemas`. The expected schema directory/file was not discoverable through the GitHub file interface during this check. Treat schema export/commit as unverified, not as confirmed absent from every possible build artifact.
- Rechecked GitHub CI evidence: the commit-status endpoint returned no statuses for the recorded native implementation commits, and the available workflow-run lookup is limited to pull-request-triggered runs. No fresh current-head debug or signed-release result could be verified. Do not claim current-head build success.
- The repository default branch is `main`; native work remains on `phase-0/release-signing`. A separate default-branch commit titled “Build scrollable folder-based native video library with search and view controls” is not a substitute for the native branch's Room-backed source and must not be merged into it casually.
- No application source code or APK was changed/generated during this continuation. The documentation-only plan-status correction is recorded in commit `be49e7931f63788a82afb4c0906cc83c8cd039a3`.

### Next gates

1. Obtain a genuine Actions run for the native branch head and inspect both debug and signed-release jobs, including apksigner verification.
2. Verify generated Room schema output and commit the v1 schema artifact; add migration coverage before schema changes.
3. Locate and inspect the existing test source tree before adding scanner batching, cancellation, failure-state and FULL/PARTIAL reconciliation tests.
4. Review WorkManager concurrency and scan reconciliation before advancing to more UI work.


## CI validation improvement — 2026-11-23

- Updated `.github/workflows/android-build.yml` in commit `bef7aa8475e51e82087cd6d3ce78b54faa6ee364`.
- The debug job now runs `:app:testDebugUnitTest` before `:app:assembleDebug`.
- The workflow now uploads generated Room schema JSON as a separate `northstar-room-schema` artifact and fails if no schema JSON is produced. This turns schema generation into an explicit CI gate rather than relying on an unverified local assumption.
- The previous confirmed run `45153766356` for commit `7051f7b92cb31f6abe236c6ff88551297ad86d12` completed successfully: debug APK and signed release APK both built, release keystore restore succeeded, apksigner verification succeeded, and both APK artifacts were uploaded. This run did not run the newly added unit-test/schema-artifact steps.
- New validation run `45154832966` was triggered by the workflow change on current native branch head `bef7aa8475e51e82087cd6d3ce78b54faa6ee364`. At the time of this update, both jobs were still in progress; do not claim the new unit tests or schema artifact succeeded until the run completes.
- Existing unit tests found in the branch: five permission resolver tests and three folder-indexer tests. No scanner batching/cancellation or Room repository reconciliation tests were found in the inspected test directories.
- No APK was delivered to the user and no Xiaomi 14 runtime test is claimed.


## Scanner and folder-reconciliation verification — 2026-11-23

- Scanner cancellation cleanup was corrected in commit `0888eb2b147249a028822484b81f3523fc92133e`: the cancellation handler now persists the cleared in-progress state inside `withContext(NonCancellable)`, preserves the prior successful-scan timestamp, records cancellation, and rethrows the cancellation.
- CI run [45155337765](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45155337765) completed successfully for that scanner change. Both debug and signed-release jobs passed; release signature verification and artifact upload passed.
- Added `RoomMediaLibraryRepositoryTest.kt` in commit `a718e7223f60b605829bf8702d987593d7a0c73d`. Three tests cover: partial refresh retaining unseen folders and their acknowledgement/discovery timestamps; full refresh removing folders no longer represented by available media; and empty partial refresh retaining the existing folder index.
- CI run [45155690835](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45155690835) completed successfully for the repository tests. The debug job's `:app:testDebugUnitTest` and debug APK build passed; Room schema upload passed; signed release build and apksigner verification passed; release APK uploaded.
- Downloaded and inspected the generated Room schema artifact from that run. It contains `com.northstar.player.core.database.NorthstarDatabase/1.json`, formatVersion 1, database version 1. The artifact is generated and retained by CI; it has not yet been committed into the repository.
- Artifacts from run 45155690835: `northstar-room-schema`, `northstar-debug-apk`, and `northstar-release-apk`.
- These results verify JVM unit tests and CI packaging/signing only. They do not establish device/runtime behavior, scanner integration against a real MediaStore, or Xiaomi 14 testing.
- Remaining scan-reliability work: add direct scanner tests for null cursor/error handling, bounded 400-row batching and cancellation/failure timestamp preservation; review scan concurrency and permission reselection; then move into the approved three-mode library UX.


## Follow-up scan concurrency review — 2026-11-23

- Reviewed the WorkManager scheduler and found that foreground refresh and 12-hour periodic reconciliation use separate unique-work names. That permits the two worker types to overlap.
- Added a scanner-instance `Mutex` in commit `77df249f1712139e20f36d569576b53c472e5b42` so requests reaching the shared application scanner serialize rather than interleave MediaStore batches, folder reconciliation, and scan-state updates.
- CI run [45156625321](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45156625321) is queued/in progress at the time of this update. Do not claim the mutex change is build-verified until both debug and signed-release jobs pass.
- This is in-process serialization through the singleton scanner in `NorthstarApplication`; it is not a cross-process database lock. The current WorkManager configuration uses the app's default process. If a future manifest assigns workers to another process, revisit this guarantee.
- Latest confirmed passing test/build run remains [45155690835](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45155690835), which includes the three folder reconciliation tests and signed APK verification.


## Three-mode library navigation and CI verification — 2026-11-23

### Source implementation
- Commit `640ea6b088c14afdde7103213a925d915642a176` (`Integrate three-mode library navigation UI`) adds the first native UI integration for the three approved library modes.
- The Activity observes Room-backed video and folder Flows and renders Video Folders, Files Only, and Nested View. Video Folders is the default.
- Folder taps open Nested View at that folder; Back navigates to the parent and then returns to Video Folders at the root.
- The top bar follows the approved order: Northstar title, You/Profile, unified Library Controls, Search. The controls menu contains Sort, View Mode, and Layout groups.
- The basic You page lists Favorites, Watch History, Private Library, and Settings.
- This is an implementation milestone, not completion of the approved library experience or a user-visible release milestone.

### CI verification for commit 640ea6b
- GitHub Actions run [45158767229](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45158767229) completed successfully for branch `phase-0/release-signing` and commit `640ea6b088c14afdde7103213a925d915642a176`.
- `debug-apk`: completed successfully. Unit tests and debug APK build passed; Room schema and debug APK artifacts uploaded.
- `release-apk`: completed successfully. Signed release APK build passed, the APK signature verification step passed, and the release APK artifact uploaded.
- Artifacts: `northstar-room-schema` (ID `14810401053`), `northstar-debug-apk` (ID `14809134305`), and `northstar-release-apk` (ID `14809971481`). Artifacts expire on 2027-02-21.
- This verifies JVM unit tests/build and CI signing/packaging for this exact commit. It does not establish runtime correctness, upgrade compatibility, or Xiaomi 14 acceptance. No device test is claimed.

### Current UI limitations — not yet implemented
- You-page destinations are placeholders that display a Toast; Favorites, Watch History, Private Library and Settings are not functional destinations yet.
- The Create Folder control is a placeholder; SAF folder creation is not implemented.
- Pull-to-refresh and the Private Library pull-and-hold quick-access gesture are not implemented.
- Folder NEW indicators are not implemented.
- Empty folders are not discoverable through the current MediaStore RELATIVE_PATH index; SAF tree selection and empty-directory indexing remain required.
- Folder sorting is currently alphabetical; reliable aggregate size and provider-derived folder dates have not been implemented.
- The player remains the basic Media3 PlayerView. The approved custom controls, playback context/previous-next, resume, subtitle/audio-track UI, gestures, Headphone Mode and PiP are not implemented.
- The current UI is a functional integration shell, not final visual polish and not a complete release.

### Next engineering priorities
1. Continue library foundations without changing the Room schema casually: design SAF tree grants and folder indexing for empty directories/removable storage, then add folder creation using provider-supported SAF APIs.
2. Define reliable folder metadata aggregation (size, Date Added, Date Modified) and introduce any required Room migration with exported schema and migration tests.
3. Implement pull-to-refresh and NEW-state lifecycle; only then add the distinct pull-and-hold Private Library shortcut with authentication.
4. Replace You-page placeholders as the corresponding Favorites, History, Private Library and Settings workflows become functional.
5. Keep the custom player, Headphone Mode and PiP behind their architecture/security/lifecycle gates.
6. Preserve one coordinated product release. Do not ask the user to install this intermediate APK; wait for the integrated release gates and then request Xiaomi 14 acceptance testing.

## Master continuation checkpoint — 2026-11-23

- Created the refreshed master handover document: `Northstar_Master_Continuation_Pack_2026-11-23.docx`.
- This checkpoint consolidates the approved eight product areas, current native architecture and implementation history, latest source milestone `640ea6b088c14afdde7103213a925d915642a176`, successful CI run `45158767229`, known UI limitations, and the next storage engineering sequence.
- Latest CI evidence remains: debug unit tests/build and Room schema artifact upload passed; signed release build and apksigner verification passed. No Xiaomi 14 acceptance test or signed upgrade test is claimed.
- Read-only storage audit found that Room's `ExternalTreeGrantEntity` / DAO already exist, but SAF tree selection, persisted-grant workflow, recursive SAF indexing, empty-directory discovery, USB/SD browsing, and provider-backed folder creation are not implemented.
- Next engineering priority: design and implement SAF tree selection and persisted grants, recursive indexing of nested/empty directories and video documents, source-aware identity, disconnect/revocation handling, and automated tests. Folder creation follows once provider access is reliable.
- No application source code was changed in preparing this checkpoint.
- The DOCX is the human-readable handover artifact. Keep this `PROJECT_STATUS.md` as the concise live engineering status. Update both after major milestones and before each new chat.


## SAF external-storage foundation — 2026-11-23

- Implemented on `phase-0/release-signing` in commits `96bd1962b811887168feb75397ddc8262a6797a2`, `fab4b3e410c0774daf2198b3ef1897784b756bea`, `aa09ff5befb3d0de7056884e003b9ef1fd73dcb0`, and `6cf9cdb1ed77bfb6440ba9696ea1083af362eb1c`.
- Added persistable Storage Access Framework tree grants, with read/write preference and read-only fallback.
- Added stable SHA-256 source identities derived from selected tree URIs; raw tree URIs remain only in the local grant table.
- Added recursive SAF traversal of directories and video documents, including empty directories, using bounded 400-item Room writes.
- SAF items use provider-supplied modified dates where available. Date Added remains unknown when the provider does not supply it; no timestamps are fabricated.
- Folder hierarchy records preserve prior discovery and acknowledgement timestamps. Library projections now scope folder metrics and folder-page video results by source identity, preventing collisions between separate storage providers.
- SAF scans serialize. Old media rows are marked unavailable and folder indexes replaced only after a complete successful traversal. Permission loss or provider disconnection marks that source unavailable without deleting its stored records.
- Added an External Storage management entry from Nested View, including folder selection and manual re-indexing. Provider reachability is checked in addition to the persisted permission; unavailable entries can be reselected.
- No Room schema migration was required. The existing `external_tree_grants` entity/table and DAO were reused.
- CI run [45170360023](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45170360023) passed for commit `6cf9cdb1ed77bfb6440ba9696ea1083af362eb1c`: unit tests, debug APK build, Room schema artifact upload, signed release APK build, apksigner verification, and artifact uploads all succeeded.
- This is CI/build evidence only. No SAF provider integration test on a physical device and no Xiaomi 14 acceptance test have been performed. Do not deliver this intermediate APK as a public/user release.
- Remaining storage work: automated provider-level tests, verify SAF playback on device, removable-media reconnect/revocation testing, and provider-backed folder creation after the access workflow is proven.


## SAF snapshot isolation hardening — 2026-11-23

- Latest commit before this hardening: `1aa89de3eb2ed990e4f34e677ea18d11be108853`; its GitHub Actions run [45170764428](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45170764428) completed successfully for both debug and signed-release jobs, including unit tests, Room schema artifact upload, release signing verification and APK artifacts.
- Code review identified a partial-scan visibility risk: SAF media rows were upserted as available before the provider traversal had completed.
- SAF rows are now staged with `isAvailable=false`. A successful complete traversal activates only rows from that scan generation and then retires older rows. A failed or cancelled traversal therefore cannot publish a partial new generation.
- Permission loss or provider I/O failure marks the source's media unavailable but retains the persisted media records and folder hierarchy; it no longer deletes the folder index. Reconnection/reselection can re-index the same source.
- A missing persisted grant is handled explicitly: the grant and source are marked unavailable and scan error state is recorded, while stored folder history remains.
- No Room schema change is needed; the change adds a DAO update query only.
- This hardening is source-committed but requires a fresh CI run before it can be called build-verified. Device-level SAF provider testing remains outstanding.


## SAF snapshot publication transaction — 2026-11-23

- Follow-up hardening moves the final SAF publication into a dedicated Room `@Transaction` DAO operation.
- The transaction replaces the source's folder hierarchy, activates the fully staged media generation, retires older media rows, and records the successful scan timestamp together. This closes the gap where a database exception between separate DAO calls could expose a mixed generation.
- No entities or columns changed; database schema version remains 1. The new DAO adds transaction/query methods only.
- The scanner's application wiring now injects the snapshot publisher. A fresh GitHub Actions run is required to validate Room code generation and compilation before treating this as verified.


## SAF scan generation uniqueness and latest validation — 2026-11-23

- Scan generation timestamps now advance beyond both the last successful scan and the newest indexed row for that source. This prevents a fast retry from reusing the same generation timestamp and accidentally activating stale rows left by a failed attempt.
- Extracted the generation calculation into `SafScanGeneration` and added three JVM unit tests covering failed-attempt advancement, current-clock precedence, and strict advancement beyond the last successful scan.
- Updated the Room repository test fake to match the current `MediaItemDao` interface after moving snapshot publication into `SafSnapshotDao`.
- Latest source commit: `704f89b4d4f03215f3d5235e2b1749b2b157ff89`.
- CI run [45171712123](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45171712123) was still in progress at checkpoint time. Do not claim this commit is build-verified until both debug unit tests/build and signed-release/apksigner jobs complete.
- Earlier attempt [45171603064](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45171603064) encountered Maven Central HTTP 429 dependency throttling during debug dependency resolution; its release job was separate. This is an external dependency-fetch failure, not evidence of a source compilation defect.
- Next gate: obtain a clean CI pass for the current head, then add provider-level SAF tests and implement folder creation only after persisted read/write grant behavior is verified. Physical-device tests on removable storage remain required.


## SAF isolated staging table and database migration — 2026-11-23

- Code review found that writing staged scan rows directly into `media_items` still replaced canonical rows sharing the same stable media key. That could hide the previous successful record before the new scan completed.
- Replaced in-place staging with a dedicated `saf_staged_media` Room table. Scan batches now write only to this table; the visible `media_items` table is untouched until publication.
- The final Room transaction copies the complete staged generation into canonical media rows, marks only missing prior rows unavailable, replaces the folder hierarchy, marks the grant available, records successful scan state, and clears staging. An interrupted or failed scan cannot overwrite the last published media snapshot.
- Stale staging generations are cleared before a new scan after their generation timestamp has been considered, so a retry always uses a newer generation.
- Database version is now 2, with explicit `MIGRATION_1_2` creating the staging table and its indexes. The migration is registered in the database builder; destructive fallback is not enabled.
- This is an application data-model migration and must be verified by CI Room schema export and migration validation before proceeding to folder creation. Physical-device upgrade testing remains required.


## SAF staging-table follow-up and CI checkpoint — 2026-11-23

- Confirmed CI run [45171712123](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45171712123) completed successfully for commit `704f89b4d4f03215f3d5235e2b1749b2b157ff89`: JVM unit tests, debug APK, Room schema artifact, signed release APK and apksigner verification all passed.
- A deeper review then found that staging rows inside the canonical `media_items` table could replace an existing row with the same stable media key before the new scan completed. This was corrected in commit `d3ec19628b529da7df3f48c10d2edf7c553c22a7`.
- Added a dedicated `saf_staged_media` table. SAF batches now write to this isolated table. The final Room transaction copies the complete generation to canonical media, retires missing records, replaces folders, updates grant availability and scan state, and clears staging.
- Added Room database version 2 and explicit migration `MIGRATION_1_2`; the migration creates the staging table and indexes without destructive fallback.
- Updated scan-generation unit test parameter names in commit `c103832dead2cfb4bc0b15770f3b757c3bcaf0e8`.
- CI run [45172461382](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45172461382) tested the pre-test-fix commit `d3ec196`; its debug unit-test compilation failed only because the tests still used the old named parameter. The release job is separate. This run does not validate the current head.
- CI run [45172696168](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45172696168) is the validation run for current code commit `c103832dead2cfb4bc0b15770f3b757c3bcaf0e8`; at checkpoint update it was still running. Do not claim migration/schema/build success until it completes.
- Next gate: confirm Room v2 schema export and both CI jobs. Then add a real migration test before relying on v1-to-v2 upgrade behavior; provider/device tests remain outstanding. Do not distribute intermediate APKs.


## Room migration verification coverage — 2026-11-23

- CI run [45172696168](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45172696168) completed successfully for code commit `c103832dead2cfb4bc0b15770f3b757c3bcaf0e8`. Both jobs passed: JVM unit tests/debug APK/Room schema export and signed release APK/apksigner verification.
- Inspected the uploaded Room schema artifact: it exports database version 2 and the `saf_staged_media` table with the intended composite primary key and source/generation indexes.
- Added an Android instrumentation test that constructs the exact exported v1 table/index layout, inserts an existing video row, opens the database using the actual `MIGRATION_1_2`, and verifies the old row survives and the new staging table exists empty.
- CI now launches an API 35 x86_64 emulator and runs `:app:connectedDebugAndroidTest` as part of the debug validation job.
- Migration instrumentation implementation commit: `25b133cfa550163c878191702adc4b9eedd13987`.
- Validation run [45173392724](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45173392724) is pending/in progress for the new instrumentation test and workflow. Do not treat the migration test as passed until this run completes.
- This test validates the v1-to-v2 database migration; it does not replace physical-device SAF provider tests for USB/SD discovery, permission revocation, disconnect/reconnect, and playback.


## Migration test hardening — 2026-11-23

- Reviewed the actual exported Room v1 and v2 schema artifacts from GitHub Actions. The v1 fixture's table columns, primary keys, and indexes match the exported v1 schema; v2 adds only `saf_staged_media`.
- Strengthened the Android migration test to seed and verify preservation of representative rows across all existing user-data tables (media, favorites, playback progress, watch history, folders, scan state, new-content state, and external tree grants), and to include the v1 Room identity hash.
- Added a 25-minute timeout to the emulator migration step so an unavailable or stalled emulator cannot leave the workflow indefinitely in progress.
- Earlier migration validation run [45173392724](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45173392724) remains in progress at the last API check; its release build and JVM/debug build passed, but the emulator migration test has not reported a result. The strengthened test must pass in a fresh run before storage-write features are unblocked.


## Instrumentation test coroutine fix — 2026-11-23

- Static review caught that the migration test calls the suspend DAO method `findByKey`; the earlier test source used a regular JUnit method and would not compile when Android instrumentation sources were compiled.
- Wrapped the test body in `runBlocking` so it can invoke Room suspend APIs correctly.
- The prior workflow run [45175159918](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45175159918) had passed JVM unit tests and release signing but remained at the emulator instrumentation step at the last check. Its instrumentation result is not accepted as passed.
- This fix triggers a fresh full validation run. Folder creation remains gated on the fresh emulator migration test.


## Emulator CI boot configuration fix — 2026-11-23

- Inspected the failed emulator job logs from run [45173392724](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45173392724). The emulator did not boot because the hosted Linux runner had no hardware CPU acceleration; the emulator explicitly reported software CPU emulation was disabled and ADB could not connect to port 5554.
- Configured the emulator step with `ANDROID_I_WANT_MY_TCG=yes`, the emulator's documented opt-in for TCG software CPU emulation when hardware acceleration is unavailable.
- The migration test now also runs inside `runBlocking` because its Room DAO lookup is suspend.
- Fresh run [45175664390](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45175664390) was queued before this emulator configuration fix and should not be treated as the final gate.
- This commit triggers a new validation run. The emulator test must complete successfully before folder creation is implemented.


## SAF provider-backed folder creation — CI verified — 2026-11-23

- Room v1-to-v2 migration gate is now verified by GitHub Actions run [45175777323](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45175777323): Android instrumentation test completed 1/1 with zero failures or skips; JVM tests, debug APK, signed release APK, and apksigner verification also passed.
- Emulator boot now uses Android emulator TCG software CPU emulation on hosted Linux; the successful run took approximately 5m44s for the instrumentation Gradle task.
- Implemented provider-backed folder creation. The feature validates persisted write permission, restricts creation to an indexed SAF directory, rejects unsafe names, uses `DocumentsContract.createDocument`, and refreshes the source index after creation.
- The subsequent clean validation run is [45179931782](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45179931782); both debug and signed-release jobs passed, including the migration instrumentation test and apksigner verification.
- No physical USB/SD provider test or Xiaomi 14 acceptance test has yet been performed. Do not distribute the intermediate APK.

## SAF provider-backed folder creation — CI verified — 2026-11-23

- Implementation commit: [5bfc5c2b9a586c315ee6aafc16fa17ca1c6f4271](https://github.com/afghanbark573-rgb/Northstar-Player/commit/5bfc5c2b9a586c315ee6aafc16fa17ca1c6f4271).
- GitHub Actions run [45179931782](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45179931782) completed successfully for this exact commit. Both `debug-apk` and `release-apk` jobs passed.
- Debug job: JVM unit tests and debug APK build passed; Android Room v1-to-v2 migration instrumentation test passed; Room schema and debug APK artifacts uploaded.
- Release job: signed release APK build passed; `apksigner` signature verification passed; signed release artifact uploaded.
- Folder-creation implementation adds SAF root browsing, nested folder navigation, provider-backed directory creation through `DocumentsContract.createDocument`, strict validation of folder names, and checks for both stored and currently active Android read/write permissions. The source is rescanned after creation.
- CI validates compilation, unit tests, Room migration, packaging, and signing. It does **not** validate real removable-storage provider behavior. USB/SD creation, reconnect/revocation behavior, SAF playback, and full Xiaomi 14 acceptance remain untested on physical hardware.
- Treat this as an internal engineering milestone only. Do not distribute the intermediate APK; all eight approved product areas remain one coordinated release.


## SAF folder-provider policy test seam — implementation in progress — 2026-11-23

- Refactored folder-creation policy to depend on a narrow `SafFolderProvider` boundary. Production uses `AndroidSafFolderProvider`, which performs the real persisted-permission check and `DocumentsContract.createDocument` call; deterministic JVM tests use a fake provider.
- Added tests for successful nested creation, missing grants, unavailable storage, read-only stored grants, revoked live write access, provider refusal, unsupported operations, provider permission denial, provider name rejection, invalid source/folder identity, and unsafe names.
- This test seam verifies application policy and error mapping, not every vendor DocumentsProvider implementation. Actual USB/SD provider behavior and physical Xiaomi 14 acceptance remain mandatory later gates.


## SAF DocumentsProvider integration test — implementation in progress — 2026-11-23

- Added a test-only Android `DocumentsProvider` registered by the `androidTest` manifest. It exposes an in-memory root and nested directory and implements Android's real provider contract, including `createDocument` and child listing.
- Added an instrumentation test that invokes the production `AndroidSafFolderProvider` adapter, creates a directory inside a nested provider folder through `DocumentsContract.createDocument`, then queries the provider and verifies the new directory appears with directory MIME type.
- This provides a controlled end-to-end Android framework test for the provider adapter. It does not emulate removable-media hardware, vendor-specific provider quirks, or Android's full user-granted persisted-permission picker flow.
- Source commit: [6dc49bb0e8625cde8200f827743cb03ae9b04648](https://github.com/afghanbark573-rgb/Northstar-Player/commit/6dc49bb0e8625cde8200f827743cb03ae9b04648). CI run [45188897334](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45188897334) is validating the new instrumentation test; do not mark it passed until the debug job completes.
- Physical USB/SD testing and Xiaomi 14 acceptance remain outstanding. No intermediate APK distribution.


## DocumentsProvider instrumentation compile fix — validation retry pending — 2026-11-23

- The first integration-test validation, [run 45188897334](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45188897334), failed at `compileDebugAndroidTestKotlin` before executing instrumentation. The precise compiler errors were three unresolved `toTypedArray()` references in `TestSafDocumentsProvider.kt`; this was a test-source compile issue, not a runtime DocumentsProvider failure.
- Corrected the provider to use the supplied projection array directly and changed the row mapper to accept `Array<out String>`.
- Fix commit: [c84d31d8d3b0e5a38cbcd9e8bfbc38a39418d03b](https://github.com/afghanbark573-rgb/Northstar-Player/commit/c84d31d8d3b0e5a38cbcd9e8bfbc38a39418d03b).
- Fresh validation [run 45224911347](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45224911347) is in progress. Unit-test/debug compilation, instrumentation execution, and release signing must all be checked before marking the integration test verified.
- Prior run [45186122294](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45186122294) passed both jobs for the provider-policy test seam and migration coverage, but predates the new DocumentsProvider integration test.


## DocumentsProvider instrumentation follow-up — remaining compile fix — 2026-11-24

- Validation run [45224911347](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45224911347) completed with overall failure.
- JVM unit tests and debug APK build passed. Android instrumentation did not execute because `compileDebugAndroidTestKotlin` still found one remaining unresolved `toTypedArray()` at line 62 in `TestSafDocumentsProvider.kt`.
- The signed release APK build and `apksigner` verification both passed in the separate release job.
- Corrected the remaining projection handling to use the supplied `Array<out String>` directly (or the default columns), matching the earlier two corrections.
- Fix commit: [b2f43954f60c666a8ce394e3c8ae6bf3f9fcb3aa](https://github.com/afghanbark573-rgb/Northstar-Player/commit/b2f43954f60c666a8ce394e3c8ae6bf3f9fcb3aa).
- A fresh full CI run is required. The DocumentsProvider integration test remains unverified until the instrumentation actually executes and passes.
- No intermediate APK distribution. Physical USB/SD provider behavior and Xiaomi 14 acceptance remain outstanding.


## DocumentsProvider instrumentation runtime failure and diagnostics — 2026-11-24

- CI run [45356712186](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45356712186) confirms the remaining Kotlin test-source compile error is fixed: JVM unit tests and debug APK build passed, and Android instrumentation launched both tests.
- The existing Room migration instrumentation test passed. The new `SafFolderProviderIntegrationTest.createsAndListsDirectoryInsideNestedDocumentProviderFolder` failed; Gradle then reported the instrumentation process crashed. The job log did not include the assertion/exception detail needed to identify the root cause.
- The separate signed release job passed, including keystore restore, release APK build, and `apksigner` verification. Overall workflow remains failed because the debug instrumentation job failed.
- Added CI diagnostics in commit [e0189d426146150d895ed2b0a045ca1b5f725a10](https://github.com/afghanbark573-rgb/Northstar-Player/commit/e0189d426146150d895ed2b0a045ca1b5f725a10): always upload Android instrumentation reports/results and capture emulator logcat after the instrumentation task, including on test failure.
- Next step is to inspect the uploaded test report and logcat from the new run, then make a root-cause fix. Do not infer the cause from the current summary.
- SAF DocumentsProvider integration remains unverified. Physical USB/SD provider behavior and Xiaomi 14 acceptance remain outstanding. No intermediate APK distribution.


## Emulator-runner shell isolation fix — 2026-11-24

- CI run [45371421075](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45371421075) completed with failure, but the failure is now conclusively identified from the complete job log: the emulator-runner executes each line of its multiline `script` in a separate shell. The standalone `cd android` therefore did not persist into the Gradle command.
- Gradle ran from the repository root and failed immediately with: `Directory '/home/runner/work/Northstar-Player/Northstar-Player' does not contain a Gradle build.` The Android instrumentation tests did **not** execute in this run.
- The diagnostics upload step ran but found no report/log files, confirming there is no new runtime test result to interpret.
- The separate release job passed: signed release APK build, signature verification, and artifact upload. The JVM unit tests and debug APK build also passed.
- Corrected the emulator script so directory change, diagnostics setup, Gradle execution, exit-code capture, and logcat capture run inside one shell block. This preserves the working directory and ensures logcat capture runs after Gradle whether tests pass or fail.
- This is a CI orchestration fix only; no production SAF code or test assertions were changed. A fresh workflow run must now execute both Android instrumentation tests before the DocumentsProvider integration can be considered validated.
- Physical USB/SD provider behavior and Xiaomi 14 acceptance remain outstanding. Do not distribute an intermediate APK.


## Emulator-runner script command-line correction — 2026-11-24

- Follow-up CI run [45403648975](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45403648975) confirms the previous shell-block workaround was also incompatible with the emulator runner: it launches each script line as an individual `sh -c` command, so the opening line containing `cd android && {` was parsed without the closing brace and failed with a shell syntax error before tests ran.
- Replaced the multiline shell block with one physical script line containing the directory change, diagnostics directory creation, Gradle test invocation, exit-code capture, logcat capture, and final exit. This matches the runner's per-line execution model while keeping the entire sequence in one shell.
- The run again confirms JVM unit tests/debug APK and signed release build/signature verification pass; instrumentation still did not execute.
- No production code or test logic changed. The next run is the first meaningful retry after correcting the runner script format.
- The DocumentsProvider integration test remains unverified; no intermediate APK distribution. Physical USB/SD and Xiaomi 14 acceptance remain outstanding.


## SAF DocumentsProvider test-process isolation — validation pending — 2026-11-24

- Inspected the full diagnostics artifact from CI run [45408437533](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45408437533), including the HTML test report, XML/textproto results, per-test logcat, emulator logcat, and UTP log.
- The one-line emulator-runner command is now working: both instrumentation tests actually launched. The Room migration test passed. The SAF DocumentsProvider test failed after Android started the provider in the separate `com.northstar.player.test` process.
- The emulator log shows repeated system-process ANRs and provider-startup timeouts. ActivityManager then killed the test-APK provider process with `timeout publishing content providers`, reported that its launch process had become null, and the instrumentation process subsequently died. The test report contains no actionable assertion stack trace. This is evidence of a provider process-startup/test-environment failure; it does not prove a defect in production SAF folder creation.
- Root-cause mitigation: move the in-memory test provider from the androidTest APK into the debug-only app source set. The instrumentation test first starts the target app and then exercises the same `DocumentsContract` path, so the provider can share the already-started target-app process instead of requiring a separate test-APK provider process. The provider remains excluded from release builds.
- This changes test packaging/isolation only; production SAF adapter, folder policy, and assertions remain unchanged. A fresh CI run must confirm whether this removes the startup failure.
- Latest run: JVM unit tests/debug APK and signed release APK/signature verification passed; Android instrumentation failed. No intermediate APK distribution. Physical USB/SD provider behavior and Xiaomi 14 acceptance remain outstanding.


## Current SAF test-provider correction — 2026-11-24

- Native source commit `07b96f31a4cbab9cfcad1e0ccfa4f81232142960` moved the in-memory DocumentsProvider to the debug app source set, but run [#56 / 45411300973](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45411300973) then exposed the exact remaining setup defect.
- Run #56 evidence: JVM unit tests and debug APK build passed; release APK build, `apksigner` verification and artifact upload passed. Android instrumentation launched zero tests because the target app process crashed while installing `TestSafDocumentsProvider`.
- Exact exception: `java.lang.SecurityException: Provider must be protected by MANAGE_DOCUMENTS`, thrown by `DocumentsProvider.attachInfo`. This is a debug test-provider manifest configuration error, not a production SAF assertion failure.
- Corrective source commit: `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b` ([commit](https://github.com/afghanbark573-rgb/Northstar-Player/commit/3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b)). Added `android:permission="android.permission.MANAGE_DOCUMENTS"` to the debug-only provider declaration. The provider remains in `src/debug` and is excluded from release builds. Production SAF implementation and test assertions were not changed.
- A new CI run should be triggered by this manifest change. Confirm that it tests the exact new HEAD and inspect both Room migration and SAF DocumentsProvider instrumentation results before closing the gate.
- No intermediate APK distribution. Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed upgrade test remain outstanding.


## CI run #57 — MANAGE_DOCUMENTS correction under test — 2026-11-24

- Run [45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513) is testing exact source commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`.
- At the latest poll, both `debug-apk` and `release-apk` jobs were `in_progress`. Debug unit tests/build and signed release build were still running; instrumentation had not started. No artifact or test result was available yet.
- The source correction is the debug provider manifest declaration `android:permission="android.permission.MANAGE_DOCUMENTS"`. Wait for final results; specifically verify that instrumentation runs tests rather than crashing during provider installation.


## CI run #57 — SAF tree-descendant contract failure and test-provider fix — 2026-11-24

- Run [45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513) tested exact commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b` and completed with overall failure.
- JVM unit tests and debug APK build passed. The Room v1-to-v2 migration instrumentation test passed (1/1). The SAF DocumentsProvider integration test executed but failed (1/1) with:
  `SecurityException: Document nested-parent is not a descendant of test-root`
  from `DocumentsProvider.enforceTree`, reached through `DocumentsContract.createDocument` and the production `AndroidSafFolderProvider.createDirectory` adapter.
- This is now a deterministic test-provider contract defect, not an emulator crash and not evidence that production SAF folder creation is broken. The in-memory debug provider modeled parent IDs and child listing but did not override `DocumentsProvider.isChildDocument`; Android therefore rejected a legitimate nested document URI when enforcing the tree URI.
- The signed release job passed, including keystore restore, release APK build, `apksigner` verification and artifact upload.
- Corrected the debug-only test provider in commit [35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4](https://github.com/afghanbark573-rgb/Northstar-Player/commit/35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4): implemented synchronized ancestor-chain traversal in `isChildDocument(parentDocumentId, documentId)`. This reports nested descendants to Android's tree-URI enforcement and returns false for missing/broken parent chains. Production SAF adapter, policy, database and test assertions were not changed.
- A fresh CI run is required to validate the correction. The SAF instrumentation gate remains open until both instrumentation tests pass on the new source commit.
- No intermediate APK distribution. Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade test remain outstanding.


## CI run #58 — nested SAF descendant fix under validation — 2026-11-24

- Run [45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) was triggered by source commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`, which implements `DocumentsProvider.isChildDocument` in the debug-only in-memory provider.
- At the latest poll, the workflow remained `in_progress`. Both debug and release jobs were running; the debug unit-test/build step and signed release build step were active. Android instrumentation had not started and no artifacts were available.
- Do not infer a pass from the code change or the earlier build results. Verify both instrumentation tests and signed release signature verification against this exact source commit.
- SAF integration gate remains open. No intermediate APK distribution; physical storage and Xiaomi 14 gates remain separate.


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

 
## CI run #58 — SAF instrumentation gate passed — 2026-11-24

- Workflow [run #58 / 45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) completed with overall conclusion **success** on exact source commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
- Debug job completed successfully: `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:connectedDebugAndroidTest` all succeeded.
- Instrumentation diagnostics artifact `northstar-instrumentation-diagnostics`, ID `14946877392`, confirms **2 tests, 0 failures, 0 errors, 0 skipped** on Android emulator API 35:
  - `NorthstarDatabaseMigrationTest.migrationFromV1PreservesLibraryAndCreatesIsolatedStagingTable` — PASSED (1/1).
  - `SafFolderProviderIntegrationTest.createsAndListsDirectoryInsideNestedDocumentProviderFolder` — PASSED (1/1).
  - Test log ends `OK (2 tests)`; Gradle XML and textproto both report both tests passed.
- This validates the debug-only test-provider descendant-chain correction. It does not prove real USB/SD provider behavior, real picker/persisted-grant lifecycle, or production SAF compatibility across devices.
- Release job completed successfully: release keystore restore, `:app:assembleRelease`, APK signature verification with `apksigner`, and signed artifact upload all passed.
- Signed artifact `northstar-release-apk`: ID `14944179159`, ZIP SHA-256 `f9a71cc8718d67485a41c3663a194a8dcfd08d68d3548b94e45925fe124dc0eb`. This remains a CI verification artifact only; **do not distribute as an intermediate release**.
- Debug artifact `northstar-debug-apk`: ID `14948455778`, ZIP SHA-256 `826bbccec895b458bda96ca44a0daf79ba7b9e69bf00831715ead1cfe63b93b9`.
- Room schema artifact ID `14948316137`, SHA-256 `d2404d01b3392284f1a7ffb90c3876bf04ecc7c3fef98f9ae606f871b5546a48`.
- Instrumentation diagnostics artifact SHA-256 `28c1b4beb37d6008e5d965ddd3d0500ac9a6da56557435cc3b50e9d3966bdfa2`.
- No production application source changes were part of this validation commit; the code under test is the debug-only in-memory DocumentsProvider fix. No APK was distributed.
- Separate unverified device gates remain: physical USB/SD storage, actual picker and persisted grant lifecycle, Xiaomi 14 acceptance, and signed install-over-existing upgrade testing.
- **Next engineering milestone, subject to live-source inspection:** Library Browser folder metadata aggregation and sorting. Before edits, re-inspect current source, establish Main Quest/Side Quests, affected files, risks, acceptance criteria and regression tests. Keep the coordinated eight-area public release and no-intermediate-APK rule.


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
