# Northstar Player — Master Product Specification
**Approved discussion record:** 23 November 2026  
**Status:** Eight product discussions completed and approved by the user  
**Repository:** https://github.com/afghanbark573-rgb/Northstar-Player  
**Native branch:** `phase-0/release-signing`

> This document is the durable record of the eight discussions completed on 23 November 2026. It is the latest product-direction authority for those topics. It supplements, and where explicitly stated supersedes, older project notes. It does not claim to reproduce the missing original eight-discussion transcripts verbatim.

## 1. Product vision — LOCKED

Northstar Player should be a polished, local-first Android media player that makes managing and watching personal media simple, private, reliable, and enjoyable.

Guiding principles:
- Local-first: personal media stays on the user's device; no automatic uploads or cloud-library dependency.
- Simple: useful capabilities without unnecessary complexity.
- Reliable: dependable playback and library management.
- Private: users remain in control of personal content.
- Distinctive: thoughtful, polished interface rather than a generic player.
- Quality over quantity.

## 2. Media library and folders — LOCKED

1. **Default view:** By Folder.
2. **Three View Modes** (all available from one unified controls location):
   - **Video Folders:** show only folders that contain videos.
   - **Files Only:** show video files without folder listings.
   - **Nested View:** show the complete folder hierarchy, including main folders, nested folders, and video folders. This is the all-folders hierarchy mode; it is not a fourth mode.
3. **Layout:** List and Grid layouts.
**Nested View navigation:** use normal folder-opening navigation, not an expandable/inline folder tree. Opening a folder displays its contents on a folder page. Show a breadcrumb path near the top so users can see their current location and navigate to ancestor folders. Keep the path usable on narrow screens (for example, horizontally scrollable or otherwise compact).

**Nested View toolbar:** provide a Back control, a Create Folder icon, and one compact controls icon that opens the unified Sort, View Mode, and Layout options. Do not scatter these options across separate toolbar buttons.

**Refresh:** support pull-to-refresh on the folder page to refresh its contents.

**Folder hierarchy:** preserve the actual parent-child structure and include empty folders in Nested View.

**Private Library quick access gesture:** On the main library and folder pages, a normal pull-down followed by release refreshes the current page. Pulling beyond the normal refresh threshold and holding briefly activates a distinct Private Library access gesture. This only opens the Private Library authentication screen; it never bypasses PIN or biometric authentication. The gesture must have clear visual feedback and be tuned to avoid accidental activation during ordinary refresh. It does not apply inside the video player.

4. **Search:** search videos by filename.
5. **Sorting:** Date Added, Date Modified, A–Z, and Largest. Date Added and Date Modified are separate sort options; both show newest first. Date Added uses the storage provider's added/indexed date where available. Date Modified uses the file's last-modified date where available. If a source does not provide a reliable date, handle that item consistently without inventing a timestamp.
6. **New content:** small NEW indicators on relevant folders.
7. **Recently Added:** no separate Recently Added section.
8. **Smart Collections:** excluded from scope.
9. **Scanning:** automatically discover accessible videos on the device.

### Unified library controls

Sort, View Mode, and Layout must be together in one single, easy-to-find controls area. Each uses a simple icon and text label. Do not scatter these controls across separate menus.

## 3. Video player — LOCKED

1. **Player design:** fully custom Northstar player interface, not the default Media3 control appearance.
2. **Playback controls:** Play/Pause, Previous, Next, Rewind, and Forward.
3. **Timeline and time display:** show current time and total duration. Tapping the total-duration display toggles it to remaining time.
4. **Gestures:** left-side vertical swipe controls brightness; right-side vertical swipe controls volume. Double-tap the left side to seek backward and the right side to seek forward.
5. **Temporary 2× playback:** press and hold the screen for temporary 2× playback; restore the previous speed when released or cancelled.
6. **Playback speed:** provide multiple speed options and custom speed selection.
7. **Subtitles:** support embedded and external subtitle files.
8. **Audio tracks:** allow selection among available audio tracks.
9. **Orientation:** portrait, landscape, and automatic rotation options.
10. **Background playback:** support audio playback outside the app where appropriate.
11. **Resume playback:** remember each video's position and offer to continue.
12. **Next/Previous context:** navigate through videos in the current folder or selected library context.
13. **Fullscreen:** clean viewing experience; controls hide automatically.

## 4. Main interface and user experience — LOCKED

1. **Visual theme:** AMOLED black and white — clean, minimal, modern, and high-contrast.
2. **Navigation:** important and frequently used navigation items belong at the top. Keep the bottom area empty; do not add bottom navigation.
3. **Home screen:** folder-focused library.
4. **Secondary options:** no scattered menus. A profile icon at the top opens the **You** page. Settings and all other secondary options belong there.
5. **Consistency:** consistent visual language across the library, folders, You page, and player.
6. **Feedback:** clear loading, empty, permission, and error states.
7. **Animations:** subtle and smooth, never distracting.
8. **Accessibility:** readable text and comfortable touch targets.

**Precedence:** This AMOLED black-and-white theme is the latest user-approved visual direction and supersedes the older Graphite Orange proposal in earlier project notes.

## 5. Smart features and personalization — LOCKED

1. **Favorites:** provide a separate Favorites section, accessible through the You page. Do not place Favorites as a section on the main home screen.
2. **Watch History:** provide playback history.
3. **Continue Watching:** offer to resume unfinished videos.
4. **Recently Played:** no separate Recently Played section; Watch History covers this need.
5. **Playback memory:** remember playback position separately for each video.
6. **AI recommendations:** none; do not add AI-based recommendations or unnecessary algorithms.
7. **Duplicate detection:** optional identification of duplicate videos.

Keep the home screen clean and folder-focused.

## 6. Private Library and security — LOCKED

1. **Private Library:** a separate protected area for private videos.
2. **Import:** import individual videos or complete folders.
3. **Organization:** allow users to create folders and subfolders inside the Private Library.
4. **Authentication:** PIN and biometric authentication.
5. **Isolation:** private videos must not appear in the public library, public search, or public watch history.
6. **Encryption:** encrypt private video storage.
7. **Recovery:** provide a safe recovery process if authentication details are forgotten.
8. **Visibility:** keep private content hidden from other apps where technically possible.

The Private Library should behave like its own small file manager, with folder creation, navigation, and organization.

**Approved import behavior — MOVE ONLY (23 November 2026):**
- The only user-facing import action is **Move to Private Library**. There is no Copy option and no separate Delete action as part of this workflow.
- The intended result is that the private video is stored in the protected Private Library and no longer appears at its former public location.
- Because Android may not permit a direct atomic move across storage providers, implementation must use a safe internal transfer: stage/encrypt the private file, verify its integrity and readability, commit it, then request removal of the original through the applicable Android/SAF permission flow.
- If the original cannot be removed, do not report a successful move. Clearly report that the move is incomplete and that the original remains; preserve the verified private data safely and let the user retry or resolve the permission issue.
- Never delete the source before the private destination is fully verified. Do not expose this safety mechanism as separate Copy and Delete product actions.

**Approved recovery experience — RECOMMENDED SECURITY QUESTIONS (23 November 2026):**
- During Private Library setup, Northstar presents a curated list of recommended security questions. The user chooses any three questions from that list and supplies an answer to each.
- All three answers must be verified for forgotten-PIN recovery.
- Users choose from Northstar's supplied questions; they do not write their own questions.
- The questions and answers are a user-friendly recovery experience, not a substitute for cryptographic key protection. Engineering must design and security-review a secure key-recovery mechanism; answers must not be stored in plaintext, and the design must account for guessable answers, offline attacks, and repeated attempts.
- No server-based recovery or automatic cloud upload.

**Still to specify before implementation:** the precise cryptographic recovery design and Android storage/visibility guarantees. Do not silently choose a design that weakens vault encryption.

## 7. Performance, reliability, and permissions — LOCKED

1. Handle libraries containing thousands of videos without freezing.
2. Scan media in the background without blocking the interface.
3. Use incremental scanning to detect new, deleted, or changed videos without unnecessarily rescanning everything.
4. Correctly handle full, partial/selected, and denied media permissions.
5. Keep playback independent from scanning; scanning and browsing must not interrupt playback.
6. Recover gracefully from crashes and interruptions.
7. Use battery and memory efficiently; avoid unnecessary background work.
8. Handle unavailable files, removed SD cards, and changed storage locations.
9. Provide useful diagnostics without collecting private media data.
10. **USB and SD card support:** read and browse video files on connected USB drives and SD cards where Android and the device support access. Use appropriate Android access mechanisms, including SAF where required.

## 8. Technical architecture and development strategy — LOCKED

1. **Platform:** native Android using Kotlin.
2. **Playback engine:** benchmark Media3, LibVLC, and mpv before making the final engine choice. Media3 ExoPlayer 1.11.1 is an existing candidate, not a final decision.
3. **Media access:** MediaStore for normal internal/shared media discovery; Storage Access Framework (SAF) for user-selected folders and external storage such as USB/SD where required.
4. **Persistence:** Room/SQLite for persistent library information, history, favorites, and playback positions.
5. **Private storage:** encrypted application-managed storage for the Private Library.
6. **Background playback:** MediaSessionService where appropriate.
7. **Architecture:** separate and maintainable components for library, player, private storage, permissions, persistence, and UI.
8. **Testing:** automated tests plus real-device testing on the user's Xiaomi 14. Never claim device testing until the user has actually tested the build.
9. **Build and release:** GitHub Actions, permanent signing identity, and verified signed APKs. Never expose signing secrets.
10. **One coordinated product release:** all eight discussion areas are intended to be implemented and integrated into one coherent release. Internal development, integration, and testing milestones are allowed, but do not ship one discussion/feature area as a separate public release. Do not generate interim APKs merely to repackage existing functionality.

## 9. Release philosophy and approval gates

- First consolidate the approved discussions into the complete master specification and review it for omissions, contradictions, technical feasibility, and acceptance criteria.
- Then audit the existing source and compare it with the specification. Do not assume an approved feature is already implemented.
- Produce one integrated implementation plan covering all eight areas.
- Develop in controlled internal milestones, with regression testing at each milestone.
- Do not publish or deliver a partial feature release. The user expects one coordinated product release after the agreed scope is implemented and validated.
- Before the final release: run automated tests, build/signature verification, upgrade-safety checks, and real-device acceptance testing.
- Report incomplete or technically unsupported requirements honestly; do not silently drop them.

## 10. Repository and project continuity

- Product: Northstar Player, native Android app.
- Repository: https://github.com/afghanbark573-rgb/Northstar-Player
- Native integration/release branch: `phase-0/release-signing`.
- `main` remains the web prototype and earlier native scaffold. Do not merge branches casually.
- Application ID / namespace: `com.northstar.player`; preserve it for upgrade compatibility.
- Existing native build configuration recorded in the project status: minSdk 26, compileSdk 36, targetSdk 35, Java/Kotlin 17.
- User's primary test device: Xiaomi 14, Android.
- User works from Android phone and GitHub mobile, without a desktop or terminal. Keep instructions mobile-friendly and use cloud builds where possible.
- Web prototype 0.8 is a frozen UX validation baseline; do not resume web releases unless a material blocker to native development appears.
- Permanent release signing is configured on the native branch. Preserve the signing identity; never commit keystore bytes/passwords or expose GitHub secret values. Keep a secure offline keystore backup.

## 11. Latest source audit snapshot (23 November 2026)

A read-only static audit was reported against `phase-0/release-signing`. No source files were changed and no build was triggered by that audit.

Recorded branch HEAD at audit time: `3cce871bcdc825184698bf16bf932655d6c26d98` (documentation-only status reconciliation commit).

Recorded native build configuration:
- Application ID / namespace `com.northstar.player`.
- minSdk 26; compileSdk 36; targetSdk 35.
- versionCode 3; versionName `0.1.2-native`.
- Media3 ExoPlayer/UI 1.11.1 candidate.
- Kotlin Android plugin 2.2.20; AGP 8.13.2; Java/Kotlin target 17.
- Release signing configured through GitHub Actions secrets (secret values must never be disclosed).

Audit findings at that point:
- Most UI, scanning, sorting, folder grouping, and player logic resides in one `MainActivity.kt`.
- No Room/SQLite dependency or implementation was found in the inspected app Gradle dependencies.
- MediaStore scanning was synchronous on the main thread.
- Permission handling did not fully model Android 14 partial/selected access and reselection.
- Folder display was basic grouping, not complete nested folder navigation.
- Player used standard Media3 PlayerView controls; custom player UI, resume state, subtitle picker, and track-selection UI were not found.
- No automated `test` or `androidTest` source sets were found in the inspected tree.
- Branch protection was disabled at audit time.
- The audit was static only: no runtime execution, profiler session, large-library test, media corpus test, or Xiaomi 14 test was performed.

Recorded successful signed CI run: https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45092225859 (candidate commit `8a2c04e1acecac089146dce5b323c2fea6ca1b35`; branch HEAD later advanced with a documentation-only commit). CI success is not proof of real-device correctness.

### Critical audit conclusion

The existing native app is a starting foundation, not the completed product. The new discussions describe the target product and do not mean these features already exist. Before implementation, re-check the live branch and source because repository state may have changed since this snapshot.

## 12. Important distinctions and unresolved implementation details

These details require technical design or explicit user approval before implementation:
- Exact interaction and placement of the unified Sort / View Mode / Layout control.
- Exact top navigation items and their ordering; bottom navigation remains excluded.
- Whether Private Library import copies, moves, or lets the user choose.
- Exact forgotten-PIN recovery method and consequences for encrypted data.
- Exact behavior when a USB/SD device disconnects during browsing or playback.
- Exact rules for what counts as a newly discovered folder/video and when NEW indicators clear.
- Exact fallback behavior when a storage provider does not expose a reliable Date Added or Date Modified value.
- Duplicate detection strategy and whether it is metadata-only or content-hash based.
- Playback engine benchmark results and format/subtitle compatibility.
- Whether all external subtitle formats and embedded tracks can be supported by the selected engine.
- Exact navigation/context behavior for Previous/Next when playback starts from Files Only or search results.
- Acceptance criteria and UI layouts for each requirement.

Do not treat these open implementation details as reasons to reopen already locked product decisions. Resolve them in the specification/technical design phase, asking the user only where a genuine product choice is needed.

## 13. Continuity rules for every future Northstar session

1. Read this master specification and `PROJECT_STATUS.md` before planning or coding.
2. Preserve all eight approved discussions and the one-coordinated-release requirement.
3. Treat this document's AMOLED black-and-white theme as newer than older Graphite Orange notes.
4. Do not restart the project or discard prior work.
5. Inspect the live branch, commits, source, Gradle files, workflows, and last user-verified baseline before changes.
6. Clearly separate approved product requirements, technical candidates, implemented code, CI evidence, and user-verified device behavior.
7. Never claim an unimplemented feature is complete.
8. Never claim CI success proves device correctness.
9. Never change application ID or permanent signing identity.
10. Never expose signing secrets.
11. Do not add Recently Added, Smart Collections, AI recommendations, or bottom navigation unless the user explicitly changes scope.
12. Do not ship partial discussion-by-discussion releases or cosmetic/duplicate micro-releases.
13. Preserve local-first privacy and do not request broad all-files access for ordinary media discovery.
14. Keep project status, decisions, test evidence, and release history updated after meaningful work.

## 14. Next action

Create a detailed implementation specification with screen-by-screen UX, data model, permissions/storage flows, playback-engine benchmark criteria, security/recovery design, acceptance criteria, test plan, and a single integrated release plan. Compare it with the live source tree and identify which requirements already exist, partially exist, or are missing. Do not start implementation until the user reviews and approves that complete plan.


## 15. Durable project record and backup

This file is the canonical, durable record of the eight approved product discussions. It is stored in the GitHub repository itself and is independent of any ChatGPT conversation. Deleting or losing a chat does not delete this repository file.

- Canonical path: `NORTHSTAR_PRODUCT_SPEC.md` at the repository root.
- Canonical branch: `phase-0/release-signing`.
- Repository: https://github.com/afghanbark573-rgb/Northstar-Player
- The current approved requirements, decisions, exclusions, release philosophy, audit snapshot, unresolved implementation details, and continuity rules must be preserved when this document is revised.
- Future project work must read this specification and `PROJECT_STATUS.md` before making decisions or code changes.
- Update this file in GitHub whenever the user approves a material change. Do not rely on chat history as the only copy.
- For additional protection against account or repository access loss, periodically download a local/offline copy of this file (and ideally the repository) to a device or storage location controlled by the user.
