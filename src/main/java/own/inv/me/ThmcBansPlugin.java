package own.inv.me;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import own.inv.me.commands.PunishmentCommand;
import own.inv.me.inventory.*;
import own.inv.me.inventory.History.ConfirmHistoryInventory;
import own.inv.me.inventory.History.HistoryInventory;
import own.inv.me.inventory.Punish.ConfirmInventory;
import own.inv.me.inventory.Punish.PunishInventory;

import java.io.IOException;

public class ThmcBansPlugin extends JavaPlugin implements Listener {

    public static ThmcBansPlugin instance;
    public static String CONSOLE_PREFIX = "[THMC BANS] ";
    public static String MSG_PREFIX = "&8| &bTHMC BANS &8| &7";

    public void onEnable() {
        instance = this;
        System.out.println(CONSOLE_PREFIX + "Plugin has been enabled!");
        registerEvents(this, new PunishmentInventory(), new HistoryInventory(), new PlayerInfoInventory(), new AltsInventory(), new PunishInventory(), new ConfirmInventory(), new ConfirmHistoryInventory());
        getCommand("punish").setExecutor(new PunishmentCommand());
        try {
            Config.initFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDisable() {
        System.out.println(CONSOLE_PREFIX + "Plugin has been disabled!");
    }

    public void registerEvents(Listener... events) {
        for (Listener event : events) {
            Bukkit.getServer().getPluginManager().registerEvents(event, this);
        }
    }
}