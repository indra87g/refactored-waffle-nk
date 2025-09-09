# RefactoredWaffleNK

RefactoredWaffleNK is a versatile Nukkit plugin that provides a collection of useful commands and features for server administrators and players. This plugin has been refactored for stability, consistency, and ease of maintenance.

## Features

- **Centralized Configuration**: All player-facing messages are located in `messages.yml`, making them easy to customize and translate.
- **Command System**: A robust command system with permissions, aliases, and cooldowns for supported commands.
- **Economy Integration**: Several commands hook into EconomyAPI to provide features like casino games and paid roaming.
- **Reward System**: Includes daily and time-based rewards to encourage player engagement.

## Commands

The following is a detailed list of all available commands.

| Command | Description | Usage | Permission | Aliases | Cooldown |
|---|---|---|---|---|---|
| `/casino` | Play various casino games to win or lose money. | `/casino <coinflip\|slot\|dice> ...` | `waffle.casino` | `bet` | 20s |
| `/calc` | Performs a simple mathematical calculation. | `/calc <num1> <operator> <num2>` | `waffle.calc` | - | - |
| `/clearchat` | Clears a specified number of lines from your chat window. | `/clearchat [amount]` | `waffle.clear.chat` | - | - |
| `/daily` | Claim your daily reward or check your current streak. | `/daily <claim\|status>` | `waffle.daily` | - | - |
| `/reload` | Reloads a specified plugin. | `/reload <pluginName>` | `waffle.reload` | - | - |
| `/roam` | Enter spectator mode for a fee or for a short trial. | `/roam [cancel\|trial]` | `waffle.roam` | - | - |
| `/servers` | Opens a UI to select and connect to another server. | `/servers` | `waffle.servers` | - | - |
| `/setblock` | Places a block at a specific coordinate in the world. | `/setblock <x> <y> <z> <block_id>` | `waffle.setblock` | - | - |

## Configuration

- **`plugin.yml`**: Main plugin definition.
- **`config.yml`**: General plugin settings (if any were to be added).
- **`commands.yml`**: Configure command aliases and cooldowns here.
- **`messages.yml`**: Customize all messages sent to players.
- **`daily_rewards.yml`**: Configure the rewards for the daily command system.
- **`time_rewards.yml`**: Configure time-based rewards.
- **`servers.yml`**: Configure the list of servers for the `/servers` command.

---
*This documentation was automatically generated and reflects the current state of the plugin.*
