package own.inv.me.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import own.inv.me.Config;
import own.inv.me.inventory.Punish.PunishInventory;
import own.inv.me.utils.BansUtils;
import own.inv.me.utils.Pair;

import javax.xml.stream.events.Namespace;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AltsInventory implements Listener {

    HashMap<String, Pair<Long, String>> editing = new HashMap<>();

    public static ItemStack getAltItem(String pName, String lastSeen) {
        ItemStack item = Config.getItem(".Alts_Inventory.Alt", new Pair<>("%alt_name%", pName), new Pair<>("%last_seen%", Bukkit.getPlayer(pName) != null? "Online" : lastSeen));
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta mitem = (SkullMeta) item.getItemMeta();
            mitem.setOwningPlayer(Bukkit.getOfflinePlayer(BansUtils.getPlayerUUID(pName)));
            mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("playername"), PersistentDataType.STRING, pName);
            item.setItemMeta(mitem);
        }
        return item;
    }

    public static ItemStack getBackItem() {
        return Config.getItem(".Global.Back");
    }
    public static Inventory getInventory(Player p, String targetName) throws ExecutionException, InterruptedException {
        Inventory inv = Bukkit.createInventory(null, 54, "Alts: " + targetName);

        ArrayList<Pair<String, String>> altsList = BansUtils.getPlayerAlts(targetName).get();

        if (altsList.size() == 0) {
            return PunishmentInventory.getInventory(targetName);
        }

        for (Pair<String, String> alt : altsList) {
            inv.addItem(getAltItem(alt.getKey(), alt.getValue()));
        }
        inv.setItem(53, getBackItem());
        return inv;
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) throws ExecutionException, InterruptedException {
        String title = e.getView().getTitle();
        if (title.contains("Alts: ")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            String targetName = title.replaceAll("Alts: ", "");
            if (item == null || item.getType() == Material.AIR) return;

            if (item.equals(getBackItem())) {
                p.openInventory(PunishmentInventory.getInventory(targetName));
            } else {
                p.openInventory(PunishInventory.getInventory(item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.minecraft("playername"), PersistentDataType.STRING), true));
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
