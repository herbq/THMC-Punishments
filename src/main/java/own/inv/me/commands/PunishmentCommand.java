package own.inv.me.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import own.inv.me.Config;
import own.inv.me.inventory.PunishmentInventory;

public class PunishmentCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("punish")) {
            if (p.hasPermission("THMC.punish")) {
                if (args.length == 0) {
                    Config.sendMessage(p, Config.getCommandMessage());
                } else {
                    String targetName = args[0];
                    p.openInventory(PunishmentInventory.getInventory(targetName));
                }
            } else {
                Config.sendMessage(p, Config.getNoPermissionMessage());
            }
        }
        return true;
    }
}
