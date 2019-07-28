package henry232323.parkourleaderboard;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.List;

class ParkourListener implements Listener {
    private ParkourLeaderboard plugin;
    HashMap<Player, Pair<String, Float>> pending;
    HashMap<Player, Location> pendingEnd;

    static MaterialSetTag pressurePlates = new MaterialSetTag().endsWith("_PRESSURE_PLATE");

    ParkourListener(ParkourLeaderboard plugin) {
        this.plugin = plugin;
        pending = new HashMap<>();
        pendingEnd = new HashMap<>();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (pending.containsKey(event.getPlayer())) {
            pending.remove(event.getPlayer());
        }
        if (pendingEnd.containsKey(event.getPlayer())) {
            pendingEnd.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPressureStep(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction().equals(Action.PHYSICAL)) {
            if (pressurePlates.isTagged(event.getClickedBlock())) {
                Block block = event.getClickedBlock();
                if (block.hasMetadata("parkour")) {
                    List<MetadataValue> mdv = block.getMetadata("parkour");
                    for (MetadataValue val : mdv) {
                        if (val.getOwningPlugin() == plugin) {
                            Parkour parkour = (Parkour) val.value();
                            if (block.getLocation().equals(parkour.getStart())) {
                                parkour.start(event.getPlayer());
                            } else if (block.getLocation().equals(parkour.getEnd())) {
                                if (parkour.currentlyRunning(event.getPlayer()))
                                    parkour.end(event.getPlayer());
                            } else if (parkour.getCheckpoints().contains(block.getLocation())) {
                                if (parkour.currentlyRunning(event.getPlayer())) {
                                    parkour.checkPoint(event.getPlayer(), block);
                                }
                            }
                        }
                    }
                }
            }
        }

        /*
        else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock() == null) {
                return;
            }
            if (pendingEnd.containsKey(event.getPlayer())) {
                if (pressurePlates.isTagged(event.getClickedBlock())) {
                    Pair<String, Float> data = pending.remove(event.getPlayer());
                    String name = data.getKey();
                    String success = data.getValue();
                    Location start = pendingEnd.remove(event.getPlayer());
                    Parkour parkour = new Parkour(start, event.getClickedBlock().getLocation(), name, success);
                    parkour.setPlugin(plugin);
                    plugin.parkours.add(parkour);

                    event.getPlayer().sendMessage(ChatColor.BLUE + String.format("New parkour created with name %s.", name));
                }
            } else if (pending.containsKey(event.getPlayer())) {
                if (pressurePlates.isTagged(event.getClickedBlock())) {
                    pendingEnd.put(event.getPlayer(), event.getClickedBlock().getLocation());
                    event.getPlayer().sendMessage(ChatColor.BLUE + "Right click a pressure plate to register it as the end of the parkour.");
                }
            }
        }
         */
    }
}
