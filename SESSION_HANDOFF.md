## 🧠 Codebase Intelligence
- PR merge cause massive code duplicate + bracket syntax break.
- Conflict in `ConnectionMixin`, `BlinkModule`, `Backtrack`, `AimAssist`, `Triggerbot` resolve.
- Mojmap mapping mismatch (e.g. `SwordItem` not exist → string `contains("sword")` fix).
- Outgoing packet dump in `BlinkModule` use `mc.getConnection().send(packet)`. Incoming in `Backtrack` use `typed.handle(mc.player.connection)`.

## 🚀 Future Roadmap
- CrystalAura embedded killaura very broken → flags Simulation/MultiActions/Timer.
- Plan: Remove embedded killaura from CrystalAura. Require separate KillAura module use.
- Backtrack/Hitbox packet order verify (queue structure sync with server ticks).
- Fix "hit through block" logic in KillAura (add raycast check).

## 🤖 AI Context & Handoff
- Clean-first codebase policy enforce. Duplicate loops remove.
- Build success with Fabric Loom 1.15.5 (`compileClientJava` pass).
- Caveman mode active (ultra-compressed responses).
- Mental model: Combat modules must NEVER bypass RotationManager/Queue syncing to avoid anticheat flags.
