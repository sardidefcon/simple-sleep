<p align="center"><img src="https://i.ibb.co/F4NYxFwM/Simple-Sleep.png"/></p>

Simple Minecraft plugin for Paper that skips the night when enough players are sleeping or automatically

## Features

- **Sleep percentage**: Uses Minecraft's native gamerule `playersSleepingPercentage` (applied to overworlds):
  - **0 or 1** = one player sleeping skips the night
  - **2–100** = percentage of players required (e.g. 50 = half)
  - **101 or more** = sleep does not skip the night (disabled)
- **Auto-skip night** (off by default): When enabled, night is skipped automatically as soon as it begins. Togglable in-game with `/sleep autoskip`.
- **Sleep gamerule toggle**: Enable or disable the native sleep mechanic. When disabled, gamerule is set to 101. Togglable with `/sleep toggle`.
- **Phantom control** (doInsomnia): Enable or disable phantom spawning. Togglable with `/sleep phantoms`.
- **Ignore AFK players** (on by default): When EssentialsX is installed, AFK players are not counted for the sleep percentage. The plugin uses its own sleep logic instead of the native gamerule in this case.
- **Custom messages**: Action bar messages when players enter/leave beds and when the night is skipped. Configurable prefix for command responses.

## Commands & Permissions

| Command           | Description                        | Permission (default: op) |
|-------------------|------------------------------------|---------------------------|
| `/sleep autoskip` | Toggle auto-skip night on/off      | `ssleep.toggle.autoskip`  |
| `/sleep toggle`   | Toggle sleep gamerule on/off       | `ssleep.toggle`           |
| `/sleep phantoms` | Toggle doInsomnia (phantoms on/off)| `ssleep.phantoms`         |
| `/sleep reload`   | Reload configuration               | `ssleep.reload`           |

## Requirements

- Java 21 (LTS)
- Paper server (tested with `api-version: "1.21"`, 1.21.1)
- Optional: EssentialsX for "ignore AFK players" (soft dependency)

## Build

```bash
gradle build
```

Plugin JAR: `build/libs/SimpleSleep-1.0.0.jar`

## Installation

1. Copy the JAR to your Paper server `plugins` folder
2. Start or restart the server
3. `config.yml` is created in `plugins/SimpleSleep/` if it does not exist

## Configuration

| Option | Description |
|--------|-------------|
| `prefix` | Prefix for command messages. Use `&` for color codes. Empty = no prefix. |
| `auto-skip-night` | When true, night is skipped as soon as it begins. Togglable with `/sleep autoskip`. |
| `sleep-gamerule-enabled` | When true, native gamerule applies. When false, set to 101 (disabled). Togglable with `/sleep toggle`. |
| `spawn-phantoms` | Native doInsomnia gamerule. true = phantoms can spawn; false = no phantoms. Togglable with `/sleep phantoms`. |
| `sleep-percentage` | Value for playersSleepingPercentage (0–1 = one player; 2–100 = percentage; 101+ = disabled). |
| `ignore-afk-players` | When true and EssentialsX present, AFK players excluded from sleep count. |

**Messages** (use `&` for color codes):

- `messages.players-sleeping`: Action bar when players enter/leave bed. Placeholders: `%sleeping%`, `%total%`. Empty = vanilla only.
- `messages.sleeping-through-this-night`: Shown when sleep skips the night. Empty = vanilla message.
- `messages.auto-skip`: Shown when auto-skip jumps the night.
- `messages.plugin-messages`: reload-success, autoskip-enabled/disabled, toggle-enabled/disabled, phantoms-enabled/disabled, no-permission, usage.

## Internal Behaviour

- On enable and reload, the plugin applies `playersSleepingPercentage` and `doInsomnia` to all overworlds (and newly loaded worlds). When **ignore-afk-players** is true and EssentialsX is present, the sleep gamerule is set to 101 and the plugin uses its own listener to count only non-AFK players and skip the night when the percentage is met.
- When the gamerule is active (and not in AFK-ignore mode), a custom action bar message is sent on bed enter/leave; otherwise vanilla messages apply. When the sleep threshold is met, the "sleeping through this night" message is shown if configured.
- When **auto-skip-night** is enabled, a task runs every second and advances time to morning for each overworld when it is night (13000–23000 ticks).
- Only overworlds (`Environment.NORMAL`) are affected; Nether and End are ignored.
