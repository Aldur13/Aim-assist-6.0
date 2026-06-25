# Features

## Aim Assist

**Toggle:** `J` (rebindable in Controls)

When enabled, the mod finds the nearest other player in the world each game tick (20 times per second) and rotates your camera to point directly at their eyes.

- Works at any distance — there is no range cap by default.
- While a target is locked, mouse movement is suppressed so your aim never drifts.
- If no other player is in the world, the lock releases and mouse movement returns to normal.
- A brief on-screen message (`Aim Assist: ON / OFF`) confirms the toggle.

The `aimSmoothness` config value (see [Config](Config.md)) controls how instantly the camera snaps to the target. At `1.0` (default) it is frame-perfect. Lower values create a smooth pull effect.

---

## Mace Auto-Hit

**Requires:** Aim Assist ON + Mace Mode enabled in config

The Mace Auto-Hit feature watches for the following conditions every tick:

1. You are holding a **Mace** in your main hand.
2. You are **falling** (downward velocity ≥ 0.15 blocks/tick).
3. You have accumulated at least **1.5 blocks** of fall distance (enough for the Minecraft smash bonus to apply).
4. The locked target is within **3 blocks horizontally** and you are **above** them.

When all four conditions are true, the mod fires `attackEntity` for you and swings your arm — landing the smash at exactly the right moment for maximum bonus damage.

A 10-tick cooldown (0.5 s) prevents the feature from firing twice in the same fall.

---

## Config Screen

**Open:** `;` (rebindable in Controls)

A simple in-game screen with two controls:

| Control | What it does |
|---|---|
| **Mace Mode** button | Toggles Mace Auto-Hit on/off. Changes save immediately. |
| **Aim Smoothness** slider | Adjusts how fast the aim snaps. Drag left for a smoother pull, right for instant lock. |

Settings are written to `.minecraft/config/aimassist.json` on every change and when the screen closes.
