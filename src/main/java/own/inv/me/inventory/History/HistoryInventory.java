package own.inv.me.inventory.History;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import own.inv.me.Config;
import own.inv.me.inventory.PunishmentInventory;
import own.inv.me.utils.BansUtils;
import own.inv.me.utils.Pair;
import own.inv.me.utils.TimeConverter;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HistoryInventory implements Listener {

    public static HashMap<String, Pair<Long, String>> editing = new HashMap<>();
    public static ItemStack getPunishmentHistoryItem(BansUtils.Punishment punishment) {
        String punishmentTypeCMD;
        if (punishment.punishmentType == BansUtils.PunishmentType.IPBAN) {
            punishmentTypeCMD = "IPBan";
        } else if (punishment.punishmentType == BansUtils.PunishmentType.BAN) {
            punishmentTypeCMD = "Ban";
        } else if (punishment.punishmentType == BansUtils.PunishmentType.MUTE) {
            punishmentTypeCMD = "Mute";
        } else {
            punishmentTypeCMD = "Warn";
        }

        return Config.getItem(".History_Inventory." + punishmentTypeCMD + "_History." + (punishment.active? "Active" : "Finished"), new Pair<>("%reason%", punishment.reason),
                new Pair<>("%staff%", Bukkit.getOfflinePlayer(UUID.fromString(punishment.bannedBy)).getName()),
                new Pair<>("%issue_date%", TimeConverter.formatDate(punishment.time)),
                new Pair<>("%expire_date%", TimeConverter.formatDate(punishment.until)),
                new Pair<>("%active%", punishment.active? "Yes" : "No"),
                new Pair<>("%remaining%", punishment.until != 0? TimeConverter.convert((punishment.until - System.currentTimeMillis()) /1000) : "Permenant"));
    }

    public static ItemStack getBackItem() {
        return Config.getItem(".Global.Back");
    }
    public static Inventory getInventory(Player p, String targetName) throws ExecutionException, InterruptedException {
        Inventory inv = Bukkit.createInventory(null, 54, "History: " + targetName);
        ArrayList<BansUtils.Punishment> bansList = BansUtils.getPlayerHistory(BansUtils.getPlayerUUID(targetName).toString(), BansUtils.PunishmentType.BAN).get();
        ArrayList<BansUtils.Punishment> mutesList = BansUtils.getPlayerHistory(BansUtils.getPlayerUUID(targetName).toString(), BansUtils.PunishmentType.MUTE).get();
        ArrayList<BansUtils.Punishment> warnsList = BansUtils.getPlayerHistory(BansUtils.getPlayerUUID(targetName).toString(), BansUtils.PunishmentType.WARN).get();

        ArrayList<BansUtils.Punishment> punishmentList = new ArrayList<>();
        punishmentList.addAll(bansList);
        punishmentList.addAll(mutesList);
        punishmentList.addAll(warnsList);

        if (punishmentList.size() == 0) {
            Config.sendMessage(p, Config.getNoHistoryMessage(targetName));
            p.closeInventory();
            return null;
        }

        for (BansUtils.Punishment punishment : punishmentList) {
            ItemStack item = getPunishmentHistoryItem(punishment);
            ItemMeta mitem = item.getItemMeta();
            mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("id"), PersistentDataType.LONG, punishment.id);
            mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("type"), PersistentDataType.STRING, punishment.punishmentType.toString());
            item.setItemMeta(mitem);
            inv.addItem(item);
        }
        inv.setItem(53, getBackItem());
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("History: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("History: ", "");

            if (item.equals(getBackItem())) {
                p.openInventory(PunishmentInventory.getInventory(targetName));
            } else {
//                ClickType clickType = e.getClick();
                PersistentDataContainer citem = item.getItemMeta().getPersistentDataContainer();
                long id = citem.get(NamespacedKey.minecraft("id"), PersistentDataType.LONG);
                String punishmentType = citem.get(NamespacedKey.minecraft("type"), PersistentDataType.STRING);
                if (item == null || item.getType() == Material.AIR) return;

                p.openInventory(ConfirmHistoryInventory.getInventory(targetName, id, punishmentType));

//                if (clickType == ClickType.LEFT) {
//                    editing.put(p.getName(), new Pair<>(id, punishmentType));
//                    p.closeInventory();
//                    Config.sendMessage(p, Config.getNewPunishmentMessage());
//                } else {
//                    BansUtils.deletePunishment(id, punishmentType).get();
//                    Config.sendMessage(p, Config.getDeletePunishmentSuccessMessage());
//                    p.openInventory(getInventory(p, targetName));
//                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) throws ExecutionException, InterruptedException {
        Player p = e.getPlayer();
        if (editing.containsKey(p.getName())) {
            e.setCancelled(true);
            Pair<Long, String> pdata = editing.get(p.getName());
            String newReason = e.getMessage();
            BansUtils.updatePunishment(pdata.getKey(), pdata.getValue(), newReason).get();
            editing.remove(p.getName());
            Config.sendMessage(p, Config.getUpdatePunishmentSuccessMessage());
        }
    }
}
