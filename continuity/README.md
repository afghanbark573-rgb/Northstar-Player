# Northstar Player — Continuity Archive

This branch is a documentation-only continuity checkpoint. It is based on `phase-0/release-signing` and does not change the native product branch or release workflow.

## Canonical records
- [Master continuation pack](MASTER_CONTINUATION_PACK.md) — current project handover and engineering state.
- [Change log](CHANGELOG.md) — checkpoint update history.
- [Next-chat instructions](NEXT_CHAT_INSTRUCTIONS.md) — exact restart procedure.

The downloadable Word version for this checkpoint is attached in the ChatGPT conversation dated 23 November 2026. The GitHub master Markdown is the repository's searchable, version-controlled canonical text copy.

## Update policy
At each major milestone and before moving to a new chat:
1. Inspect the live native branch and current repository state.
2. Update `PROJECT_STATUS.md` on the native branch when project status changes.
3. Update this branch's master pack, changelog and next-chat instructions.
4. Record exact source commit, CI run and artifacts, and distinguish CI evidence from device verification.
5. Preserve prior decisions and mark superseded material; do not silently erase history.

Never store signing secrets, keystore bytes, private media, or recovery answers in this branch.
