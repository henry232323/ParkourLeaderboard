package henry232323.parkourleaderboard;

import javafx.util.Pair;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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

    Extras:
    + Ability to add a Text hologram above starting pressure plate with name of parkour
    and other info
 */

public class ParkourLeaderboard extends JavaPlugin implements CommandExecutor {
    private static final Logger log = Logger.getLogger("Minecraft");

    public ArrayList<Parkour> parkours;
    ParkourListener parkourListener;
    File parkourDir;

    Economy econ;
    Permission perms;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        parkours = new ArrayList<>();
        parkourDir = new File(getDataFolder().getAbsoluteFile(), "leaderboards");

        if (!parkourDir.exists()) {
            log.info(String.format("Data directory %s for plugin %s does not exist, creating new directory", parkourDir.getName(), getDescription().getName()));
            if (!parkourDir.mkdir()) {
                log.severe(String.format("Failed to create directory %s", parkourDir.getName()));
            }
        }

        try {
            loadParkours();
        } catch (Exception e) {
            e.printStackTrace();
            getConfig().options().copyDefaults(true);
            saveConfig();
            loadParkours();
        }

        parkourListener = new ParkourListener(this);
        getServer().getPluginManager().registerEvents(parkourListener, this);
    }

    public void loadParkours() {
        for (String key : getConfig().getConfigurationSection("parkours").getKeys(false)) {
            ConfigurationSection parkourConfig = getConfig().getConfigurationSection("parkours").getConfigurationSection(key);
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

            for (String item : parkourConfig.getStringList("checkpoints")) {
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
            }

            parkours.add(parkour);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (command.getName().equalsIgnoreCase("parkour")) {
            try {
                String name = args[0];
                if (args.length > 1) {
                    return false;
                }


                Parkour parkour = null;
                for (Parkour pk : parkours) {
                    if (pk.getName().equals(name)) {
                        parkour = pk;
                    }
                }
                if (parkour == null) {
                    sender.sendMessage(ChatColor.RED + "[Parkour] That is not a valid parkour!");
                    return true;
                }

                String leaderBoard = "";
                sender.sendMessage(String.format("%s%sLeaderboard for %s", ChatColor.BOLD, ChatColor.BLUE, name, leaderBoard));
                for (int i = 0; i < 10; i++) {
                    Pair<UUID, Float> data = parkour.getLeaderboard().get(i);
                    sender.sendMessage(String.format("%s%s. %s - %ss", ChatColor.GREEN, i+1, getServer().getOfflinePlayer(data.getKey()).getName(), data.getValue()));
                }
                return true;
            } catch (Exception e) {
                return false;
            }
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
