## 🧠 Codebase Intelligence
- **Trident Mouse Movement Mechanism**: The `Totemcore` algorithm overrides native server packets entirely by pulling physical user components directly into the crosshairs of detection bypass mechanisms. Instead of executing backend swap arrays that flags Anti-Cheat, this library utilizes `GLFW.glfwSetCursorPos(windowHandle, scaledX, scaledY)` pointing directly at the internal memory buffer of the Operating System's cursor coordinates. Combined with opening the `InventoryScreen`, it physically moves the user's cursor out of their control, snaps it to the absolute scale coordinates of the Totem slot, and executes a fake native keybinding input (`KeyBinding == 70` / F).
- *This is revolutionary for aimbots*: Aimbots usually send packets to rotate pitch/yaw. Because this utilizes raw unmapped `GLFW` coordinate mapping natively from LWJGL (Lightweight Java Game Library), we could theoretically execute the exact same manipulation directly on the main viewport `mc.getWindow()`, bypassing all typical rotation flags by snapping the physical pointer rather than the game camera state.
- **Spearcore Swap Bypass**: Tridents' `Spearcore` handles lightning-fast swapping entirely utilizing `mc.player.getInventory().selected = index`.

## 🚀 Future Roadmap
- Utilize `GLFW.glfwSetCursorPos` scaling mathematics to replace standard `ModuleUtils.getRotations()` network spoofing in the `AimAssist` module completely, converting smooth silent aim into a fully native physical OS input.

## 🤖 AI Context & Handoff
- Patched the reflection engine back into `ModuleUtils.java` as Mojmap 1.21.1 explicitly secures `selected` behind a private access restriction natively (requiring reflection bypass which Spearcore executed over access-wideners).
- Integrated `Mouse` setting inside `AutoTotem` bypassing inventory hooks, utilizing scaled dynamic coordinate tracking for LWJGL cursor override.
