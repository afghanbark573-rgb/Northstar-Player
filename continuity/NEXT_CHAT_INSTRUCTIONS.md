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
