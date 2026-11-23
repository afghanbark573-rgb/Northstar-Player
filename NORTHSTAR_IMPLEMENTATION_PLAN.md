# Northstar Player — Integrated Implementation Plan

**Prepared:** 23 November 2026  
**Status:** Engineering proposal for user review — NOT yet approved for implementation  
**Product authority:** `NORTHSTAR_PRODUCT_SPEC.md`  
**Branch audited:** `phase-0/release-signing`  
**Repository:** https://github.com/afghanbark573-rgb/Northstar-Player

> This plan translates the eight locked product discussions into an engineering approach. It does not change source code, select a final playback engine, or authorize an APK release. User-approved product requirements remain locked; technical choices marked **Proposed** are recommendations for review.

---

## 1. Executive direction

Build Northstar as a native, local-first Android media player with a folder-focused library, a custom playback experience, a separately protected private library, and dependable support for internal and removable storage.

Deliver all eight product areas as **one coordinated product release**. Work may proceed through internal engineering and test milestones, but do not ship separate public releases for individual discussion areas or create interim APKs merely to repackage existing behavior.

The current native branch is a starting prototype, not the target product. Implementation must be based on the approved product specification and verified against the live source at the time work begins.

### Non-negotiable safeguards

- Preserve application ID/namespace `com.northstar.player`.
- Preserve the permanent release-signing certificate and existing signed release workflow.
- Never expose or commit signing secrets or keystore bytes.
- Do not merge `main` and `phase-0/release-signing` casually.
- Do not claim a CI build proves real-device correctness.
- Do not claim Xiaomi 14 testing until the user has tested the APK.
- Do not silently drop requirements that prove technically unsupported; report the limitation and seek a decision.
- Keep the web prototype 0.8 frozen unless a material native-development blocker appears.

---

## 2. Live source baseline and gap assessment

### 2.1 What was inspected

A read-only inspection was performed against the current `phase-0/release-signing` branch. Files inspected included:

- `android/app/src/main/java/com/northstar/player/MainActivity.kt`
- `android/app/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- `android/settings.gradle.kts`
- `.github/workflows/android-build.yml`
- `PROJECT_STATUS.md`
- `NORTHSTAR_PRODUCT_SPEC.md`

No application source code was changed and no build was triggered during this inspection.

### 2.2 Current implementation observed

- One `MainActivity.kt` currently owns the library UI, permission request, MediaStore query, filtering, sorting, folder grouping, player setup, and lifecycle behavior.
- MediaStore scanning is performed synchronously from the Activity flow rather than through a background repository/dispatcher.
- The current library has basic search, Folders/All Videos switching, List/Grid switching, Recent/A–Z/Largest sorting, and grouping using MediaStore relative paths.
- The folder display is a first grouping pass; it is not the approved three-mode library or full nested folder browser.
- The current UI is a functional shell and does not implement the approved AMOLED black-and-white design, top navigation, or You page.
- Playback uses standard Media3 `PlayerView` controls. The approved custom player, gesture system, time-display toggle, hold-for-2×, subtitle/audio track UI, persistent resume, and background audio service are not implemented in the inspected Activity.
- The app Gradle file currently includes Core KTX, Activity KTX, Media3 ExoPlayer and Media3 UI. No Room, coroutine, WorkManager, Paging, or test dependencies were found in that file.
- The manifest includes video permissions, including Android 14 selected-visual-media permission, but the Activity callback does not model full, selected/partial, denied, and reselection states as distinct library states.
- No automated `test` or `androidTest` source tree was present in the inspected source listing.
- Release signing and the GitHub Actions signed APK workflow are already configured. Preserve them.

### 2.3 Gap matrix

| Product area | Current evidence | Required target | Gap |
|---|---|---|---|
| Product UI | Functional prototype shell | AMOLED black/white, top navigation, You page | Major |
| Library modes | Folders/All Videos toggle | Video Folders, Files Only, Nested View | Major |
| Folder navigation | Relative-path grouping | Real hierarchy and folder navigation | Major |
| Unified controls | Separate inline buttons | One controls area for Sort, View Mode, Layout | Major |
| Persistence | In-memory list/state | Room-backed library metadata, history, favorites, progress | Major |
| Scanning | Synchronous MediaStore query | Background, incremental, lifecycle-safe scanning | Major |
| Permissions | Basic permission request | Full/partial/denied/reselection handling | Major |
| Player | Standard Media3 PlayerView | Custom player and approved controls/gestures | Major |
| Playback memory | Not found | Per-video resume and history | Major |
| Tracks/subtitles | No custom selection UI found | Embedded/external subtitles and audio track selection | Major |
| Private Library | Not found | Encrypted, authenticated, folder-organized private storage | Major |
| USB/SD | No SAF browser found | User-granted removable-storage browsing | Major |
| Tests | No test source sets found | Automated and device regression coverage | Major |
| CI signing | Configured | Preserve and verify | Existing foundation |

**Important:** “Not found” means not present in the inspected source paths; it is not a claim about uninspected historical branches or previous APK internals.

---

## 3. Proposed application architecture

### 3.1 Structure

**Proposed:** Keep one Android application and organize it into maintainable feature and data layers. Avoid putting new features into a replacement mega-Activity.

Suggested package layout:

```
com.northstar.player/
  app/
    NorthstarApplication
    MainActivity
    navigation/
  core/
    model/
    database/
    datastore/
    permissions/
    storage/
    security/
    diagnostics/
    ui/
  feature/
    library/
    folder/
    player/
    you/
    favorites/
    history/
    private/
    settings/
  data/
    library/
    playback/
    private/
    external/
  service/
    playback/
  worker/
    library/
  test/
```

This is a logical package plan, not a requirement to split into many Gradle modules immediately. Start with a single app module and clear boundaries; split modules only when build/test ownership or dependency boundaries justify it.

### 3.2 UI framework

**Proposed default:** Use native Android UI components with a consistent design system. The existing code uses Android Views. A framework change (for example, adopting Jetpack Compose) should be decided deliberately before the final UI build, not mixed into unrelated storage/player work.

Regardless of framework:
- AMOLED black surfaces and white/grey text.
- High contrast and restrained accent use only if later approved.
- Reusable buttons, icon-and-text controls, list rows, folder rows, video tiles, dialogs, and player overlays.
- Touch targets and text remain readable on the Xiaomi 14.
- No bottom navigation.

### 3.3 State and data flow

- Screens observe state from ViewModels or equivalent presentation controllers.
- Repositories own data access and coordinate Room, MediaStore, and SAF.
- Long-running I/O runs off the main thread using Kotlin coroutines.
- UI state survives configuration changes and is restored where appropriate.
- Playback state is owned by the playback layer/service, not by a screen's transient view hierarchy.
- Errors are represented as explicit UI states, not only status text or swallowed exceptions.

---

## 4. Data and persistence design

### 4.1 Room as the persistence layer

Room/SQLite is the approved persistence direction. Proposed core records:

- **MediaItem:** stable source identity, content URI, display name, MIME type, duration, byte size, modified/added dates, source type, relative path/folder identity, availability.
- **Folder:** normalized folder identity, parent identity, display name, source/storage identity, derived video count and child-folder count.
- **Favorite:** media/folder identity and favorite timestamp.
- **PlaybackProgress:** media identity, last position, duration snapshot, completion state, last played timestamp.
- **WatchHistory:** playback session/event information needed to render chronological history.
- **ScanState:** source identity, last completed scan, permission scope, and reconciliation state.
- **NewContentState:** discovery timestamp/acknowledgement state used for folder NEW indicators.
- **PrivateFolder / PrivateMedia:** encrypted private-library hierarchy and encrypted-file metadata; never mix these records into the public MediaStore library.
- **ExternalTreeGrant:** SAF tree URI, persisted permission flags, display name, and availability state.

Use migrations from the first schema version. Do not clear the database on upgrade. Database identity must not depend only on display filename; duplicate filenames can exist.

### 4.2 Source of truth

- MediaStore remains the discovery source for ordinary shared/internal media.
- Room stores app-owned metadata, user choices, scan state, history, favorites, and playback positions.
- SAF is the access path for user-selected USB/SD folders and other document-provider locations.
- Do not copy all public media into private app storage just to build the public library.
- If a MediaStore item disappears, mark/reconcile it and retain or remove user metadata according to an explicit orphan policy; do not silently attach a favorite or history record to a different file with the same name.

### 4.3 History and resume behavior

- Save progress periodically during playback and at pause/stop/end.
- Resume prompt appears for unfinished videos, not for videos at or near completion.
- A completed video can remain in Watch History without being treated as unfinished.
- History is chronological and is the sole recently-watched destination; do not add a separate Recently Played section.
- Private videos must not enter public Watch History.

**Open technical value to set during implementation:** the exact completion threshold and progress-save interval. Proposed starting points are “near the final 5%” for completion and a short periodic save interval, then validate write frequency and reliability.

---

## 5. Media discovery, permissions, and external storage

### 5.1 MediaStore scanning

**Proposed approach:**

1. Determine current permission scope.
2. Query MediaStore on an I/O dispatcher.
3. Read only required columns and process results incrementally.
4. Upsert records into Room in bounded batches.
5. Publish scan progress/state without blocking scrolling or playback.
6. Use a MediaStore content observer to schedule reconciliation when media changes.
7. Use WorkManager for deferrable reconciliation/recovery, not as a substitute for immediate foreground refresh.
8. Handle cancellation, process death, and partial scans safely. A failed scan must not erase the last known-good library.
9. Avoid re-reading or re-hashing every file on every app launch.

A scan is complete only when its result has been committed consistently. Store a last-successful-scan marker; never advance it after a failed or interrupted scan.

### 5.2 Permission states

Model at least:

- Full video access.
- Selected/partial video access.
- Permission denied.
- Permission permanently denied / settings route where applicable.
- No accessible videos.
- Access changed since the last scan.

On Android 14+ and later versions, account for selected-media access and reselection. Re-check permissions when the app resumes and re-query the accessible set after the user changes selection. Do not assume a prior permission result remains valid forever.

No broad all-files permission for ordinary video discovery.

### 5.3 USB and SD cards

- Offer an understandable external-storage entry point.
- Use Android's Storage Access Framework (SAF) folder picker where required; ask the user to grant access to a USB/SD folder.
- Persist URI permissions when the provider supports it.
- Browse the selected tree and its nested directories; do not assume all USB/SD content is automatically indexed by MediaStore.
- Keep external sources distinguishable from internal storage.
- Handle removed/disconnected media without crashing. Show an unavailable-source state and restore access when reconnected/reselected.
- Playback should use the granted content URI directly where supported.
- Do not copy removable-storage files into the public library by default.

### 5.4 Unified library identity

MediaStore and SAF may expose different URI formats and metadata. Use a source-aware media identity so an item from a USB tree cannot collide with an internal MediaStore item. Test duplicate appearances if the same removable content is also indexed by MediaStore.

---

## 6. Library and main interface implementation

### 6.1 Main shell

- Launch into the folder-focused library.
- Top navigation contains the frequently used navigation controls; the exact final item set and order must be finalized in the screen design.
- A profile icon opens the **You** page.
- Keep the bottom area free of bottom-navigation controls.
- Keep the player hidden until a video is selected.
- Keep the home screen focused on folders; do not add Favorites, History, Recently Added, or Smart Collections as home sections.

### 6.2 Three library View Modes

Exactly three modes:

1. **Video Folders:** show only folders that contain videos. Do not show empty folders or unrelated folders.
2. **Files Only:** show video files without folder listings.
3. **Nested View:** show the complete folder hierarchy, including main folders, nested folders, and folders containing videos. This is the all-folders hierarchy mode, not a fourth mode.

By Folder / Video Folders is the default starting mode.

Nested View uses normal folder-opening navigation, not an expandable/inline tree. Tapping a folder opens a folder page showing its child folders and files while preserving the actual parent-child hierarchy. Provide:
- A Back control for returning to the previous folder.
- A breadcrumb path near the top showing the current location and allowing navigation to ancestor folders. Keep it compact and usable on narrow phone screens (horizontal scrolling or an equivalent compact treatment is acceptable).
- A Create Folder icon in the toolbar.
- One compact controls icon that opens the unified Sort, View Mode, and Layout options; do not scatter these options across separate toolbar buttons.
- Pull-to-refresh on the folder page to refresh its contents.
Nested View includes empty folders and the complete hierarchy; it is not flattened and is not an expandable tree.

### 6.3 Unified controls

A single, easy-to-find controls area contains:
- Sort (icon + text).
- View Mode (icon + text).
- Layout (icon + text).

List/Grid applies to folders and video files. Do not scatter these controls into unrelated menus. The control area should remain compact and legible.

Sort options:
- Date Added — newest first, using the storage provider's added/indexed date where available.
- Date Modified — newest first, using the file's last-modified date where available.
- A–Z.
- Largest.

Date Added and Date Modified are separate user-facing sort options; do not collapse them into a generic Recent option. The underlying date fields can differ by source (MediaStore versus SAF document provider). If a provider does not expose a reliable date, handle the item consistently and do not invent a timestamp. Keep folder NEW-indicator state separate from these sort keys.

### 6.4 Search and folder indicators

- Search video filenames and relevant folder names/paths.
- Search results should clearly identify their containing folder.
- Folder-level NEW indicators appear only when relevant new content exists.
- NEW state must be stable across app restarts and should clear according to a defined acknowledgement rule (for example, when the folder is opened); finalize that rule before implementation.
- Do not create a Recently Added page/section.

### 6.5 You page

The You page is the single home for secondary destinations and settings. It contains:
- Favorites.
- Watch History.
- Private Library.
- Settings.
- Other secondary options only when they are approved and useful.

Favorites is a separate section inside You, not a home-screen block. It can list favorited videos and folders. History replaces a separate Recently Played destination.

---

## 7. Player implementation and engine selection

### 7.1 Playback-engine benchmark gate

The final engine is not yet selected. Media3 ExoPlayer 1.11.1 is the current candidate, not a final commitment. Compare:

- AndroidX Media3 ExoPlayer.
- LibVLC.
- mpv.

Use the same representative test corpus and device conditions. Record:
- File open success and startup time.
- Video/audio correctness.
- Seeking accuracy and stability.
- Embedded subtitle discovery/rendering.
- External subtitle loading and rendering.
- Multiple audio/subtitle track selection.
- Portrait/landscape and rotation behavior.
- Long-playback stability.
- Memory, battery, heat, and app size.
- Integration complexity, maintenance, licensing obligations, and compatibility with custom UI.

Test available representative samples:
- MP4 / H.264 / AAC.
- MKV / H.264 / AAC.
- MKV / HEVC with common audio variants.
- MKV with text subtitles (SRT/ASS/SSA) and bitmap subtitles (PGS/VobSub) where available.
- External SRT/VTT and other formats actually required.
- Multiple audio and subtitle tracks.
- Long videos, seeking, rotation, and interrupted playback.

Do not promise universal codec/subtitle support. Record actual outcomes by sample and engine. Select the engine based on measured compatibility and maintenance trade-offs, then document the decision.

### 7.2 Custom player experience

The selected engine supplies playback; Northstar owns the visible control design.

Required:
- Play/Pause, Previous, Next, Rewind, Forward.
- Responsive seek timeline.
- Current time and total duration.
- Tap the total-duration display to toggle to remaining time.
- Double-tap seeking.
- Press-and-hold for temporary 2× playback; restore the previous speed on release/cancel.
- Persistent speed menu with multiple options and custom speed.
- Swipe gestures for brightness and volume.
- Portrait, landscape, and automatic orientation choices.
- Embedded and external subtitle selection.
- Audio-track selection.
- Fullscreen with controls that hide automatically.
- Per-video resume offer.
- Previous/Next follows the active folder or selected library context.

Gesture zones and conflict rules (for example, swipe versus double-tap versus hold) must be designed and tested to avoid accidental actions. Proposed convention: left-side vertical swipe for brightness, right-side vertical swipe for volume, and left/right double-tap zones for seeking; confirm in player UX design.

### 7.3 Playback service and lifecycle

Use a MediaSessionService or equivalent service-backed playback architecture if validated for the selected engine. The service owns the active player and notification/media-session integration. Activity/screen recreation must not unexpectedly destroy active playback.

Background playback requirement is audio playback outside the app where appropriate. Whether the product also needs Picture-in-Picture is not currently approved and must not be silently added.

### 7.4 Previous/Next context

Build a stable playback queue from the current context:
- Current folder.
- Files Only result set.
- Search results, if the user starts playback from search.

Preserve the active sort/filter order. Define behavior when a queue item becomes unavailable.

---

## 8. Private Library and security design

### 8.1 Private storage boundary

- Separate private repository and database records from the public media library.
- Private media must not appear in public MediaStore browsing, public search, Favorites, or public Watch History.
- Store private files in app-controlled storage and encrypt file contents.
- Use Android Keystore-backed key protection and authenticated encryption (for example, AES-GCM) after a security review.
- Avoid plaintext temporary copies and clean up partial imports safely.
- Exclude private media and sensitive metadata from backups, logs, diagnostics, and crash reports unless the user explicitly exports something.

### 8.2 Organization and import

- Private Library behaves as its own file manager.
- Create folders and subfolders.
- Import individual files or an entire selected folder tree.
- Preserve the source folder structure during folder import.
- Show progress, cancellation, insufficient-space errors, and partial-import recovery.
- The only user-facing import action is **Move to Private Library**; do not offer Copy or a separate Delete action in this workflow.
- Implement a safe move across Android storage providers as a staged transfer: encrypt/write to private storage, verify integrity and readability, commit the destination, then request source removal using the appropriate MediaStore/SAF permission flow.
- Never remove the source before the private destination is verified. If source removal is denied or unsupported, report the operation as incomplete and explicitly state that the original remains; do not claim the move succeeded. Preserve the verified private destination safely and support a clear retry/recovery path.

**User decision resolved (23 November 2026):** Move only. The user explicitly rejected Copy and selected Move as the sole user-facing import action. The safe staged-transfer and source-removal behavior above is an implementation safeguard, not a separate Copy/Delete workflow.

### 8.3 PIN, biometrics, and recovery

- Require PIN and support biometric unlock.
- Rate-limit repeated PIN failures and avoid revealing whether a PIN guess is close.
- Use Keystore-protected keys; biometric enrollment/key invalidation must have a planned fallback.
- A forgotten PIN recovery process must not undermine encryption.

**User decision resolved — security-question experience (23 November 2026):**
- During setup, present a curated list of Northstar-provided security questions. The user selects any three and supplies answers; all three must be correct for forgotten-PIN recovery.
- Do not ask users to invent their own questions.
- Security questions are not cryptographic keys. Their answers may be guessable and must not be stored in plaintext. The implementation must use a reviewed cryptographic key-recovery design that protects against online guessing and offline attacks; do not weaken vault encryption merely to make recovery convenient.
- Recovery remains local-first, with no server-based recovery or automatic cloud upload.
- The exact key hierarchy, answer normalization, secure answer processing, rate-limiting, lockout policy, and recovery-key wrapping mechanism remain engineering design work and require security review before implementation.

### 8.4 Threat and privacy review

Before release, review:
- Rooted/compromised device limitations.
- App switcher/recents screenshot exposure.
- Clipboard and share-sheet exposure.
- Notifications and logs.
- Backup/restore behavior.
- Temporary files and failed imports.
- Authentication timeout and app backgrounding.
- Biometric lockout and PIN reset.
- Whether private media can be discovered by file managers or media scanners.

“Hidden from other apps where technically possible” must be described accurately; no app can guarantee secrecy on a fully compromised device.

---

## 9. Favorites, history, duplicates, and settings

- Favorites supports videos and folders, with a dedicated destination inside You.
- Watch History is chronological and does not create a second Recently Played section.
- Continue Watching/resume is based on per-video playback progress.
- Optional duplicate detection should begin with low-cost metadata candidates (size, duration, name/path) and offer an on-demand content-hash verification for likely matches. Do not hash the entire library on every launch or in the background without user awareness.
- Settings belongs under You. Organize settings into clear groups such as Playback, Library, Appearance, Storage & Permissions, Privacy & Security, and About/Diagnostics.
- Only include settings that correspond to implemented, tested behavior. Do not expose nonfunctional toggles.

---

## 10. Performance, reliability, and diagnostics

### Required engineering behavior

- Thousands of media entries do not freeze the UI.
- Scanning is cancellable and does not block playback, scrolling, or navigation.
- Room writes are batched and transactions are bounded.
- Thumbnails load asynchronously, are cached, and do not decode full video files on the main thread.
- Database queries are indexed for common sort/filter operations.
- Large folder lists and video lists are paged or otherwise bounded; avoid rebuilding the entire view tree on every keystroke.
- Removed files and disconnected drives become unavailable states, not crashes.
- Failed scans/imports leave a consistent prior library and recoverable work state.
- Errors are actionable and understandable.
- Diagnostics are generated locally and shared only when the user explicitly requests it. Exclude media contents, private filenames/paths, PINs, keys, and tokens.

---

## 11. Screen-by-screen acceptance criteria

### A. Main Library
- Opens into the folder-focused library.
- AMOLED black-and-white visual direction is applied consistently.
- Top navigation is present; no bottom navigation.
- Profile icon opens You.
- Player is not shown until a video is selected.
- Sort, View Mode, and Layout live together in one icon-and-text controls area.
- Default mode is Video Folders.
- Empty, loading, partial-access, denied-access, and error states are distinct and understandable.

### B. Video Folders
- Shows only folders that contain videos.
- Folder counts are correct for the selected source/access scope.
- Opening a folder shows its videos and nested folders according to the agreed navigation design.
- NEW indicators are consistent and persist across restarts until acknowledged.

### C. Files Only
- Shows video files without folder listings.
- List/Grid and all approved sorting options work.
- Search results identify the containing folder.
- Previous/Next queue follows the displayed/selected result context.

### D. Nested View
- Shows all folders, including empty folders and nested hierarchy.
- Parent/child relationships are accurate.
- Navigation does not flatten or misrepresent hierarchy.
- USB/SD trees remain distinguishable from internal storage.

### E. You
- Opens from the top profile icon.
- Contains Favorites, Watch History, Private Library, Settings, and approved secondary options.
- Does not turn the main Library into a dashboard.

### F. Player
- All approved controls, timeline toggle, gestures, temporary 2× hold, speed, orientation, tracks, subtitles, fullscreen, resume, and contextual Previous/Next work.
- Controls hide and reappear predictably.
- Playback survives screen changes and app backgrounding according to the approved background behavior.
- Unsupported file/track cases show clear errors or unavailable options rather than crashing.

### G. Private Library
- Requires authentication.
- Imports individual files and full folder trees.
- Creates folders/subfolders.
- Encrypts stored content.
- Never leaks private items into public library/search/history/favorites/diagnostics.
- Recovery behavior is tested and clearly explained.
- Interrupted imports can be safely resumed, retried, or cleaned up.

### H. USB/SD
- User can grant a folder/tree.
- Files and nested folders can be browsed and played where supported.
- Access persists when the provider supports persisted grants.
- Disconnect/reconnect and revoked-permission states are handled.
- Internal library remains functional when external storage is unavailable.

### I. Reliability
- Large library scan does not freeze the interface.
- Playback remains responsive during scanning.
- Partial permission/reselection updates the visible library correctly.
- App process death does not corrupt library state or private imports.
- Diagnostics contain no private content.

---

## 12. Test strategy and release gates

### 12.1 Automated tests

Add unit tests for:
- Folder-tree construction and all three view-mode filters.
- Sorting and search.
- NEW indicator state/acknowledgement.
- Playback completion/resume decisions.
- Previous/Next queue construction.
- Database migrations and repository reconciliation.
- Duplicate candidate detection.
- Private folder hierarchy and import state transitions.
- Permission-state mapping.

Add instrumentation/integration tests for:
- Room persistence across process recreation.
- MediaStore repository behavior with test data.
- SAF grant persistence and revoked grants.
- Private encryption/decryption and authentication gates.
- Playback service lifecycle and media-session behavior.
- Navigation and key screen states.

### 12.2 Android permission/storage matrix

Test at minimum:
- Android 8–12: legacy media permission behavior.
- Android 13: READ_MEDIA_VIDEO grant/denial.
- Android 14+: full versus selected/partial access, reselection, and changed selections.
- Internal storage.
- SD card (when hardware/device supports it).
- USB OTG drive (when hardware/device supports it).
- Provider revoked, device disconnected, file deleted, and insufficient storage cases.

### 12.3 Playback corpus

Run the same test corpus against each engine candidate. Keep a recorded result sheet with device/OS, file details, open/play/seek/track/subtitle outcomes, stability, heat, battery, and notes.

### 12.4 Performance tests

- Small library, several thousand items, and a large stress library.
- Cold start and warm start.
- Initial scan and incremental refresh.
- Search typing, sort changes, folder navigation, thumbnail scrolling.
- Playback while scanning.
- Long-duration playback and repeated seek/rotation.
- Memory pressure and process recreation.

### 12.5 User-device acceptance

The user tests the integrated release on the Xiaomi 14. Do not mark device acceptance complete until the user confirms the APK has been installed and the agreed test scenarios pass.

### 12.6 Single coordinated release gates

A release candidate is eligible only when:
1. All eight discussion areas are implemented or any exception is explicitly approved.
2. Automated tests pass.
3. Engine benchmark and media corpus results are recorded.
4. Security/privacy review passes.
5. Signed release APK builds and signature verification succeeds.
6. Upgrade installation is verified over the existing signed app without changing application ID or signing identity.
7. User completes Xiaomi 14 acceptance testing.
8. Known limitations are documented.
9. One coordinated release package and release notes are prepared.

Internal CI artifacts may be used for engineering verification, but do not deliver or publish a partial feature release. Increment versionCode for the eventual installable release; do not create a user-facing release just to show incremental progress.

---

## 13. Implementation sequence (internal milestones, one public release)

These are development milestones, not separate product releases.

### Milestone 0 — Design and decisions
- Approve this plan.
- Resolve only genuine product decisions listed in Section 14.
- Create screen layouts and interaction specifications.
- Finalize acceptance criteria.

### Milestone 1 — Architecture and data foundation
- Establish package boundaries and app state architecture.
- Add Room and migrations.
- Add coroutine-based repositories.
- Add permission-state model.
- Preserve package identity, signing, and upgrade path.

### Milestone 2 — Library and storage foundation
- Background/incremental MediaStore scanning.
- Full/partial/denied/reselection handling.
- SAF external-storage browsing and persisted grants.
- Folder hierarchy and three View Modes.
- Unified controls, search, sort, List/Grid.
- Thumbnails and NEW state.

### Milestone 3 — Player engine and service
- Run engine benchmark and select engine.
- Add playback service/session.
- Add queue/context model.
- Implement custom player controls, gestures, tracks, subtitles, resume, and history.

### Milestone 4 — You and personal library
- You page, Favorites, Watch History, Settings.
- Private Library encryption, authentication, recovery, folder organization, and import.
- Optional duplicate detection.

### Milestone 5 — Integration and hardening
- Integrate all feature areas.
- Run full automated test suite.
- Performance, lifecycle, privacy, security, storage-disconnect, and media-corpus testing.
- Resolve regressions without scope creep.

### Milestone 6 — One coordinated release
- Version and release notes.
- Signed GitHub Actions build and signature verification.
- Upgrade-safety test.
- User Xiaomi 14 acceptance.
- Deliver the single integrated release candidate after all gates pass.

---

## 14. Product decisions still requiring user confirmation

These are the only currently identified product choices that should be returned to the user. Do not reopen already locked decisions.

1. **Private import behavior — RESOLVED:** Move only. No Copy option and no separate Delete action. Use the safe staged-transfer procedure in Section 8.2; if Android prevents source removal, report the move as incomplete and preserve the original.
2. **Private recovery experience — RESOLVED:** curated Northstar-provided question bank; user selects any three and answers all three. No user-created questions. Cryptographic recovery implementation remains an engineering/security design task, not a further product choice at this stage.
3. **Nested View interaction — RESOLVED:** normal folder-opening navigation with a folder page, Back control, breadcrumb path, Create Folder icon, compact unified controls icon for Sort/View Mode/Layout, and pull-to-refresh. No expandable/inline tree.
4. **Sorting options — RESOLVED:** provide separate Date Added and Date Modified options (both newest first), alongside A–Z and Largest. Use source-provided dates where reliable; do not invent missing dates.
5. **Swipe gestures:** Recommended left-side vertical swipe = brightness; right-side vertical swipe = volume. Confirm or change.
6. **Top navigation:** The You/profile entry is locked. The remaining top-level navigation items and exact order should be settled in the screen mockup. Do not add bottom navigation.
7. **Background playback:** Audio playback outside the app is locked. Confirm whether it should continue automatically when the user leaves the app, or only after the user explicitly enables/starts background audio. Do not add Picture-in-Picture unless separately approved.

Technical engine choice, encryption implementation, schema details, and exact scan strategy are engineering decisions to be justified by tests; they do not need to be presented as product questionnaires unless they change the user experience or data-loss risk.

---

## 15. Approval and implementation status

- [x] Eight product discussions completed and recorded.
- [x] Master product specification saved in GitHub.
- [x] Current native source re-inspected.
- [x] Initial gap assessment recorded.
- [x] Integrated implementation plan drafted.
- [ ] User reviews and approves this implementation plan.
- [ ] Resolve the product decisions in Section 14.
- [ ] Prepare detailed screen-by-screen wireframes and data/permission flow diagrams.
- [ ] Begin implementation only after approval.
- [ ] Deliver one coordinated, tested product release.

**Current status: planning only. No application code was changed as part of this plan.**
