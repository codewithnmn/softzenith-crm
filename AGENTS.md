# AGENTS.md

Instructions for Codex and any other coding agent. Claude Code reads the same rules from `CLAUDE.md`.

1. Read **`CLAUDE.md`**: project overview, ground rules, stack, commands, architectural rules, settled decisions.
2. Read **`HANDOFF.md`**: current task status, the last session, the next action and open questions.
3. Read **`docs/flows.md`** (use-case diagrams, flowcharts, state diagrams) before changing any flow it covers,
   and update it via `docs/generate_flows.py` when you do.
4. Work only on the next task in HANDOFF.md, following the rules in CLAUDE.md.
5. Keep `cd backend && ./mvnw test` green.
6. Before you stop, update HANDOFF.md using the template at the bottom of CLAUDE.md.

If CLAUDE.md and this file ever disagree, CLAUDE.md wins. Update CLAUDE.md, not this file.
