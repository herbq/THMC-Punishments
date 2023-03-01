package own.inv.me.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import own.inv.me.Config;
import own.inv.me.utils.BansUtils;
import own.inv.me.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class PlayerInfoInventory implements Listener {
    public static ItemStack getGodPermsItem(String targetName) {
        Player p = Bukkit.getPlayer(BansUtils.getPlayerUUID(targetName));
        return Config.getItem("Player_Info_Inventory.God_Perms", new Pair<>("%god_perms%", (p != null? (p.hasPermission("essentials.god")? "Yes" : "No") : "-") ));
    }
    public static ItemStack getFlyPermsItem(String targetName) {
        Player p = Bukkit.getPlayer(BansUtils.getPlayerUUID(targetName));
        return Config.getItem("Player_Info_Inventory.Fly_Perms", new Pair<>("%fly_perms%", (p != null? (p.hasPermission("thmc.fly")? "Yes" : "No") : "-") ));
    }
    public static ItemStack getUsernameItem(String targetName) {
        ItemStack item = Config.getItem("Player_Info_Inventory.Username", new Pair<>("%username%", targetName));
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta mitem = (SkullMeta) item.getItemMeta();
            mitem.setOwningPlayer(Bukkit.getOfflinePlayer(BansUtils.getPlayerUUID(targetName)));
            item.setItemMeta(mitem);
        }
        return item;
    }
    public static ItemStack getIsStaffItem(String targetName) {
        Player p = Bukkit.getPlayer(BansUtils.getPlayerUUID(targetName));
        return Config.getItem("Player_Info_Inventory.Staff", new Pair<>("%is_staff%", (p != null? (p.hasPermission("thmc.staff")? "Yes" : "No") : "-") ));
    }
    public static ItemStack getIsOPItem(String targetName) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(BansUtils.getPlayerUUID(targetName));
        return Config.getItem("Player_Info_Inventory.OP", new Pair<>("%is_op%", p.isOp()? "Yes" : "No"));
    }

    public static ItemStack getBackItem() {
        return Config.getItem(".Global.Back");
    }

    public static Inventory getInventory(String targetName) {
        Inventory inv = Bukkit.createInventory(null, 27, "Player Info: " + targetName);
        inv.setItem(10, getGodPermsItem(targetName));
        inv.setItem(11, getFlyPermsItem(targetName));
        inv.setItem(13, getUsernameItem(targetName));
        inv.setItem(15, getIsStaffItem(targetName));
        inv.setItem(16, getIsOPItem(targetName));

        inv.setItem(26, getBackItem());
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("Player Info: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("Player Info: ", "");
            if (item == null || item.getType() == Material.AIR) return;

            if (item.equals(getBackItem())) {
                p.openInventory(PunishmentInventory.getInventory(targetName));
            }
        }
    }
}
