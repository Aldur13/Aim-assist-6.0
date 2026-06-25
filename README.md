# Aim Assist — Minecraft 26.x Fabric Mod

A client-side Fabric mod that locks your crosshair onto the nearest player with a single keypress, and can automatically land a mace smash attack at the perfect moment.

---

## Features

| Feature | Description |
|---|---|
| **Aim Assist** | Press `J` to toggle. Your camera will always point at the nearest player. |
| **Mace Auto-Hit** | While falling with a mace, the mod fires the attack at exactly the right moment for maximum smash damage. Toggle in the config. |
| **Config Screen** | Press `;` in-game to open the config screen. All settings are saved to disk automatically. |

---

## Controls

| Key | Action |
|---|---|
| `J` | Toggle aim assist on/off |
| `;` | Open config screen |

Both keys can be rebound in **Options → Controls → Aim Assist**.

---

## Config Options

Open with `;` in-game, or edit `.minecraft/config/aimassist.json` directly.

| Option | Default | Description |
|---|---|---|
| `maceMode` | `true` | Auto-hit with mace when falling and aim assist is on |
| `aimSmoothness` | `1.0` | How fast the aim locks on. `1.0` = instant snap, `0.05` = gentle pull |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.x.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and drop it in your `mods/` folder.
3. Drop the `aim-assist-x.x.x.jar` into your `mods/` folder.
4. Launch the game.

---

## Building from Source

Requires Java 21 and an internet connection (Gradle downloads dependencies automatically).

```bash
git clone https://github.com/Aldur13/Aim-assist-6.0.git
cd Aim-assist-6.0
./gradlew build
```

The built jar will be at `build/libs/aim-assist-1.0.0.jar`.

> **Note:** You may need to update `yarn_mappings` and `fabric_version` in `gradle.properties`
> to match the exact 26.x build you are targeting. Check [FabricMC Meta](https://meta.fabricmc.net/)
> for the current values.

---

## Compatibility

- **Minecraft:** 26.x (all builds)
- **Mod loader:** Fabric
- **Side:** Client only — safe to use on any server

---

## Wiki

Full documentation is in the [`wiki/`](wiki/) folder:

- [Home](wiki/Home.md)
- [Installation](wiki/Installation.md)
- [Features](wiki/Features.md)
- [Config](wiki/Config.md)

---

## License

MIT
