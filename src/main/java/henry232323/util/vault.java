package henry232323.util;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

public class vault {
    public static Economy setupEconomy(Server server) {
        if (server.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        Economy econ = rsp.getProvider();
        return econ;
    }

    public static Permission setupPermissions(Server server) {
        RegisteredServiceProvider<Permission> rsp = server.getServicesManager().getRegistration(Permission.class);
        Permission perms = rsp.getProvider();
        return perms;
    }

    public static Chat setupChat(Server server) {
        RegisteredServiceProvider<Chat> rsp = server.getServicesManager().getRegistration(Chat.class);
        Chat chat = rsp.getProvider();
        return chat;
    }
}
