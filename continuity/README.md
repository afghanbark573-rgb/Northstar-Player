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
