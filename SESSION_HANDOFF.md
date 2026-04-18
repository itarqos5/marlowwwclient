## 🧠 Codebase Intelligence
- Mappings conflict resolved. Use `owo_version=0.13.0+1.21.11` to avoid `Expected: intermediary` from official releases containing `official` namespace inside accesswideners.
- Owo-lib factory methods moved from `Components`/`Containers` to `UIComponents`/`UIContainers` in `0.13.0`.
- Minecraft's `ResourceLocation` maps to `net.minecraft.resources.Identifier` under Fabric's official-mapped + loom magic in this env.

## 🚀 Future Roadmap
- Test new declarative XML `Clickgui` in-game and tweak margins/surfaces/colors based on visual results.
- Finish cleaning up `AnimationUtil.java` usages. Keep HUD logic intact per user request, but optimize and isolate it.
- Remove embedded `KillAura` logic from `CrystalAura` code to fix flags.

## 🤖 AI Context & Handoff
- Successfully converted `Clickgui.java` into an `owo-ui` powered UI. It compiles!
- We are currently waiting for user to compile/run client to confirm new UI layout matches their `goated` design expectations.
- Caveman mode is active.
