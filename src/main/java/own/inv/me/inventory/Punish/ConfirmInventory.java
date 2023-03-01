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
import own.inv.me.utils.BansUtils;

import java.util.concurrent.ExecutionException;

public class ConfirmInventory implements Listener {
    public static ItemStack getSilentItem() {
        return Config.getItem("Confirm_Inventory.Silent");
    }
    public static ItemStack getPublicItem() {
        return Config.getItem("Confirm_Inventory.Public");
    }
    public static ItemStack getCancelItem(String punishmentName) {
        ItemStack item = Config.getItem("Confirm_Inventory.Cancel");
        ItemMeta mitem = item.getItemMeta();
        PersistentDataContainer citem = mitem.getPersistentDataContainer();
        citem.set(NamespacedKey.minecraft("name"), PersistentDataType.STRING, punishmentName);
        item.setItemMeta(mitem);
        return item;
    }

    public static Inventory getInventory(String targetName, String punishmentName) {
        Inventory inv = Bukkit.createInventory(null, 27, "Confirm: " + targetName);
        inv.setItem(11, getSilentItem());
        inv.setItem(13, getCancelItem(punishmentName));
        inv.setItem(15, getPublicItem());
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("Confirm: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("Confirm: ", "");
            if (item == null || item.getType() == Material.AIR) return;

            int slot = e.getSlot();

            Inventory inv = e.getInventory();
            ItemStack closeItem = inv.getItem(13);
            ItemMeta mcloseItem = closeItem.getItemMeta();
            PersistentDataContainer ccloseItem = mcloseItem.getPersistentDataContainer();
            String punishmentName = ccloseItem.get(NamespacedKey.minecraft("name"), PersistentDataType.STRING);
            if (item.equals(getSilentItem())) {
                Bukkit.dispatchCommand(p, BansUtils.getPunishPlayerCommand(targetName, punishmentName, true));
                p.closeInventory();
            } else  if (item.equals(getPublicItem())) {
                Bukkit.dispatchCommand(p, BansUtils.getPunishPlayerCommand(targetName, punishmentName, false));
                p.closeInventory();
            } else  if (slot == 13) {
                p.openInventory(PunishInventory.getInventory(targetName, false));
            }
        }
    }
}
