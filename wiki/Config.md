# Config Reference

Config file location: `.minecraft/config/aimassist.json`

You can edit this file by hand while the game is closed, or use the in-game config screen (`;`).

---

## Options

### `maceMode`

| | |
|---|---|
| Type | boolean |
| Default | `true` |

Enables the Mace Auto-Hit feature. When `true`, the mod will automatically attack the locked target when you are falling with a mace and enter smash range.

Set to `false` if you only want the aim assist rotation without the auto-attack.

---

### `aimSmoothness`

| | |
|---|---|
| Type | float |
| Range | `0.05` – `1.0` |
| Default | `1.0` |

Controls how quickly the camera rotates towards the target each tick.

| Value | Behaviour |
|---|---|
| `1.0` | Instant snap — camera jumps to target immediately |
| `0.5` | Medium pull — reaches target in a few ticks |
| `0.1` | Gentle pull — slow, smooth tracking |
| `0.05` | Very gentle — barely noticeable assist |

Values below `0.05` are clamped to `0.05` so the aim never completely stops moving.

---

## Example File

```json
{
  "maceMode": true,
  "aimSmoothness": 1.0
}
```
