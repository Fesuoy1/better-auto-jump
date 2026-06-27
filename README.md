# Better Auto-Jump

A Minecraft Fabric mod that replaces and vastly improves the vanilla auto-jump system. Client-side only.

## Features

- **Improved Auto-Jump** -- Predicts collisions in the direction of movement and automatically jumps over blocks up to a configurable height. Accounts for the Jump Boost potion effect and respects an angle threshold so you only jump over blocks in your general path.
- **Edge Jumping (Parkour Helper)** -- Detects when you're approaching the edge of a block while moving forward and triggers a jump just before the edge. Includes built-in presets and full parameter customization. (Disabled by default, enable in mod's settings)
- **Sprint Persistence** -- Maintains sprinting momentum through auto-jumps.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API for Minecraft 26.2.
3. Download the mod JAR from [Releases](https://github.com/Fesuoy1/better-auto-jump/releases).
4. Place the JAR in your `mods` folder.

## Configuration

Edit `<game_dir>/config/better-auto-jump.json` manually, or use the **ModMenu** GUI (recommended). The GUI offers:

- **Master Toggle** -- Enable/disable the entire mod.
- **Obstacle Jump / Edge Jump** -- Individual feature toggles.
- **Preset Selector** -- Switch between built-in and saved custom presets.
- **Variance / Cooldown** -- Random tick delay (0–5) and minimum ticks (1–10+) between edge jumps.
- **Advanced Settings** -- Fine-tune all detection parameters and save custom presets.

### Edge Detection Parameters

| Parameter | Default | Description |
|---|---|---|
| Sprint Distance | 2.0 | Max scan distance when sprinting |
| Walk Distance | 1.5 | Max scan distance when walking |
| Step Distance | 0.35 | Distance between scan samples |
| Start Distance | 0.3 | Distance from player to start scanning |
| Scan Radius | 0.3 | Horizontal radius of the scan area |
| Vertical Offset | 0.05 | Vertical offset for the scan area |
| Min Velocity | 0.1 | Minimum player speed to trigger |
| Solid Min | 0.001 | Min ground height at feet (edge if below) |
| Solid Max | 0.6 | Max ground height at feet |

### Obstacle Detection Parameters

| Parameter | Default | Description |
|---|---|---|
| Jump Height | 1.2 | Max block height to auto-jump |
| Prediction Mult | 6.0 | How far ahead to check for collisions |
| BPT Clamp Max | 0.8 | Max blocks-per-tick clamp |
| BPT Slow Threshold | 0.2 | Slow movement threshold |
| BPT Slow Multiplier | 0.7 | Multiplier applied during slow movement |
| Angle Threshold | 30.0 | Max angle between movement and obstacle |

### Presets

| Preset | Sprint Dist | Walk Dist | Description |
|---|---|---|---|
| **Default** | 2.0 | 1.5 | Balanced edge detection |
| **Precision** | 1.2 | 1.0 | Tighter, more precise jumps |
| **Long Range** | 3.5 | 2.5 | Detects edges from further away |
| **Aggressive** | 2.5 | 2.0 | Larger scan radius, more responsive |

Unlimited custom presets can be saved from the Advanced Settings screen.

## License

MIT -- see [LICENSE](LICENSE) for details.
