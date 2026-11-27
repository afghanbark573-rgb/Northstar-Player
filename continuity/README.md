# Northstar Player — Continuity Archive

This branch is a documentation-only continuity checkpoint. Latest checkpoint update: 24 November 2026. It is based on `phase-0/release-signing` and does not change the native product branch or release workflow.

## Canonical records
- [Master continuation pack](MASTER_CONTINUATION_PACK.md) — current project handover and engineering state.
- [Change log](CHANGELOG.md) — checkpoint update history.
- [Next-chat instructions](NEXT_CHAT_INSTRUCTIONS.md) — exact restart procedure.

The GitHub master Markdown is the searchable, version-controlled canonical text copy. The 23 November 2026 Word export is historical and is not the latest checkpoint.

## Update policy
At each major milestone and before moving to a new chat:
1. Inspect the live native branch and current repository state.
2. Update `PROJECT_STATUS.md` on the native branch when project status changes.
3. Update this branch's master pack, changelog and next-chat instructions.
4. Record exact source commit, CI run and artifacts, and distinguish CI evidence from device verification.
5. Preserve prior decisions and mark superseded material; do not silently erase history.

Never store signing secrets, keystore bytes, private media, or recovery answers in this branch.


## 24 November 2026 checkpoint policy update
The latest checkpoint diagnoses run #56's instrumentation crash and records the debug-only provider manifest correction in native commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`. The next chat must poll the CI run triggered by that exact source correction before proceeding. The master pack contains the precise failure and verification requirements.

Engineering cadence: after each successful milestone, update native `PROJECT_STATUS.md` and the continuity master pack, changelog and next-chat instructions before moving to the next milestone. Record failures with evidence rather than silently overwriting history.

Current CI gate: run [#57 / 45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513) validates the debug SAF provider's required MANAGE_DOCUMENTS manifest protection. Its result was pending at the latest checkpoint.


## 24 November 2026 checkpoint — run #57 diagnosis
Run [#57 / 45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513) ran both instrumentation tests: Room migration passed; SAF integration failed because the debug in-memory DocumentsProvider did not implement `isChildDocument` for nested tree descendants. The debug-only provider was corrected in native commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`. The master pack and next-chat instructions contain the evidence and next validation gate. Poll the new CI run before proceeding; do not distribute intermediate APKs.


Current validation run: [#58 / 45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837), testing the `isChildDocument` descendant-chain fix from native commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`. At latest checkpoint, instrumentation had not started; see the master pack for details.


## Chat-independent source of truth — 24 November 2026
GitHub is the durable project memory; previous chat history is not required. Native code/spec/status live on `phase-0/release-signing`; the documentation-only archive lives on `continuity/master-pack`. Start with this README, then the final dated checkpoint in [MASTER_CONTINUATION_PACK.md](MASTER_CONTINUATION_PACK.md) and [NEXT_CHAT_INSTRUCTIONS.md](NEXT_CHAT_INSTRUCTIONS.md). Always inspect live branch heads and native status before coding. If records conflict, current source/product spec takes precedence, followed by current implementation plan/status, latest dated checkpoint, then historical changelog. Never commit credentials or keystore bytes.

Current gate: [CI run #58](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837), testing `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`. JVM unit tests/debug APK build and signed release/signature verification have passed; Room migration and SAF integration instrumentation is still running at the last poll. Do not begin Library Browser work until the SAF test passes.

 
## 25 November 2026 — run #58 passed
Run [#58 / 45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) passed on source SHA `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`. Instrumentation diagnostics confirm 2 tests passed, 0 failures/errors/skips (Room migration and nested SAF DocumentsProvider integration). Signed release build and `apksigner` verification passed. The CI SAF/Room gate is closed for this commit; physical USB/SD, actual picker/persisted grants, Xiaomi 14 acceptance and signed upgrade testing remain open. Next internal milestone: inspect and implement Library Browser folder metadata aggregation/sorting. No intermediate APK distribution.


## 25 November 2026 — Library Browser hardening passed
Native commit `9b84ea24a1f11671aca54c10a339dc161307473d` passed CI run [#60 / 45479496288](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45479496288). JVM unit tests, debug build, Room migration and nested SAF instrumentation (2/2, no failures), signed release build and signature verification passed. Diagnostics artifact ID `14962565694`; release artifact ID `14960903935`. The Library Browser metadata aggregation and deterministic sorting change is verified by CI. Artifacts were not distributed. Physical USB/SD, picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed upgrade remain device gates. See the latest checkpoint in the master pack for full digests and exact tests.


## 25 November 2026 — Folder NEW-state lifecycle passed
Native commit `d00f920399d699981532985c5e07a9c4b23035fe` passed CI run [#68 / 45483698896](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45483698896). Unit tests/debug build, emulator instrumentation (2/2, no failures), signed release build and APK signature verification passed. Diagnostics artifact ID `14964397667`; release artifact ID `14963031377`. Folder NEW markers now persist until acknowledgement and update for new descendant content using existing Room fields; no schema migration. Artifacts were not distributed. Physical-device gates remain open. See the latest master-pack checkpoint for exact evidence.


## 26 November 2026 — Pull-to-refresh milestone passed CI

Native source commit `7925d39b466897c23b74d76c9a8f08112185ec68` passed [workflow run #74](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45494565813) on retry attempt 2. Unit tests/debug build, emulator instrumentation (2/2), signed release build and signature verification passed. Four refresh-target unit tests passed; instrumentation diagnostics artifact ID `15089184886`. Pull-to-refresh is CI-verified, not yet physically accepted on Xiaomi 14 or real USB/SD providers. The Private Library long-pull shortcut remains a separate authenticated behavior. See the latest dated checkpoint in the master pack.



## 26 November 2026 — Permission-aware MediaStore reconciliation passed CI
Native commit `87183de1d23ebede848d6c59eae9106358e9093e` passed [workflow #45764030633](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45764030633). Unit tests/debug build, emulator instrumentation (4/4, 0 failures/errors/skips), signed release build and `apksigner` verification passed. Diagnostics artifact ID `15092115234`; release artifact ID `15091590357`. The implementation validates atomic MediaStore snapshot publication, abandoned-stage discard, metadata preservation and permission-aware reconciliation. No APK was distributed. Physical-device gates remain open. See the latest checkpoint in the master pack and native `PROJECT_STATUS.md` for exact evidence and next steps.




## Latest checkpoint — 26 November 2026
The automatic SAF grant reconciliation milestone is CI-verified on exact native source commit `e37544cd19294774846e19dbd72657cb4307d7d4`. See the final dated checkpoint in [MASTER_CONTINUATION_PACK.md](MASTER_CONTINUATION_PACK.md), the corresponding [CHANGELOG.md](CHANGELOG.md) entry, and [NEXT_CHAT_INSTRUCTIONS.md](NEXT_CHAT_INSTRUCTIONS.md). CI run [#97 / 45775813217](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45775813217) passed, including four emulator instrumentation tests and signed-release signature verification. Physical USB/SD, actual picker/grant lifecycle, Xiaomi 14, upgrade and manual gesture checks remain outstanding.


## Latest checkpoint — 26 November 2026
The current native milestone is generation-aware incremental MediaStore scanning, implemented at source commit `49a73222f29b28fba8924210aef5a8b10fe17fdf`. The earlier pending-CI note is superseded: run [#45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016) completed successfully, including 5/5 emulator instrumentation tests and signed-release verification. The remaining task is to check in the exact generated Room schema v2 and v3 snapshots; see the latest checkpoint in [MASTER_CONTINUATION_PACK.md](MASTER_CONTINUATION_PACK.md).
## Latest checkpoint — 26 November 2026

Generation-aware incremental MediaStore scanning is CI-verified on exact source SHA `49a73222f29b28fba8924210aef5a8b10fe17fdf`; Actions run [45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016) passed both debug and signed-release jobs. Emulator instrumentation: 5/5 passed, 0 failures/errors/skips. The exact next task is to check in generated Room schema v2 and v3 snapshots; their source artifacts are IDs `15097206733` (v2) and `15099991005` (v3). Read the latest dated section in `MASTER_CONTINUATION_PACK.md` before continuing. Incremental deltas do not generically detect deletions; full manual and 12-hour scans reconcile deletions. Device gates remain open; no APK was distributed.


## VERIFIED CHECKPOINT — 27 NOVEMBER 2026 — ROOM SCHEMA V2/V3 CHECK-IN COMPLETE

- Native branch: `phase-0/release-signing`.
- Generated Room schema snapshots are now committed under `android/app/schemas/com.northstar.player.core.database.NorthstarDatabase/`:
  - `2.json` — database version 2, identity hash `48608d98b3b10817ec5775638633296d`; source artifact ID `15097206733`, ZIP SHA-256 `890eaf76897b3ea5e926b684bfe5999db2ceca6c15165d2aa8f9d279873e5f97`; schema commit `4b61bf709e318eb45c30403aa2704c2b46fb1f6e`.
  - `3.json` — database version 3, identity hash `9a446e872052b86ac50b30160a496191`; source artifact ID `15099991005`, ZIP SHA-256 `f0e05b986fde24e0efab95f0cafe1518e4edd4cdb1bf639b6c6e368176bd03fb`; schema commit `19a4287a831599471be33b6e1b4f2573e76621c6`.
- Both committed JSON files were fetched back from GitHub and parsed. Each reports the expected database version, identity hash and nine entities. Version 2 `scan_states` has the original five fields; version 3 adds nullable `mediaStoreVersion` (TEXT) and `mediaStoreGeneration` (INTEGER). No historical v2 schema was regenerated from v3.
- The schema files were copied from their exact CI artifact JSON payloads; JSON formatting is compacted, but schema content/identity is retained. Artifact ZIP digests above identify the original CI evidence.
- CI remains the prior exact-source verification: Actions run [45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016), source SHA `49a73222f29b28fba8924210aef5a8b10fe17fdf`; debug and signed-release jobs passed, with 5 emulator instrumentation tests passing (0 failures/errors/skips). The schema-only commits do not change application code and have not yet been independently rebuilt.
- Native branch head after schema check-in: `19a4287a831599471be33b6e1b4f2573e76621c6` before the status-document commit(s). Verify the live head again after documentation updates.
- Incremental MediaStore deltas upsert new/changed rows; generic deletions are reconciled by complete manual refresh and the 12-hour forced full scan. Do not imply immediate deletion detection.
- No APK was distributed. Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing, and manual pull/Private Library long-pull behavior remain unverified device gates.
- No new engineering milestone has been started. Next: inspect the current product spec, integrated implementation plan and native source/branch heads, then declare the next Main Quest, bundled Side Quests, files, risks, acceptance criteria and regression tests before coding.

### Preserved product and release guardrails
Package/application ID `com.northstar.player`; preserve permanent signing identity; local-first with no automatic cloud upload; AMOLED black-and-white design; exactly three Library modes; all eight approved product areas ship together in one coordinated public release; no intermediate APK distribution; never commit signing secrets or keystore bytes.


## Latest checkpoint — 27 November 2026
Room schema v2 and v3 snapshots are now checked into the native branch at `android/app/schemas/com.northstar.player.core.database.NorthstarDatabase/2.json` and `3.json`. See the latest dated checkpoint in [MASTER_CONTINUATION_PACK.md](MASTER_CONTINUATION_PACK.md), [PROJECT_STATUS.md](PROJECT_STATUS.md), [CHANGELOG.md](CHANGELOG.md), and [NEXT_CHAT_INSTRUCTIONS.md](NEXT_CHAT_INSTRUCTIONS.md). Prior incremental MediaStore CI run 45782253016 passed on source SHA 49a73222f29b28fba8924210aef5a8b10fe17fdf. Schema-only commits have not yet had a separate CI run. Device acceptance gates remain open; no APK was distributed.


## IN-PROGRESS MILESTONE — 27 NOVEMBER 2026 — ASYNCHRONOUS LIBRARY THUMBNAILS

### Scope declared before implementation
- **Main Quest:** replace text-only video placeholders with real, asynchronously loaded video thumbnails in both List and Grid layouts.
- **Bundled Side Quests:** use the same loader for MediaStore and SAF content URIs; bound decode concurrency and memory use; cancel obsolete view-load requests during re-render and Activity destruction; keep thumbnail failure non-fatal with a neutral placeholder.
- **Files:** `android/app/src/main/java/com/northstar/player/data/library/VideoThumbnailLoader.kt`; `android/app/src/main/java/com/northstar/player/MainActivity.kt`.
- **Risks:** provider thumbnail support varies; SAF URIs can become unavailable; decoding must never run on the main thread; fast search/render changes must not leave stale images attached to the wrong row.
- **Acceptance criteria:** Android 10+ uses `ContentResolver.loadThumbnail`; Android 8/9 uses a MediaMetadataRetriever fallback; decode work is off-main-thread and limited to two concurrent operations; an 8 MB LRU cache bounds retained previews; both layouts display previews when providers can supply them; missing/unavailable thumbnails leave a safe placeholder; old requests are cancelled and cannot update a stale view.
- **Regression gates:** existing unit tests, debug build, emulator Room/SAF instrumentation, signed release build and `apksigner` verification. No Room schema change.

### Source and CI state
- Helper implementation commit: `0e25e7d1a24a439b9fe1423e5078fa9fd3d17837`.
- UI integration commit: `3cc1eee0290b6f7e486badefbfa5cbadb9c6ff43`.
- GitHub Actions run [45952500122](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45952500122) was triggered for exact source HEAD `3cc1eee0290b6f7e486badefbfa5cbadb9c6ff43`; at this checkpoint both debug and release jobs are still in progress. Do not claim build/test/signature success until the run completes and reports are inspected.
- No APK has been distributed. Physical-device acceptance remains outstanding.


## Thumbnail loader hardening — 27 November 2026

- Corrective source commit: `e7fa4077d9213b6e6fc8288a1529030cb8c7c251`. Android 10+ provider thumbnail calls now propagate coroutine cancellation to Android `CancellationSignal`; Android 8.1+ legacy extraction requests aspect-ratio-preserving scaled frames within 480×270, and Android 8.0 scales the extracted frame before caching.
- Existing limits remain: two concurrent decode operations and an 8 MB LRU cache; work stays off the main thread; both MediaStore and SAF URIs are supported; failed thumbnails remain non-fatal.
- Authoritative corrected-source CI: [run #116 / 45953446757](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45953446757), exact source SHA `e7fa4077d9213b6e6fc8288a1529030cb8c7c251`. At this update, debug unit-test/build and signed release build steps are still running. Await overall completion and inspect instrumentation diagnostics before marking CI verified.
- Earlier run #115 / 45952500122 is for pre-hardening source `3cc1eee0290b6f7e486badefbfa5cbadb9c6ff43`; release job succeeded and debug unit/build step passed, but migration instrumentation remains in progress. It does not certify the corrected source.
- No APK distribution. Device acceptance, including Xiaomi 14, actual MediaStore/SAF provider thumbnails, scrolling/memory performance, remains outstanding. Preserve one coordinated public release and all existing package, signing, privacy, three-mode Library and design guardrails.


### CI update — corrected thumbnail source, 27 November 2026

- Run #116 / `45953446757`, exact source `e7fa4077d9213b6e6fc8288a1529030cb8c7c251`: debug unit tests and debug APK compilation passed; emulator migration instrumentation remains in progress.
- Release keystore restoration passed, but `:app:assembleRelease` stopped at Maven dependency resolution after Maven Central returned HTTP 429 for `kotlinx-coroutines-android:1.10.2`. Signature verification and artifact upload were skipped. This is a transient external repository rate limit, not evidence of a source compile failure.
- GitHub would not rerun the failed release job while this workflow run was still active. Retry it after the run completes; do not claim CI green until the retry and instrumentation both pass.
