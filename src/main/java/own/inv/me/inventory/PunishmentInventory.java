package own.inv.me.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import own.inv.me.Config;
import own.inv.me.inventory.History.HistoryInventory;
import own.inv.me.inventory.Punish.PunishInventory;
import own.inv.me.utils.BansUtils;
import own.inv.me.utils.Pair;

import java.util.concurrent.ExecutionException;

public class PunishmentInventory implements Listener {
    public static ItemStack getHistoryItem(String targetName) {return Config.getItem("Main_Inventory.History", new Pair<>("%name%", targetName));}
    public static ItemStack getNewOffenceItem(String targetName) {return Config.getItem("Main_Inventory.New_Offence", new Pair<>("%name%", targetName));}
    public static ItemStack getPlayerInfo(String targetName) {
        ItemStack item = Config.getItem("Main_Inventory.Player_Info", new Pair<>("%name%", targetName));
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta mitem = (SkullMeta) item.getItemMeta();
            mitem.setOwningPlayer(Bukkit.getOfflinePlayer(targetName));
            item.setItemMeta(mitem);
        }
        return item;
    }
    public static ItemStack getAlts(String targetName) {return Config.getItem("Main_Inventory.Alts", new Pair<>("%name%", targetName));}

    public static Inventory getInventory(String targetName) {
        Inventory inv = Bukkit.createInventory(null, 27, "Punish: " + targetName);
        inv.setItem(10, getHistoryItem(targetName));
        inv.setItem(12, getNewOffenceItem(targetName));
        inv.setItem(14, getPlayerInfo(targetName));
        inv.setItem(16, getAlts(targetName));
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("Punish: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("Punish: ", "");
            if (item == null || item.getType() == Material.AIR) return;

            if (item.equals(getHistoryItem(targetName))) {
                Inventory inv = HistoryInventory.getInventory(p, targetName);
                if (inv == null) return;
                p.openInventory(inv);
            } else if (item.equals(getNewOffenceItem(targetName))) {
                p.openInventory(PunishInventory.getInventory(targetName, false));
            } else if (e.getSlot() == 14) {
                p.openInventory(PlayerInfoInventory.getInventory(targetName));
            } else if (item.equals(getAlts(targetName))) {
                p.openInventory(AltsInventory.getInventory(p, targetName));
            }
        }
    }
}