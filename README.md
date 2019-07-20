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
    on_win: eco give %1$s 500
    start_position: "0, 64, 0"
    end_position: "88, 90, 73"
    checkpoints: [
      "10, 68, 10",
      "15, 75, 19"
    ]
    start_world: world
    end_world: world
    checkpoint_text: "Checkpoint\n%2$s"
    start_text: "%1$s\n%2$s Checkpoints"
    end_text: "%1$s: Finish"
name_error: "§c[Parkour] That is not a valid parkour!"
leaderboard_header: "§l§9Leaderboard for %1$s"
leaderboard_item: "§a%1$s. %2$s - %3$ss"
start_message: "§9[Parkour] Starting run."
checkpoint_message: "§9[Parkour] Checkpoint: %1$ss"
end_message: "§a[Parkour] Your time was %1$ss."
end_with_best: "§a[Parkour] Your time was %1$ss. Your best time is %2$ss."
end_with_record: "§a[Parkour] Your time was %1$ss. Your best time is %2$ss. New record!"
new_record_announce: "§1[Parkour] %1$s §9has set a new record of %2$ss for parkour %3$s!"
run_complete_announce: "§1[Parkour] %1$s §9has has completed %2$s in %1$ss!"
```
```yaml
Formatting keys:
  on_win: This is the command executed upon winning a parkour
      %1$s: Player's name
  checkpoint_text: This is the text that appears above each checkpoint as a hologram
      %1$s: Parkour name
      %2$s: Checkpoint number
      %3$s: Total checkpoints
  start_text: This is the text that appears above the start as a hologram
      %1$s: Parkour name
      %2$s: Total checkpoints
  end_text: This is the text that appears above the end as a hologram
      %1$s: Parkour name
      %2$s: Total checkpoints
  name_error: This is the error message when a user attempts to look at the leaderboard for a parkour that doesn't exist
  leaderboard_header: This is the text at the top when a leaderboard is displayed
      %1$s: Parkour name
  leaderboard_item: This is the text for each entry in the leaderboard
      %1$s: Rank (i.e. 1, 2, 3)
      %2$s: Player's name
      %3$s: Player's time
  start_message: The message sent to the user when they begin the run
  checkpoint_message: The message sent to the user when they hit a checkpoint
      %1$s: Current run time
  end_message: The message sent to the user when they end the run for the first time
      %1$s: The user's time
  end_with_best: The message sent to the user when they end the run
      %1$s: The user's time
      %2$s: The user's best time
  end_with_record: The message sent to the user when they set a new personal record
      %1$s: The user's time
      %2$s: The user's best time
  new_record_announce: The message broadcast to the server when they set a new record
        %1$s: Player's name
        %2$s: Player's time
        %3$s: Parkour name
  run_complete_announce: The message broadcast to the server when a player completes a parkour
        %1$s: Player's name
        %2$s: Player's time
        %3$s: Parkour name
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