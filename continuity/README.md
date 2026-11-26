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
