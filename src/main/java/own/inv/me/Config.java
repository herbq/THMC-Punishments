package own.inv.me;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import own.inv.me.utils.Pair;

import javax.xml.stream.events.Namespace;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class Config {
    public static File lang, items, punishments;
    public static FileConfiguration langConfig, itemsConfig, punishmentsConfig;

    public static void initFiles() throws IOException {
        ThmcBansPlugin.instance.saveResource("lang.yml", false);
        ThmcBansPlugin.instance.saveResource("items.yml", false);
        ThmcBansPlugin.instance.saveResource("punishments.yml", false);


        lang = new File(ThmcBansPlugin.instance.getDataFolder(), "lang.yml");
        langConfig = YamlConfiguration.loadConfiguration(lang);

        items = new File(ThmcBansPlugin.instance.getDataFolder(), "items.yml");
        itemsConfig = YamlConfiguration.loadConfiguration(items);

        punishments = new File(ThmcBansPlugin.instance.getDataFolder(), "punishments.yml");
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishments);

        saveLang();
        saveItems();
        savePunishments();
    }

    public static void saveLang() throws IOException {
        langConfig.save(lang);
    }

    public static void saveItems() throws IOException {
        itemsConfig.save(items);
    }

    public static void savePunishments() throws IOException {
        punishmentsConfig.save(punishments);
    }

    public static void sendMessage(Player p, String msg) {
        if (msg == null || msg.equalsIgnoreCase("")) return;
        p.sendMessage(msg);
    }

    public static String replaceText(String str, Pair<String, String>... replaces) {
        str = str.replaceAll("&", "§");
        for (Pair<String, String> pair : replaces) {
            str = str.replaceAll(pair.getKey(), pair.getValue());
        }
        return str;
    }

    public static String getText(FileConfiguration cfg, String path) {
        return (cfg.contains(path)? cfg.getString(path).replaceAll("&", "§").replaceAll("%prefix%", getPrefix()) : null);
    }

    public static String getText(FileConfiguration cfg, String path, Pair<String, String>... replaces) {
        if (cfg.contains(path)) {
            return replaceText(getText(cfg, path), replaces);
        } else {
            return null;
        }
    }

    public static ArrayList<String> getList(FileConfiguration cfg, String path) {
        ArrayList<String> arr = new ArrayList<String>();
        for (String str : cfg.getStringList(path)) {
            arr.add(replaceText(str, new Pair<String, String>("&", "§")));
        }
        return arr;
    }

    public static ArrayList<String> getList(FileConfiguration cfg, String path, Pair<String, String>... replaces) {
        ArrayList<String> arr = new ArrayList<String>();
        for (String str : cfg.getStringList(path)) {
            arr.add(replaceText(str, replaces));
        }
        return arr;
    }

    public static String getPrefix() {
        return langConfig.contains("Messages.prefix")? langConfig.getString("Messages.prefix").replaceAll("&", "§") : null;
    }

    public static ItemStack getItem(String path, Pair<String, String>... replaces) {
        path = "Items." + path;

        String itemName = getText(itemsConfig, path + ".Name", replaces);
        ArrayList<String> itemLore = getList(itemsConfig, path + ".Lore", replaces);
        Material itemMaterial = Material.getMaterial(getText(itemsConfig, path + ".Material"));

        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta mitem = item.getItemMeta();
        mitem.setDisplayName(itemName);
        mitem.setLore(itemLore);
        item.setItemMeta(mitem);

        return item;
    }

    public static ItemStack getPunishmentItem(String path) {
        String itemName = getText(punishmentsConfig, path + ".Name");
        ArrayList<String> itemLore = getList(punishmentsConfig, path + ".Lore");
        Material itemMaterial = Material.getMaterial(getText(punishmentsConfig, path + ".Item"));

        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta mitem = item.getItemMeta();
        mitem.setDisplayName(itemName);
        mitem.setLore(itemLore);
        mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("name"), PersistentDataType.STRING, path);
//        if (punishmentsConfig.contains(path + ".Degrees")) {
//            for (int i = 0; i < 3; i++) {
//                while (punishmentsConfig.contains(path + ".Degrees." + i)) {
//                    mitem.getPersistentDataContainer().set(NamespacedKey.minecraft("d" + i), PersistentDataType.STRING, getText(punishmentsConfig, path + ".Degrees." + i));
//                }
//            }
//        }
        item.setItemMeta(mitem);

        return item;
    }

    public static String getPunishmentType(String punishmentName) {
        return getText(punishmentsConfig, punishmentName + ".Type");
    }

    public static String getPunishmentReason(String punishmentName) {
        return getText(punishmentsConfig, punishmentName + ".Name");
    }

    public static String getPunishmentDuration(String punishmentName, int punishmentCount) {
        String duration = getText(punishmentsConfig, punishmentName + ".Degrees.1");
        for (int i = 2; i <= punishmentCount + 1; i++) {
            if (punishmentsConfig.contains(punishmentName + ".Degrees." + i)) {
                duration = getText(punishmentsConfig, punishmentName + ".Degrees." + i);
            }
        }
        return duration;
    }

    public static Set<String> getPunishmentKeys() {
        return punishmentsConfig.getKeys(false);
    }

    public static String getNewPunishmentMessage() {
        return getText(langConfig, "Messages.new_punishment");
    }

    public static String getDeletePunishmentSuccessMessage() {
        return getText(langConfig, "Messages.delete_success");
    }

    public static String getUpdatePunishmentSuccessMessage() {
        return getText(langConfig, "Messages.update_success");
    }
    public static String getNoHistoryMessage(String targetName) {return getText(langConfig, "Messages.no_history", new Pair<>("%player%", targetName));}
    public static String getNoStatusMessage(String targetName) {return getText(langConfig, "Messages.no_status", new Pair<>("%player%", targetName));}
    public static String getCommandMessage() {return getText(langConfig, "Messages.command");}
    public static String getNoPermissionMessage() {return getText(langConfig, "Messages.no_permission");}
}
