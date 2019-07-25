package henry232323.parkourleaderboard;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


/*
TODO:
    Leaderboard using side scoreboard
*/

public class ParkourLeaderboard extends JavaPlugin implements CommandExecutor {
    private static Logger log;

    public ArrayList<Parkour> parkours;
    ParkourListener parkourListener;
    File parkourDir;
    boolean hologramsEnabled = true;

    FileConfiguration config;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        log = getLogger();

        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            log.info("Cannot find plugin HolographicDisplays, disabling holograms");
            hologramsEnabled = false;
        }

        parkours = new ArrayList<>();
        parkourDir = new File(getDataFolder().getAbsoluteFile(), "leaderboards");

        File datafolder = getDataFolder();
        if (!datafolder.exists()) {
            log.info(String.format("Data directory %s for plugin %s does not exist, creating new directory", datafolder.getName(), getDescription().getName()));
            if (!datafolder.mkdir()) {
                log.severe(String.format("Failed to create directory %s", datafolder.getName()));
            }
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        if (!parkourDir.exists()) {
            log.info(String.format("Data directory %s for plugin %s does not exist, creating new directory", parkourDir.getName(), getDescription().getName()));
            if (!parkourDir.mkdir()) {
                log.severe(String.format("Failed to create directory %s", parkourDir.getName()));
            }
        }

        config = getConfig();
        loadParkours();

        parkourListener = new ParkourListener(this);
        getServer().getPluginManager().registerEvents(parkourListener, this);
    }

    public void loadParkours() {
        for (String key : config.getConfigurationSection("parkours").getKeys(false)) {
            try {
                ConfigurationSection parkourConfig = config.getConfigurationSection("parkours").getConfigurationSection(key);
                String startWorld = parkourConfig.getString("start_world");
                String endWorld = parkourConfig.getString("end_world");

                World sworld = getServer().getWorld(startWorld);
                World eworld = getServer().getWorld(endWorld);

                String[] startPos = parkourConfig.getString("start_position").split(" *, *");
                String[] endPos = parkourConfig.getString("end_position").split(" *, *");

                Location start = new Location(
                        sworld,
                        Integer.parseInt(startPos[0]),
                        Integer.parseInt(startPos[1]),
                        Integer.parseInt(startPos[2])
                );
                Location end = new Location(
                        eworld,
                        Integer.parseInt(endPos[0]),
                        Integer.parseInt(endPos[1]),
                        Integer.parseInt(endPos[2])
                );
                String success = parkourConfig.getString("on_win");
                ArrayList<Location> checkpoints = new ArrayList<>();

                Parkour parkour = new Parkour(this, start, end, checkpoints, key, success);

                parkour.getStart().getBlock().setMetadata("parkour", new FixedMetadataValue(this, parkour));
                parkour.getEnd().getBlock().setMetadata("parkour", new FixedMetadataValue(this, parkour));

                List<String> stringCheckpoints = parkourConfig.getStringList("checkpoints");
                if (stringCheckpoints == null) {
                    stringCheckpoints = new ArrayList<>();
                }

                String startFormat = parkourConfig.getString("start_text");
                if (startFormat == null) {
                    startFormat = "%1$s\n%2$s Checkpoints";
                }


                if (hologramsEnabled) {
                    Hologram sHologram = HologramsAPI.createHologram(this, start.clone().add(0.5, 2, 0.5));
                    for (String line : String.format(startFormat, parkour.getName(), stringCheckpoints.size()).split("\n")) {
                        sHologram.appendTextLine(line);
                    }
                    parkour.getHolograms().add(sHologram);

                    String endFormat = parkourConfig.getString("end_text");
                    if (endFormat == null) {
                        endFormat = "%1$s: Finish";
                    }

                    Hologram eHologram = HologramsAPI.createHologram(this, end.clone().add(0.5, 2, 0.5));
                    for (String line : String.format(endFormat, parkour.getName(), stringCheckpoints.size()).split("\n")) {
                        eHologram.appendTextLine(line);
                    }
                    parkour.getHolograms().add(eHologram);
                }

                String cpFormat = parkourConfig.getString("checkpoint_text");
                if (cpFormat == null) {
                    cpFormat = "Checkpoint\n#%2$s";
                }

                int i = 1;
                for (String item : stringCheckpoints) {
                    String[] itemPos = item.split(" *, *");
                    String world;
                    if (itemPos.length == 4) {
                        world = itemPos[3];
                    } else {
                        world = sworld.getName();
                    }

                    Location itemLocation = new Location(
                            sworld = getServer().getWorld(world),
                            Integer.parseInt(itemPos[0]),
                            Integer.parseInt(itemPos[1]),
                            Integer.parseInt(itemPos[2])
                    );

                    checkpoints.add(itemLocation);
                    itemLocation.getBlock().setMetadata("parkour", new FixedMetadataValue(this, parkour));
                    if (hologramsEnabled) {
                        Hologram hologram = HologramsAPI.createHologram(this, itemLocation.clone().add(0.5, 2, 0.5));

                        for (String line : String.format(cpFormat, parkour.getName(), i, stringCheckpoints.size()).split("\n")) {
                            hologram.appendTextLine(line);
                        }
                        parkour.getHolograms().add(hologram);
                    }
                    i++;
                }

                parkours.add(parkour);
            } catch (Exception e) {
                log.severe("Failed to load parkour: " + key);
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("parkour")) {
            try {
                String name = args[0];
                if (args.length > 1) {
                    return false;
                }


                Parkour parkour = null;
                for (Parkour pk : parkours) {
                    if (pk.getName().equalsIgnoreCase(name)) {
                        parkour = pk;
                    }
                }
                String errorMessage = config.getString("name_error");
                if (errorMessage == null) {
                    errorMessage = ChatColor.RED + "[Parkour] That is not a valid parkour!";
                }

                if (parkour == null) {
                    sender.sendMessage(errorMessage);
                    return true;
                }

                String lbHeaderFormat = config.getString("leaderboard_header");
                if (lbHeaderFormat == null) {
                    lbHeaderFormat = "§l§9Leaderboard for %1$s";
                }

                String lbListItemFormat = config.getString("leaderboard_item");
                if (lbListItemFormat == null) {
                    lbListItemFormat = "§a%1$s. %2$s - %3$ss";
                }
                sender.sendMessage(String.format(lbHeaderFormat, parkour.getName()));
                for (int i = 0; i < Math.min(10, parkour.getLeaderboard().size()); i++) {
                    Pair<UUID, Float> data = parkour.getLeaderboard().get(i);
                    sender.sendMessage(String.format(lbListItemFormat, i + 1, getServer().getOfflinePlayer(data.getKey()).getName(), data.getValue()));
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        } else if (command.getName().equalsIgnoreCase("preload")) {
            for (Parkour parkour : parkours) {
                for (Hologram hologram : parkour.getHolograms()) {
                    hologram.delete();
                }

                for (Location checkpoint : parkour.getCheckpoints()) {
                    checkpoint.getBlock().removeMetadata("parkour", this);
                }

                parkour.getStart().getBlock().removeMetadata("parkour", this);
                parkour.getEnd().getBlock().removeMetadata("parkour", this);
            }
            parkours = new ArrayList<>();

            reloadConfig();
            config = getConfig();
            loadParkours();
            sender.sendMessage("Reloaded config");
            return true;
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> names = new ArrayList<>();
        for (Parkour parkour : parkours) {
            names.add(parkour.getName());
        }
        return names;
    }
}
