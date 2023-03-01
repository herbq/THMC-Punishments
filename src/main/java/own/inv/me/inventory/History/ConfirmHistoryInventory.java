package own.inv.me.inventory.History;

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
import own.inv.me.utils.Pair;

import java.util.concurrent.ExecutionException;

public class ConfirmHistoryInventory implements Listener {
    public static ItemStack getDeleteItem() {return Config.getItem("Confirm_History_Inventory.Delete");}
    public static ItemStack getEditItem() {return Config.getItem("Confirm_History_Inventory.Edit");}
    public static ItemStack getLiftItem() {return Config.getItem("Confirm_History_Inventory.Lift");}
    public static ItemStack getBackItem(long id, String punishmentType) {
        ItemStack item = Config.getItem(".Global.Back");
        ItemMeta mitem = item.getItemMeta();
        mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("id"), PersistentDataType.LONG, id);
        mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("type"), PersistentDataType.STRING, punishmentType);
        item.setItemMeta(mitem);
        return item;
    }

    public static Inventory getInventory(String targetName, long id, String punishmentType) {
        Inventory inv = Bukkit.createInventory(null, 27, "Action: " + targetName);
        inv.setItem(11, getDeleteItem());
        inv.setItem(13, getEditItem());
        inv.setItem(15, getLiftItem());
        inv.setItem(26, getBackItem(id, punishmentType));
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("Action: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("Action: ", "");
            if (item == null || item.getType() == Material.AIR) return;

            if (e.getSlot() == 26) { // Back item
                p.openInventory(HistoryInventory.getInventory(p, targetName));
                return;
            }

            Inventory inv = e.getInventory();
            ItemStack backItem = inv.getItem(26);
            ItemMeta mbackItem = backItem.getItemMeta();
            PersistentDataContainer pbackItem = mbackItem.getPersistentDataContainer();
            long id = pbackItem.get(NamespacedKey.minecraft("id"), PersistentDataType.LONG);
            String punishmentType = pbackItem.get(NamespacedKey.minecraft("type"), PersistentDataType.STRING);

            if (item.equals(getDeleteItem())) {
                BansUtils.deletePunishment(id, punishmentType).get();
                Config.sendMessage(p, Config.getDeletePunishmentSuccessMessage());
                p.closeInventory();
            } else if (item.equals(getEditItem())) {
                HistoryInventory.editing.put(p.getName(), new Pair<>(id, punishmentType));
                p.closeInventory();
                Config.sendMessage(p, Config.getNewPunishmentMessage());
            } else if (item.equals(getLiftItem())) {
                Bukkit.dispatchCommand(p, "un" + punishmentType + " " + targetName);
                p.closeInventory();
            }
        }
    }
}
