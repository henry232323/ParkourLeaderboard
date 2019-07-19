package henry232323.parkourleaderboard;


import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;

public class Parkour implements Serializable {
    transient private ParkourLeaderboard plugin;
    transient private HashMap<Player, Long> current;
    transient private YamlConfiguration leaderboardConf;

    private String successCommand;
    private String name;
    private Location start, end;

    private ArrayList<Pair<UUID, Float>> leaderboard;
    private ArrayList<Location> checkpoints;

    public Parkour(ParkourLeaderboard plugin, Location start, Location end, ArrayList<Location> checkpoints, String name, String success) {
        this.start = start;
        this.end = end;

        this.plugin = plugin;
        this.name = name;
        this.checkpoints = checkpoints;

        current = new HashMap<>();
        successCommand = success;

        leaderboard = new ArrayList<>();
        File parkourFile = new File(plugin.parkourDir,name + ".yml");

        if (!parkourFile.exists()) {
            try {
                if (!parkourFile.createNewFile()) {
                    System.out.println("Thats not good");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        leaderboardConf = YamlConfiguration.loadConfiguration(
                parkourFile
        );
        if (!leaderboardConf.getKeys(false).contains("leaderboard")) {
            leaderboardConf.set("leaderboard", new HashMap<String, ArrayList<String>>());
        }
        try {
            leaderboardConf.save(parkourFile);
            parkourFile = new File(plugin.parkourDir,name + ".yml");
            leaderboardConf = YamlConfiguration.loadConfiguration(
                    parkourFile
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConfigurationSection cs = leaderboardConf.getConfigurationSection("leaderboard");
        for (String key : cs.getKeys(false)) {
            for (String item : cs.getStringList(key)) {
                leaderboard.add(new Pair<>(UUID.fromString(key), Float.valueOf(item)));
            }
        }
    }

    public Location getStart() {
        return start;
    }

    public Location getEnd() {
        return end;
    }

    public void start(Player player) {
        long stime = System.currentTimeMillis();
        current.put(player, stime);
        player.sendMessage(ChatColor.BLUE + "[Parkour] Starting run.");
    }

    public void end(Player player) {
        long etime = System.currentTimeMillis();
        long stime = current.remove(player);

        float ftime = Float.parseFloat(new DecimalFormat("###.###").format(etime - stime)) / 1000;
        Pair<UUID, Float> cscore = new Pair<>(player.getUniqueId(), ftime);

        leaderboard.add(cscore);
        leaderboard.sort(Comparator.comparing(Pair::getValue));

        float besttime = -1;
        for (Pair<UUID, Float> score : leaderboard) {
            if (score.getKey().equals(player.getUniqueId())) {
                if (besttime == -1) {
                    besttime = score.getValue();
                } else if (score.getValue() < besttime) {
                    besttime = score.getValue();
                }
            }
        }

        String message = String.format(ChatColor.GREEN + "[Parkour] Your time was %ss. ", ftime);
        if (besttime != -1) {
            message += String.format("Your best time is %ss. ", besttime);
        }
        if (besttime == ftime) {
            message += "New record!";
        }

        player.sendMessage(message);
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), String.format(successCommand, player.getName()));

        if (leaderboard.get(0) == cscore) {
            plugin.getServer().broadcastMessage(String.format("%s[Parkour] %s %shas set a new record of %ss for parkour %s!", ChatColor.DARK_BLUE, player.getDisplayName(), ChatColor.BLUE, ftime, name));
        } else {
            plugin.getServer().broadcastMessage(String.format("%s[Parkour] %s %shas has completed %s in %ss!", ChatColor.DARK_BLUE, player.getDisplayName(), ChatColor.BLUE, name, ftime));
        }

        String uuid = player.getUniqueId().toString();
        try {
            ConfigurationSection cs = leaderboardConf.getConfigurationSection("leaderboard");
            if (!cs.getKeys(false).contains(uuid)) {
                cs.set(uuid, new ArrayList<String>());
            }
            List<String> times = cs.getStringList(uuid);
            times.add(String.valueOf(ftime));
            cs.set(uuid, times);

            leaderboardConf.save(new File(plugin.getDataFolder().getAbsoluteFile(), "leaderboards" + File.separator + name + ".yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void checkPoint(Player player, Block block) {
        long etime = System.currentTimeMillis();
        long stime = current.get(player);

        float ftime = Float.parseFloat(new DecimalFormat("###.###").format(etime - stime)) / 1000;
        player.sendMessage(String.format("%s[Parkour] Checkpoint: %ss", ChatColor.BLUE, ftime));

    }

    public String getName() {
        return name;
    }

    public ArrayList<Pair<UUID, Float>> getLeaderboard() {
        return leaderboard;
    }

    public String getSuccessCommand() {
        return successCommand;
    }

    public boolean currentlyRunning(Player player) {
        return current.containsKey(player);
    }

    public String toString() {
        return String.format("Parkour(name=%s, command=%s, start=%s,%s,%s, end=%s,%s,%s)", name, successCommand);
    }

    public ArrayList<Location> getCheckpoints() {
        return checkpoints;
    }
}
