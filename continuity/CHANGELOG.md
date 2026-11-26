# Continuity changelog

## 2026-11-23 — Initial master checkpoint
- Created dedicated `continuity/master-pack` branch from `phase-0/release-signing`.
- Added canonical master continuation pack in Markdown.
- Recorded eight locked product areas, technical baseline, implementation history, CI evidence, known UI limitations, storage audit and next SAF engineering sequence.
- Added this archive's update policy and next-chat restart instructions.
- No application source files or release branch were changed by this continuity checkpoint.

## 2026-11-24 — SAF provider process-isolation mitigation and run #56 checkpoint
- Inspected the run #55 diagnostics artifact and found provider process startup timeouts / process termination, without an actionable SAF assertion stack trace.
- Committed test-only packaging mitigation at native HEAD `07b96f31a4cbab9cfcad1e0ccfa4f81232142960`: moved the in-memory DocumentsProvider to the debug app source set and registered it in the debug manifest; removed the androidTest provider source/manifest. Production SAF implementation and integration assertions were not changed.
- Run #56 (`45411300973`) release job passed signed build and signature verification; debug unit tests and debug build passed. At checkpoint, Android instrumentation remained in progress, so the overall run and SAF integration result are unresolved.
- Updated native `PROJECT_STATUS.md` and master continuation pack with exact evidence and next steps. Physical USB/SD and Xiaomi 14 acceptance remain outstanding. No intermediate APK distribution.


## 2026-11-24 — Run #56 diagnosis and SAF test-provider manifest fix
- Run #56 completed: unit tests/debug build and signed release/signature verification passed; instrumentation failed before starting tests.
- Inspected full job log and identified the exact crash: `SecurityException: Provider must be protected by MANAGE_DOCUMENTS` from `DocumentsProvider.attachInfo` while installing the debug test provider.
- Fixed the debug-only provider declaration by adding `android:permission="android.permission.MANAGE_DOCUMENTS"` in native commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`. Production SAF code and integration assertions were unchanged.
- Updated native `PROJECT_STATUS.md` and this continuity archive. Next gate: verify the CI run triggered by the manifest correction and confirm both instrumentation tests actually run and pass.


## 2026-11-24 — Run #57 started
- The debug-only SAF provider manifest correction triggered run [45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513), testing source commit `3ae4586c6c0753b48001bde8ca2a61fb3c6bba0b`.
- At the latest poll, debug and release jobs were still running and instrumentation had not started. Result remains pending.
- Native status and continuity master pack updated with the active run ID and exact verification gate.


## 2026-11-24 — Run #57 diagnosis and nested SAF test-provider correction
- Run [45441499513](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45441499513) completed failed. JVM unit tests/debug build passed; Room migration instrumentation passed 1/1; SAF DocumentsProvider integration ran and failed 1/1 with `SecurityException: Document nested-parent is not a descendant of test-root` from `DocumentsProvider.enforceTree`.
- Inspected the XML report, textproto result and per-test logcat. The exact defect was the debug in-memory provider's missing `isChildDocument` implementation; Android could not verify that the nested test document belonged to the root tree. This is test-provider contract behavior, not evidence of a production SAF failure.
- Signed release APK build, `apksigner` verification and artifact upload passed.
- Implemented synchronized parent-chain descendant checks in debug-only provider in native commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`. Production SAF implementation and test assertions unchanged.
- Updated native `PROJECT_STATUS.md` with exact failure and correction. New CI validation is required before closing SAF gate; no intermediate APK distribution.


## 2026-11-24 — Run #58 started for SAF descendant correction
- Run [45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) was triggered by native commit `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
- Latest poll: debug unit/build and signed release build were active; instrumentation pending. No result claimed. Native status and continuity pack updated with this in-progress gate.


## 2026-11-24 — Durable continuity checkpoint / chat-independent project record
- Declared the native branch and continuity branch as the permanent source of truth; documented authority order, required live-repository inspection and the no-secrets rule.
- Recorded run #58 exact source SHA `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`: JVM unit tests/debug APK build passed; signed release build, `apksigner` verification and artifact upload passed. Room migration and SAF DocumentsProvider instrumentation remained in progress at the latest poll; no test pass claimed.
- Recorded signed artifact ID `14944179159` and SHA-256 `f9a71cc8718d67485a41c3663a194a8dcfd08d68d3548b94e45925fe124dc0eb`; artifact is not for intermediate distribution.
- Added exact restart steps and the rule to update native status plus all continuity files after each milestone. No app source changed by this continuity update.

 
## 2026-11-25 — Run #58 passed; SAF instrumentation gate closed
- Run [45444674837](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45444674837) completed successfully on exact native source SHA `35bd3cd64ff9ecf7d5269e591dd69c54d5aa4ee4`.
- Debug unit tests/build and connected instrumentation succeeded. The diagnostics artifact reports 2 instrumentation tests, 0 failures, 0 errors, 0 skipped: Room v1-to-v2 migration passed 1/1; nested SAF DocumentsProvider create/list integration passed 1/1. Test log: `OK (2 tests)`.
- Signed release build, `apksigner` verification and artifact upload passed. Release artifact ID `14944179159`; SHA-256 `f9a71cc8718d67485a41c3663a194a8dcfd08d68d3548b94e45925fe124dc0eb`. CI artifact only; not distributed.
- SAF emulator CI gate is closed for this tested commit. Real USB/SD provider behavior, picker/persisted grants, Xiaomi 14 acceptance and signed upgrade-over-existing remain unverified device gates.
- Next planned internal milestone: inspect current source and implement Library Browser folder metadata aggregation/sorting with defined regression coverage. No intermediate APK release.


## 2026-11-25 — Library Browser metadata aggregation and deterministic sorting verified
- Native commit `9b84ea24a1f11671aca54c10a339dc161307473d`; CI [run #60 / 45479496288](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45479496288) completed success on exact HEAD.
- Folder metrics now aggregate by normalized ancestor path in one pass; deterministic folder/video tie-breakers and known-before-unknown date ordering are covered; source separation and the three approved modes are preserved.
- JVM unit tests/debug build passed. LibraryBrowser regression class contains ten tests. Emulator instrumentation: Room migration 1/1 passed; nested SAF create/list 1/1 passed; total 2 passed, 0 failed/errors/skipped.
- Signed release build and `apksigner` verification passed. Release artifact ID `14960903935`, SHA-256 `086e153dbfdaddd2aa0478e9fc2e160e6ffc145e0141dc41088d32b43fc899c5`; CI only, not distributed.
- Diagnostics artifact ID `14962565694`, SHA-256 `41e5f659cf75907379bc4152fe297ee18912c90b3486f1665e78895d9e6e0043`. Debug artifact ID `14962885359`, SHA-256 `45a813a101cc58d6e5e0d0483b0825fdf3196f002cf5461678a2e316c09e58c6`. Room schema artifact ID `14961903353`, SHA-256 `bd3f21aa0d42ab1a5d15220f16d916d60d7200d5755cc0c900baee527ed5a239`.
- Earlier run #59 also passed on the intermediate code-only commit; run #60 is authoritative for final code plus tests.
- Device gates remain open: physical USB/SD behavior, real picker/persisted grants, Xiaomi 14 acceptance, and signed install-over-existing upgrade. No APK distributed.
- Next: inspect the current implementation plan and adjacent code before selecting the next Library/Storage foundation task. No scope assumptions or schema changes.


## Folder NEW-state lifecycle — CI run #68 passed — 2026-11-25

- Native source commit: `d00f920399d699981532985c5e07a9c4b23035fe` on `phase-0/release-signing`.
- Workflow [run #68 / 45483698896](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45483698896) completed with overall conclusion **success** on that exact commit.
- Debug job passed `:app:testDebugUnitTest`, `:app:assembleDebug`, and `:app:connectedDebugAndroidTest`. The unit-test task includes six focused `FolderNewStateTest` cases and the repository discovery/acknowledgement regression test.
- Emulator API 35 instrumentation diagnostics artifact ID `14964397667`, SHA-256 `606acb2774245aa223605da8217ebf4dcf77067ff421894bb2da20443bd087ab`, confirms **2 tests, 0 failures, 0 errors, 0 skipped**: Room migration/acknowledgement assertion passed; nested SAF DocumentsProvider create/list integration passed. XML reports `tests="2" failures="0" errors="0" skipped="0"`; test log ends `OK (2 tests)`.
- Release job passed release keystore restoration, `:app:assembleRelease`, `apksigner` verification and signed artifact upload. Release artifact ID `14963031377`, SHA-256 `58f2c260ec92394e8b6a3c93b523925a699c297353a207f9ad5b6026d9a253bb`.
- Debug artifact ID `14964815811`, SHA-256 `bc15724c02fc50bf63354fd1ab47d31d059a1fa094c6dfe685d5600dce0f4ccc`. Room schema artifact ID `14964681631`, SHA-256 `b18dad2293c8469ac18a972f1b0fb00245848ed3944e354fd032307ae24dd6ce`.
- Implementation reuses existing `discoveredAtMillis` and `acknowledgedAtMillis` fields. Shared `FolderNewState` logic is used by MediaStore and SAF; new descendant videos or child folders advance discovery state; opening a folder acknowledges it. Same-millisecond discoveries are handled without losing the NEW state. No Room schema migration was introduced.
- Physical USB/SD provider behavior, actual picker/persisted-grant lifecycle, Xiaomi 14 acceptance and signed install-over-existing upgrade remain **unverified device gates**. CI artifacts are engineering evidence only. **No APK was distributed**; all eight approved product areas remain one coordinated public release.


## 2026-11-26 — Library pull-to-refresh CI run #74 passed

- Native source SHA: `7925d39b466897c23b74d76c9a8f08112185ec68`.
- Workflow [run #74 / 45494565813](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45494565813) completed success on retry attempt 2. Unit tests/debug build, emulator instrumentation, signed release build and signature verification passed.
- Four `LibraryRefreshTargetsTest` unit cases passed as part of `:app:testDebugUnitTest`; they cover all available SAF trees on the main/external-storage pages, only the owning tree for a SAF folder, and no unrelated SAF rescan for a MediaStore folder.
- Diagnostics artifact ID `15089184886`, SHA-256 `7c9731584722b1bf4a1bbf18974eabd6a1669acc9259c38131dbd287225088f3`: emulator API 35 XML and test log confirm 2/2 instrumentation tests passed, 0 failures/errors/skips (Room migration and nested SAF provider create/list).
- Debug artifact ID `15089374232`, SHA-256 `1df67ac1d0387179eba3f5d27fae1253ed9e9f3d46783284f7c086146a6c703b`; Room schema artifact ID `15090132138`, SHA-256 `c968ed5b4188c5a74a723daf50ebba810280e48177b8611325e236b61fac8c34`.
- Signed release artifact ID `14967587841`, SHA-256 `c06460e0db3e8d132be0eac7adc0cd136c42014f183a8089cb9f8f03f4ea4dba`; release build and `apksigner` verification passed in the original run attempt and were retained during the debug-only retry.
- Initial attempt's instrumentation stage was blocked by transient emulator/ADB offline/broken-pipe errors; retry attempt 2 passed without an application-code change for that infrastructure issue.
- CI evidence only. Physical pull gesture behavior, real USB/SD, picker/persisted grants, Xiaomi 14 acceptance and signed upgrade-over-existing remain unverified. No APK distributed; eight approved areas remain one coordinated release.
- Next milestone selection must re-inspect the live implementation plan/source. Keep ordinary pull-to-refresh distinct from the Private Library long-pull authentication shortcut.



## 2026-11-26 — Permission-aware MediaStore reconciliation passed CI

- Native source commit `87183de1d23ebede848d6c59eae9106358e9093e`; workflow [#45764030633](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45764030633) completed success on the exact SHA. Debug and release jobs passed.
- JVM unit tests/debug build passed. Emulator API 35 instrumentation XML confirms 4 passed, 0 failures/errors/skips: two MediaStore snapshot/staging regression cases, Room v1 migration, and nested SAF provider integration.
- Diagnostics artifact ID `15092115234`, ZIP SHA-256 `8c8888439932a14614e81e50368eb0c909492b4dace52ca880d500e43024e3be`.
- Signed release build and `apksigner` verification passed. Release artifact ID `15091590357`, ZIP SHA-256 `f5589bc6578fb8aae8c39699b15d674e9520a1387090e51a1d3cf615ea0aafff`; extracted APK SHA-256 `b35e9a7cf6fa1d3e18c890e5086d6c76bb9b05ec334d63954a2c92135a558c56`. Debug artifact ID `15092045614`; Room schema artifact ID `15092055600`.
- Atomic completed-snapshot publication, abandoned-stage discard, preservation of historical metadata on missing/denied media, stale-folder deactivation with identity retention, and permission reconciliation on callback/foreground are CI-verified. No Room schema migration was introduced; SAF remains isolated.
- Physical USB/SD, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing and manual pull gesture remain unverified. No APK distributed; all eight product areas remain one coordinated release.
- Next: inspect current plan/spec/source and live heads before selecting the next Library/Storage milestone. No casual schema changes.


## 2026-11-26 — Automatic SAF grant reconciliation (CI-verified)
- Source commit: `e37544cd19294774846e19dbd72657cb4307d7d4`.
- CI run [#97 / 45775813217](https://github.com/afghanbark573-rgb/Northstar-Player/actions/runs/45775813217) passed on the exact commit.
- Persisted SAF grant reachability reconciliation, unavailable-source preservation, reconnect-triggered rescan, and battery-conscious avoidance of recursive rescans for continuously mounted trees.
- Four SAF reconciliation policy unit tests passed as part of `:app:testDebugUnitTest`.
- Emulator instrumentation: 4 passed / 0 failed / 0 errors / 0 skipped, including Room migration and nested SAF provider integration.
- Signed release build and `apksigner` verification passed.
- Diagnostics artifact `15096549503`; release artifact `15096530730`; debug artifact `15096619296`; Room schema artifact `15097206733`. Release ZIP and extracted APK SHA-256: `3c714faaadfa1ffd73aa9b004dba6569b6ba64f1cdd5ddf8a2cd260c0232bfb2`.
- No Room schema migration; no APK distributed.
- Physical USB/SD, real picker/persisted-grant lifecycle, Xiaomi 14 acceptance, signed install-over-existing and manual pull gesture remain device gates.
