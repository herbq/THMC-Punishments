package own.inv.me.inventory.Punish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import own.inv.me.Config;
import own.inv.me.inventory.AltsInventory;
import own.inv.me.inventory.PunishmentInventory;

import java.util.concurrent.ExecutionException;

public class PunishInventory implements Listener {
    public static ItemStack getPunishmentItem(String path) {return Config.getPunishmentItem(path);}

    public static ItemStack getBackItem(boolean backAlts) {
        ItemStack item = Config.getItem(".Global.Back");
        ItemMeta mitem = item.getItemMeta();
        PersistentDataContainer citem = mitem.getPersistentDataContainer();
        citem.set(NamespacedKey.minecraft("alts"), PersistentDataType.INTEGER, backAlts? 1 : 0);
        item.setItemMeta(mitem);
        return item;
    }

    public static Inventory getInventory(String targetName, boolean backAlts) {
        Inventory inv = Bukkit.createInventory(null, 54, "Punishment: " + targetName);
        Config.getPunishmentKeys().forEach(key -> {
            inv.addItem(getPunishmentItem(key));
        });

        inv.setItem(53, getBackItem(backAlts));
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("Punishment: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("Punishment: ", "");
            if (item == null || item.getType() == Material.AIR) return;

            if (item.equals(getBackItem(false))) {
                p.openInventory(PunishmentInventory.getInventory(targetName));
            } else if (item.equals(getBackItem(true))) {
                p.openInventory(AltsInventory.getInventory(p, targetName));
            } else {
                PersistentDataContainer citem = item.getItemMeta().getPersistentDataContainer();
                String punishmentName = citem.get(NamespacedKey.minecraft("name"), PersistentDataType.STRING);
                p.openInventory(ConfirmInventory.getInventory(targetName, punishmentName));
            }
        }
    }
}
