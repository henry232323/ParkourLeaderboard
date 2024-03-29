package henry232323.parkourleaderboard;


import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
    private ArrayList<Hologram> holograms;

    public Parkour(ParkourLeaderboard plugin, Location start, Location end, ArrayList<Location> checkpoints, String name, String success) {
        this.start = start;
        this.end = end;

        this.plugin = plugin;
        this.name = name;
        this.checkpoints = checkpoints;

        current = new HashMap<>();
        holograms = new ArrayList<>();
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
            leaderboardConf.set("leaderboard", new HashMap<String, Float>());
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
            String item = cs.getString(key);
            leaderboard.add(new Pair<>(UUID.fromString(key), Float.parseFloat(item)));
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
        String startMessage = plugin.config.getConfigurationSection("parkours." + name)
                                           .getString("start_message");
        if (startMessage == null) {
            startMessage = ChatColor.BLUE + "[Parkour] Starting run.";
        }
        if (!startMessage.equals("")) {
            player.sendMessage(startMessage.split("\n"));
        }
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

        String endMessage = plugin.config.getConfigurationSection("parkours." + name).getString("end_message");
        if (endMessage == null) {
            endMessage = ChatColor.GREEN + "[Parkour] Your time was %1$ss.";
        }

        String endWithBest = plugin.config.getConfigurationSection("parkours." + name).getString("end_with_best");
        if (endWithBest == null) {
            endWithBest = ChatColor.GREEN + "[Parkour] Your time was %1$ss. Your best time is %2$ss.";
        }

        String endWithRecord = plugin.config.getConfigurationSection("parkours." + name).getString("end_with_record");
        if (endWithRecord == null) {
            endWithRecord = ChatColor.GREEN + "[Parkour] Your time was %1$ss. Your best time is %2$ss. New record!";
        }
        String message;
        if (besttime == ftime) {
            message = String.format(endWithRecord, ftime, besttime);
        } else if (besttime != -1) {
            message = String.format(endWithBest, ftime, besttime);
        } else {
            message = String.format(endMessage, ftime);
        }

        if (!message.equals("")) {
            player.sendMessage(message.split("\n"));
        }
        for (String part : successCommand.split("\n")) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), String.format(part, player.getName()));
        }

        if (leaderboard.get(0) == cscore) {
            String newRecordAnnounce = plugin.config.getConfigurationSection("parkours." + name).getString("new_record_announce");
            if (newRecordAnnounce == null) {
                newRecordAnnounce = ChatColor.DARK_BLUE + "[Parkour] %1$s §9has set a new record of %3$ss for parkour %2$s!";
            }

            if (!newRecordAnnounce.equals("")) {
                String nmessage = String.format(newRecordAnnounce, player.getName(), ftime, name);
                for (String s : nmessage.split("\n")) {
                    plugin.getServer().broadcastMessage(s);
                }
            }
        } else {
            String runCompleteAnnounce = plugin.config.getConfigurationSection("parkour." + name).getString("run_complete_announce");
            if (runCompleteAnnounce == null) {
                runCompleteAnnounce = "§1[Parkour] %1$s §9has has completed %3$s in %2$ss!";
            }

            if (!runCompleteAnnounce.equals("")) {
                String nmessage = String.format(runCompleteAnnounce, player.getName(), ftime, name);
                for (String s : nmessage.split("\n")) {
                    plugin.getServer().broadcastMessage(s);
                }
            }
        }

        String uuid = player.getUniqueId().toString();
        try {
            ConfigurationSection cs = leaderboardConf.getConfigurationSection("leaderboard");
            String time = cs.getString(uuid);
            if (time == null || ftime < Float.parseFloat(time)) {
                cs.set(uuid, ftime);
            }


            leaderboardConf.save(new File(plugin.getDataFolder().getAbsoluteFile(), "leaderboards" + File.separator + name + ".yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkPoint(Player player, Block block) {
        long etime = System.currentTimeMillis();
        long stime = current.get(player);

        float ftime = Float.parseFloat(new DecimalFormat("###.###").format(etime - stime)) / 1000;

        String checkpointMessage = plugin.config.getConfigurationSection("parkours." + name).getString("checkpoint_message");
        if (checkpointMessage == null) {
            checkpointMessage = "§9[Parkour] Checkpoint: %1$ss";
        }
        if (!checkpointMessage.equals("")) {
            player.sendMessage(String.format(checkpointMessage, ftime).split("\n"));
        }

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

    public ArrayList<Location> getCheckpoints() {
        return checkpoints;
    }

    public ArrayList<Hologram> getHolograms() {
        return holograms;
    }
}
