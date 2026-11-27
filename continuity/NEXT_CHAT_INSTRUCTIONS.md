# Next-chat instructions — Northstar Player

1. Read the latest **CURRENT CHECKPOINT UPDATE — 24 NOVEMBER 2026** section at the end of [MASTER_CONTINUATION_PACK.md](MASTER_CONTINUATION_PACK.md), then read the historical decisions above it.
2. Inspect the live native branch `phase-0/release-signing`, current HEAD, recent commits, `PROJECT_STATUS.md`, source tree and workflow. The pack is a handover, not proof that GitHub has not changed.
3. First poll CI run [#56 / 45411300973](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45411300973) for exact commit `07b96f31a4cbab9cfcad1e0ccfa4f81232142960`. At the last checkpoint it was still running Android instrumentation. Do not claim the SAF integration test passed until the final debug job and both instrumentation tests are confirmed.
4. If run #56 fails, inspect its diagnostics artifact, test report, XML/textproto and logcat before making another code change. Identify the exact cause; do not speculate or change production SAF code without evidence.
5. If run #56 passes, update native `PROJECT_STATUS.md` and this continuity archive with exact results. Then resume Library Browser folder metadata aggregation/sorting and regression tests: source separation, nested descendants, empty folders, unknown dates, equal tie-breaks, root mode, path normalization and video sorting.
6. Keep physical USB/SD provider behavior, Android picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade test as separate outstanding gates until actually tested.
7. Preserve all eight approved product areas, AMOLED black-and-white design, exactly three library modes, package/signing identity, local-first privacy and one coordinated public release. Never distribute intermediate APKs.
8. Do not mix the `main` web prototype branch with native development. Before coding, state Main Quest, bundled Side Quests, affected files, risks, acceptance criteria and regression tests.
9. At each major milestone and before a new chat, update native `PROJECT_STATUS.md` plus this branch's master pack and changelog. Keep verified CI evidence separate from device acceptance.


## Latest restart instruction — 24 November 2026, after run #56 diagnosis
1. Inspect the live native branch `phase-0/release-signing` and confirm the latest HEAD. Source correction: `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`.
2. Poll the CI run triggered by that commit. Verify the tested SHA, job conclusions, and that both Room migration and SAF DocumentsProvider instrumentation tests actually ran and passed.
3. Run #56 (`45411300973`) is conclusively failed: the app process crashed before tests started because the debug test DocumentsProvider lacked `android:permission="android.permission.MANAGE_DOCUMENTS"`. This was fixed in the source commit above. Do not misstate run #56 as a production SAF failure.
4. If the new run fails, inspect diagnostics and exact exception before changing code. If it passes, update native status and continuity files with evidence, then resume Library Browser folder metadata aggregation/sorting and regression tests.
5. Do not begin Library Browser work before the SAF instrumentation gate is resolved. Keep physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade testing as separate outstanding gates.
6. Preserve all eight approved product areas as one coordinated public release; no intermediate APK distribution. Preserve package/signing identity, AMOLED black-and-white direction, three library modes and local-first privacy.
7. After each successful engineering milestone, update native `PROJECT_STATUS.md` plus this continuity master pack, changelog and restart instructions before starting the next milestone. Record failed attempts with exact evidence too.


### Current in-progress CI — run #57
- Poll [run #57 / 45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513), which tests exact source commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`.
- Last known state: debug and release jobs in progress; instrumentation pending. Do not infer a pass from the manifest change or from a successful release build alone.


## Latest restart instruction — 24 November 2026, after run #57 diagnosis
1. Inspect live native branch `phase-0/release-signing` and confirm current HEAD. The latest source correction is `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
2. Poll the CI run triggered by that commit. Confirm exact tested SHA, all job conclusions, and both instrumentation test counts/results.
3. Run #57 (`45441499513`) failed after both tests executed: Room migration passed 1/1; SAF provider integration failed 1/1 with `SecurityException: Document nested-parent is not a descendant of test-root`. The in-memory debug test provider lacked `isChildDocument`; the fix implements ancestor-chain checks. Do not mislabel this as a production SAF defect.
4. Unit tests/debug build and signed release build/signature verification passed in run #57. Still do not distribute intermediate APKs.
5. If the new CI run fails, inspect report/XML/textproto/logcat and exact exception before changing source. If both instrumentation tests pass, update native status and continuity docs, then resume Library Browser metadata aggregation/sorting and its regression suite.
6. Do not begin Library Browser work before SAF instrumentation passes. Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade remain separate unverified gates.
7. Preserve all eight approved product areas as one coordinated public release; preserve package/signing identity, AMOLED black-and-white direction, exactly three library modes and local-first privacy.
8. After each successful engineering milestone, update native `PROJECT_STATUS.md` plus continuity master pack, changelog and restart instructions before starting the next milestone. Record failures with exact evidence.


## Current CI — run #58
- Poll [45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837), exact source commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
- At last check, unit/debug build and signed release build were still running; instrumentation had not started. Verify the SAF integration test and Room migration test results before proceeding.
- Run #57 diagnosis and source correction are in the latest checkpoint section above.


## Current restart checkpoint — 24 November 2026 (chat-independent)
1. Treat GitHub as the durable project memory; old chats are not required. Read [README](README.md), then the final dated checkpoint in [MASTER_CONTINUATION_PACK.md](MASTER_CONTINUATION_PACK.md) and this file.
2. Inspect live native branch `phase-0/release-signing`, continuity branch `continuity/master-pack`, current HEADs, product spec, implementation plan and `PROJECT_STATUS.md`. Do not assume the archive proves current GitHub state.
3. Poll [CI run #58 / 45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837), exact source SHA `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`. Unit tests/debug APK build passed; signed release build and signature verification passed; Room migration and SAF DocumentsProvider instrumentation was still running at last poll. Verify exact final job conclusions and test counts.
4. If run #58 fails, inspect report/XML/textproto/logcat diagnostics before source edits. If it passes, update native status and all continuity files with exact evidence, then begin Library Browser folder metadata aggregation/sorting and its defined regression suite.
5. Do not begin Library Browser milestone before SAF instrumentation passes. Physical USB/SD behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade remain separate device gates.
6. Preserve eight approved product areas as one coordinated release; AMOLED black-and-white direction; exactly three library modes; local-first privacy; package/signing identity; no intermediate APK distribution.
7. Before coding, state Main Quest, bundled Side Quests, affected files, risks, acceptance criteria and regression tests. After each successful milestone update native `PROJECT_STATUS.md` and continuity master pack/changelog/README/next-chat instructions. Record failures with evidence.
8. Never store secrets or keystore bytes in tracked files. Never claim device testing that the user has not performed.

Standard restart phrase: **“Let's continue the Northstar Player project from the last checkpoint.”**

 
## Latest restart instruction — 25 November 2026, after CI run #58
1. Read the latest checkpoint at the end of `MASTER_CONTINUATION_PACK.md`; historical run #58 pending statements above are superseded.
2. Inspect live native and continuity branch heads before coding. Native status was updated after the verified CI result.
3. Run #58 / [45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) tested exact source `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4` and completed success. Instrumentation diagnostics show 2/2 passed, 0 failures/errors/skips: Room migration and nested SAF DocumentsProvider integration. Signed release build and signature verification passed.
4. The emulator SAF/Room CI gate is closed. Do not confuse this with physical USB/SD, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance or signed install-over-existing device verification; all remain outstanding.
5. Next planned internal milestone: Library Browser folder metadata aggregation/sorting. Re-inspect current code and plan first; define Main Quest, bundled Side Quests, files, risks, acceptance criteria and tests before edits. Preserve three modes, hierarchy/empty folders, source separation, unknown-date behavior and deterministic tie-breaks.
6. No intermediate APK distribution. Eight product areas remain one coordinated public release. Preserve package ID/signing identity and local-first privacy.
7. After every milestone update native `PROJECT_STATUS.md` and all four continuity records before proceeding.


## Latest restart instruction — 25 November 2026, after Library Browser CI run #60
1. Confirm native HEAD is `9b84ea24a1f11671aca54c10a339dc161307473d` or inspect whatever newer commit is now current.
2. CI [run #60 / 45479496288](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45479496288) passed on exact source SHA above. Unit tests/debug build, emulator instrumentation (2/2; Room migration and nested SAF integration), signed release build and signature verification all passed. Read the latest dated checkpoint in `MASTER_CONTINUATION_PACK.md` for artifact IDs/digests.
3. Library Browser metadata aggregation and deterministic sorting is complete for that tested commit. It preserves three modes, normalized folder paths, source separation, unknown-date ordering and deterministic tie-breaks.
4. Re-inspect live product spec, implementation plan, `PROJECT_STATUS.md`, source and branch heads before choosing the next internal task. Candidate broader Library/Storage work includes background/incremental MediaStore scanning, permission-scope/reselection behavior, SAF persisted-grant lifecycle/revocation, and folder hierarchy integration; establish what is already implemented before selecting.
5. Before coding, state Main Quest, bundled Side Quests, exact files, risks, acceptance criteria and regression tests. No casual Room schema changes.
6. Keep real USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade as outstanding device gates until physically verified.
7. Preserve local-first privacy, package ID `com.northstar.player`, permanent signing identity, all eight approved product areas as one coordinated public release, and no intermediate APK distribution.
8. After each successful milestone, update native `PROJECT_STATUS.md` and all four continuity records before moving on. Never put signing secrets or keystore bytes in tracked docs.


## Latest restart instruction — 25 November 2026, after Folder NEW-state CI run #68
1. Confirm native HEAD is `d00f920399d699981532985c5e07a9c4b23035fe` or inspect any newer commit.
2. CI [run #68 / 45483698896](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45483698896) passed on exact source SHA above. Unit tests/debug build, emulator instrumentation (2/2), signed release build and signature verification passed. Diagnostics artifact ID `14964397667`; release artifact ID `14963031377`.
3. Folder NEW-state lifecycle is implemented using existing discovery/acknowledgement columns, with shared MediaStore/SAF discovery logic, folder-open acknowledgement and regression coverage. No schema migration was added.
4. Read the latest dated checkpoint in the master pack. Re-inspect live product spec, implementation plan, `PROJECT_STATUS.md`, and source before selecting the next internal task. Pull-to-refresh and the Private Library long-pull shortcut are distinct approved behaviors; authentication must remain mandatory. Also check remaining scanning, permission and SAF grant lifecycle gaps.
5. Before coding, declare Main Quest, bundled Side Quests, exact files, risks, acceptance criteria and regression tests. No casual Room schema changes.
6. Physical USB/SD, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing remain unverified device gates. No intermediate APK distribution; all eight product areas remain one coordinated public release.
7. After each successful milestone, update native `PROJECT_STATUS.md` and all four continuity records before proceeding. Never include signing secrets or keystore bytes.


## Latest restart instruction — 26 November 2026, after pull-to-refresh CI run #74

1. Confirm native branch `phase-0/release-signing` and inspect live HEAD; tested source commit is `7925d39b466897c23b74d76c9a8f08112185ec68`.
2. CI [run #74 / 45494565813](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45494565813) completed success on retry attempt 2. Unit tests/debug build, emulator instrumentation (2/2), signed release build and signature verification passed.
3. Pull-to-refresh target selection has four passing JVM tests. Instrumentation diagnostics artifact `15089184886` confirms Room migration and nested SAF provider integration, 2 passed / 0 failed / 0 errors / 0 skipped. Debug artifact `15089374232`; Room schema artifact `15090132138`; signed release artifact `14967587841` (release job passed in original attempt and was retained during retry).
4. Read the latest dated checkpoint in `MASTER_CONTINUATION_PACK.md` and native `PROJECT_STATUS.md`. Inspect the current product spec, implementation plan, source and live branch heads before choosing the next internal task.
5. Pull-to-refresh milestone is CI-verified. Do not confuse CI with Xiaomi 14/physical-device acceptance. Outstanding device gates: physical USB/SD providers, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing upgrade, and manual pull gesture behavior.
6. Ordinary pull-to-refresh is separate from the Private Library pull-past-threshold-and-hold shortcut. Private Library must always require authentication; do not implement or describe any authentication bypass.
7. Before next coding milestone, declare Main Quest, bundled Side Quests, exact files, risks, acceptance criteria and regression tests. No casual Room schema changes.
8. Preserve local-first privacy, package ID `com.northstar.player`, permanent signing identity, AMOLED black-and-white direction, exactly three library modes, all eight approved product areas as one coordinated public release, and no intermediate APK distribution.
9. After each successful milestone, update native `PROJECT_STATUS.md` and all four continuity records before proceeding. Never include signing secrets or keystore bytes.



## Latest restart instruction — 26 November 2026, after MediaStore reconciliation CI run #45764030633

1. Confirm native HEAD and inspect the live branch; this checkpoint tests exact source SHA `87183de1d23ebede848d6c59eae9106358e9093e`.
2. Workflow [#45764030633](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45764030633) completed success. JVM unit tests/debug build, emulator instrumentation (4/4; 0 failures/errors/skips), signed release build and `apksigner` verification passed.
3. Instrumentation XML confirms: `mediaStoreSnapshotPublicationHidesMissingRowsWithoutDeletingTheirMetadata`; `abandonedMediaStoreStageCanBeDiscardedWithoutChangingVisibleSnapshot`; `migrationFromV1PreservesLibraryAndCreatesIsolatedStagingTable`; `createsAndListsDirectoryInsideNestedDocumentProviderFolder`. Diagnostics artifact ID `15092115234`, ZIP SHA-256 `f032950c5600d4ebe966aa9e6f3418ad8b20de1f8e5b696fe3c3a4c748ed05d1`.
4. Release artifact ID `15091590357`, ZIP SHA-256 `f5589bc6578fb8aae8c39699b15d674e9520a1387090e51a1d3cf615ea0aafff`; extracted APK SHA-256 `b35e9a7cf6fa1d3e18c890e5086d6c76bb9b05ec334d63954a2c92135a558c56`. Debug artifact ID `15092045614`; Room schema artifact ID `15092055600`.
5. The milestone implements permission-aware full/partial/denied reconciliation, atomic staging/publication, preservation of historical metadata on missing/denied media, stale MediaStore folder deactivation with identity retention, and permission checks on callback/app foreground. No Room schema migration; SAF remains isolated.
6. Re-read current product spec, implementation plan, native status and live source before choosing the next task. Explicitly map background/incremental scan work, SAF grant/revocation lifecycle and hierarchy integration to actual code; do not assume gaps.
7. Keep USB/SD provider behavior, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing and manual pull gesture as unverified device gates until physically tested. CI is not device acceptance.
8. Preserve local-first privacy, package ID/signing identity, AMOLED black-and-white direction, exactly three library modes, all eight approved product areas as one coordinated public release, and no intermediate APK distribution. Before coding, state Main Quest, bundled Side Quests, files, risks, acceptance criteria and tests. No casual Room schema changes.




## Latest restart instruction — 26 November 2026, after SAF grant reconciliation CI run #97
1. Confirm native branch HEAD is `e37544cd19294774846e19dbd72657cb4307d7d4` or inspect any newer commit. Check live continuity branch HEAD as well.
2. CI [run #97 / 45775813217](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45775813217) completed success on exact source SHA `e37544cd19294774846e19dbd72657cb4307d7d4`. Both jobs passed: JVM unit tests/debug build, emulator instrumentation, signed release build and `apksigner` verification.
3. Diagnostics artifact `15096549503` confirms 4 instrumentation tests passed / 0 failures / 0 errors / 0 skipped. The four SAF reconciliation policy JVM tests also passed in `:app:testDebugUnitTest`.
4. Release artifact `15096530730`; ZIP SHA-256 `7efceb79324d0d250afc019f1dcd1f4d932d6ff49a8b7414b3dd9dd33328fe01`; extracted APK SHA-256 `3c714faaadfa1ffd73aa9b004dba6569b6ba64f1cdd5ddf8a2cd260c0232bfb2`. Debug artifact `15096619296`; Room schema artifact `15097206733`. No Room schema migration was introduced.
5. SAF grant lifecycle reconciliation is CI-verified: unreachable/revoked grants mark sources unavailable while preserving historical metadata; newly reachable grants trigger a rescan; continuously available grants are not recursively rescanned by periodic/foreground reconciliation.
6. Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing and manual pull/Private Library long-pull behavior remain unverified device gates. Do not claim device acceptance. No APK distributed.
7. Before selecting the next task, inspect the live product spec, integrated implementation plan, native source/status, and both branch heads. Declare Main Quest, bundled Side Quests, files, risks, acceptance criteria and regression tests before coding.
8. Preserve package ID `com.northstar.player`, permanent signing identity, local-first privacy, AMOLED black-and-white direction, exactly three library modes, all eight product areas as one coordinated public release, and no intermediate APK distribution. No casual Room schema changes.


---

## HISTORICAL RESTART CHECKPOINT — 26 NOVEMBER 2026 — INCREMENTAL MEDIASTORE CI PENDING AT THE TIME (SUPERSEDED BY VERIFIED RESTART INSTRUCTION BELOW)

1. Read the latest checkpoint in `MASTER_CONTINUATION_PACK.md`.
2. Inspect live native branch `phase-0/release-signing`; current source commit at checkpoint is `49a73222f29b28fba8924210aef5a8b10fe17fdf`.
3. Poll CI [run #45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016), which tests that exact SHA. At checkpoint time: unit tests/debug build passed; signed release build and `apksigner` verification passed; emulator database migration instrumentation remained in progress. Release artifact ID `15098767237`, ZIP SHA-256 `cf039350a15537da866882e36ec0ecb4c8e9fcc26456b328b78c07b0b404ad49`.
4. If CI fails, inspect logs and instrumentation diagnostics before edits. If it succeeds, inspect instrumentation XML/HTML, download and inspect Room schema v3, and check in exact schema v2 and v3 snapshots. Then update native `PROJECT_STATUS.md` and all continuity records with verified evidence.
5. Incremental scanning uses MediaStore version/generation checkpoints and atomic staged delta publication. Full scans are fallback for older API, partial permissions, version/checkpoint changes, generation rollback, and forced reconciliation. Manual refresh and the 12-hour periodic worker force full snapshots.
6. Important: incremental generation queries do not generically track deletions. Complete manual and 12-hour periodic full snapshots detect deletions; do not claim immediate deletion detection.
7. Keep SAF isolated. Preserve package ID/signing identity, local-first privacy, AMOLED black-and-white design, exactly three library modes, eight-area coordinated public release, and no intermediate APK distribution.
8. Physical USB/SD, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing, and manual pull gesture remain unverified device gates. No device acceptance claims.
9. Do not start the next milestone until this CI/schema verification and documentation update are complete. Never include signing secrets or keystore bytes.
## Latest restart instruction — 26 November 2026 — incremental MediaStore CI passed

1. Read the latest verified checkpoint in `MASTER_CONTINUATION_PACK.md` and native `PROJECT_STATUS.md`.
2. CI run [45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016) passed on exact source SHA `49a73222f29b28fba8924210aef5a8b10fe17fdf`. Both debug and signed-release jobs passed. Emulator instrumentation XML: 5 tests, 0 failures, 0 errors, 0 skipped.
3. Next task: check in exact generated Room schema snapshots v2 and v3 under `android/app/schemas/com.northstar.player.core.database.NorthstarDatabase/`. Version 2 artifact ID `15097206733`; version 3 artifact ID `15099991005`. Both were downloaded and parsed; v3 adds nullable MediaStore version/generation checkpoint fields. Neither schema file is currently in the native repository. Do not reconstruct v2.
4. After schema check-in, verify live branch heads and update native status plus all four continuity records. Do not begin another engineering milestone before this housekeeping is complete.
5. Incremental generation scans do not generically detect deletions; full manual and 12-hour periodic scans reconcile deletions.
6. No APK was distributed. Physical USB/SD, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing and manual pull gesture remain unverified device gates. Preserve all project guardrails and do not claim device acceptance.


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


## Restart instruction — 27 November 2026
1. Confirm both live branch heads before editing.
2. Schema v2/v3 check-in is complete. Fetch and parse `android/app/schemas/com.northstar.player.core.database.NorthstarDatabase/2.json` and `3.json`; do not recreate them.
3. Prior incremental MediaStore milestone is CI-verified on source SHA `49a73222f29b28fba8924210aef5a8b10fe17fdf`, run [45782253016](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45782253016), 5/5 instrumentation tests, debug and signed release jobs passed.
4. The schema commits are documentation/database-contract files only; they have not been independently rebuilt. Do not claim a new app-code CI run for them.
5. Inspect current product spec, implementation plan, actual source and live branch heads before choosing the next engineering milestone. Explicitly declare Main Quest, bundled Side Quests, files, risks, acceptance criteria and regression tests before coding.
6. Preserve incremental deletion limitation: manual full refresh and 12-hour forced full scan reconcile deletions.
7. Keep physical-device gates open until physically tested; no APK distribution. Preserve all package, signing, privacy, design, three-mode library and coordinated-release guardrails.


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
