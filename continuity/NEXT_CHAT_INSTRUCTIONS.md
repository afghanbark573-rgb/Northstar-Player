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
