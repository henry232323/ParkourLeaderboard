# ParkourLeaderboard

This plugin aims to allow servers to track their players' parkour runs.

## Commands
### /parkour <name>
See the top 10 leaderboard for a parkour.

## Config


Preloaded with the plugin is an example parkour config file.

`config.yml`
```yaml
parkours:
  example_parkour:
    on_win: eco give %s 500
    start_position: "0, 64, 0"
    end_position: "88, 90, 73"
    checkpoints: [
      "10, 68, 10",
      "15, 75, 19"
    ]
    start_world: world
    end_world: world
```
The name of the parkour is `example_parkour`.
Upon winning the parkour, the command `/eco give {playername} 500` is executed.
The start position is at "0, 64, 0". This is the coordinates of the pressure plate that marks the beginning.
The end position is at "88, 90, 73". This is the coordinates of the pressure plate that marks the end.
Checkpoints are the coordinates of pressure plates that occur between the beginning and end. Stepping on one will alert the player of their current time.
The `start_world` and `end_world` are the world names which the parkour begins and ends with.
The checkpoints assume the same world as the start_world, unless specified  by setting a checkpoint to `"x, y, z, worldname"`

The `leaderboards/example_parkour.yml` is an example of a file for storing runs, these are automatically generated and may be
doctored manually.