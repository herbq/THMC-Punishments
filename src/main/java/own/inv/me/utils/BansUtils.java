package own.inv.me.utils;

import litebans.api.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import own.inv.me.Config;
import own.inv.me.ThmcBansPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BansUtils {

    public static enum PunishmentType {
        BAN,
        MUTE,
        KICK,
        WARN,
        IPBAN,
    }

    public static class Punishment {
        public long id;
        public String uuid;
        public String ip;
        public String reason;
        public String bannedBy;
        public String removedBy;
        public long time;
        public long until;
        public boolean silent;
        public boolean active;
        public PunishmentType punishmentType;
        public Punishment(long id, String uuid, String ip, String reason, String bannedBy, String removedBy, long until, long time, boolean silent, boolean active, PunishmentType punishmentType) {
            this.id = id;
            this.uuid = uuid;
            this.ip = ip;
            this.reason = reason;
            this.bannedBy = bannedBy;
            this.removedBy = removedBy;
            this.until = until;
            this.time = time;
            this.silent = silent;
            this.active = active;
            this.punishmentType = punishmentType;
        }
    }

    public static String getPlayerUUID(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (playerName.equalsIgnoreCase("console")) return playerName;
        UUID uuid = offlinePlayer.getUniqueId();
        return uuid.toString();
    }

    public static String getPunishPlayerCommand(String targetName, String punishmentName, boolean silent) throws ExecutionException, InterruptedException {
        String punishmentType = Config.getPunishmentType(punishmentName);
        punishmentType = punishmentType.toLowerCase().replaceAll("ip", "");
        String punishmentReason = Config.getPunishmentReason(punishmentName);
        Integer punishmentCount = getPunishmentCount(targetName, punishmentReason, punishmentType).get();
        return (punishmentType + (silent? " -s " : " ") + targetName + " " + Config.getPunishmentDuration(punishmentName, punishmentCount) + " " + punishmentReason);
    }

    public static Future<Integer> getPunishmentCount(String targetName, String punishmentReason, String punishmentType) {
        punishmentType = punishmentType.replaceFirst("ip", "");
        if (punishmentType.equalsIgnoreCase("warn")) {
            punishmentType = "warning";
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String finalPunishmentType = punishmentType;
        return executor.submit(() -> {
            int counter = 0;
            String query = "SELECT * FROM {" + finalPunishmentType.toLowerCase() + "s} WHERE reason=? AND uuid='" + BansUtils.getPlayerUUID(targetName) + "'";
            try (PreparedStatement st = Database.get().prepareStatement(query)) {
                st.setString(1, punishmentReason);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        counter++;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return counter;
        });
    }

    public static Future<ArrayList<Punishment>> getPlayerHistory(String playerUUID, PunishmentType punishmentType) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> {
            ArrayList<Punishment> punishmentList = new ArrayList<Punishment>();
            String query = "SELECT * FROM " + (punishmentType == PunishmentType.BAN? "{bans}" : (punishmentType == PunishmentType.MUTE? "{mutes}" : "{warnings}")) + " WHERE uuid=?";
            try (PreparedStatement st = Database.get().prepareStatement(query)) {
                st.setString(1, playerUUID);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id");
                        String uuid = rs.getString("uuid");
                        String ip = rs.getString("ip");
                        String reason = rs.getString("reason");
                        String bannedBy = rs.getString("banned_by_uuid");
                        String removedBy = rs.getString("removed_by_uuid");
                        long until = rs.getLong("until");
                        long time = rs.getLong("time");
                        boolean silent = rs.getBoolean("silent");
                        boolean active = rs.getBoolean("active");
                        punishmentList.add(new Punishment(id, uuid, ip, reason, bannedBy, removedBy, until, time, silent, active, punishmentType));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return punishmentList;
        });
    }

    public static Future<Boolean> deletePunishment(long id, String punishmentType) {
        punishmentType = punishmentType.replaceFirst("ip", "");
        if (punishmentType.toLowerCase().equals("warn")) {
            punishmentType = "warning";
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String finalPunishmentType = punishmentType;
        return executor.submit(() -> {
            String query = "DELETE FROM {" + finalPunishmentType.toLowerCase() + "s} WHERE id=?";
            try (PreparedStatement st = Database.get().prepareStatement(query)) {
                st.setString(1, Long.toString(id));
                int rowsAffected = st.executeUpdate();
                return true;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public static Future<Boolean> updatePunishment(long id, String punishmentType, String reason) {
        punishmentType = punishmentType.replaceFirst("ip", "");
        if (punishmentType.toLowerCase().equals("warn")) {
            punishmentType = "warning";
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String finalPunishmentType = punishmentType;
        return executor.submit(() -> {
            String query = "UPDATE {" + finalPunishmentType.toLowerCase() + "s} SET reason = '" + reason.replaceAll("\\[", "").replaceAll("\\]","").replaceAll("event cancelled by LiteBans", "") + "' WHERE id=?";
            try (PreparedStatement st = Database.get().prepareStatement(query)) {
                st.setString(1, Long.toString(id));
                int rowsAffected = st.executeUpdate();
                return true;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public static Future<String> getPlayerIP(String targetName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> {
            String query = "SELECT ip FROM {history} WHERE name=?";
            try (PreparedStatement st = Database.get().prepareStatement(query)) {
                st.setString(1, targetName);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        String ip = rs.getString("ip");
                        return ip;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return "#";
        });
    }

    public static Future<ArrayList<Pair<String, String>>> getPlayerAlts(String targetName) throws ExecutionException, InterruptedException {
        String ip = getPlayerIP(targetName).get();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> {
            ArrayList<Pair<String, String>> data = new ArrayList<>();
            if (ip.equals("#")) {
                data.add(new Pair<>(targetName, "-"));
                return data;
            }
            String query = "SELECT name, date FROM {history} WHERE ip=?";
            try (PreparedStatement st = Database.get().prepareStatement(query)) {
                st.setString(1, ip);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String lastSeen = rs.getString("date");
                        data.add(new Pair<>(name, lastSeen));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return data;
        });
    }
}