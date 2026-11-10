package com.narutoto.ultimate.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Stores Ultimate RPG data in the player's persistent Forge NBT. */
public final class PlayerProfile {
    private static final String ROOT = "NarutotoUltimate";
    private static final int DATA_VERSION = 3;

    private PlayerProfile() {}

    public static CompoundTag get(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, CompoundTag.TAG_COMPOUND)) {
            CompoundTag fresh = new CompoundTag();
            fresh.putInt("DataVersion", DATA_VERSION);
            fresh.putString("Rank", "GENIN");
            fresh.putString("Village", "NONE");
            fresh.putString("Clan", "NONE");
            fresh.putString("Organization", "NONE");
            fresh.putInt("Experience", 0);
            fresh.putInt("Reputation", 0);
            fresh.putInt("MissionsCompleted", 0);
            fresh.putString("CampaignStage", "GENIN");
            fresh.putInt("CampaignKills", 0);
            fresh.putBoolean("CampaignBossDefeated", false);
            fresh.putBoolean("MissionActive", false);
            fresh.putBoolean("MissionReady", false);
            fresh.putString("MissionId", "");
            fresh.putString("MissionRank", "");
            fresh.putInt("MissionProgress", 0);
            fresh.putInt("MissionRequiredKills", 0);
            fresh.putBoolean("MissionRequiresBoss", false);
            fresh.putBoolean("MissionBossDone", false);
            fresh.putBoolean("KageAccess", false);
            fresh.putString("KageAccessVillage", "");
            fresh.putInt("Missions_GENIN", 0);
            fresh.putInt("Missions_CHUNIN", 0);
            fresh.putInt("Missions_JOUNIN", 0);
            fresh.putInt("Missions_ANBU", 0);
            fresh.putInt("Missions_KAGE", 0);
            persistent.put(ROOT, fresh);
        }
        CompoundTag data = persistent.getCompound(ROOT);
        migrateCampaignData(data);
        return data;
    }

    private static void migrateCampaignData(CompoundTag data) {
        if (!data.contains("CampaignStage", CompoundTag.TAG_STRING)) {
            String oldRank = data.getString("Rank");
            String stage = switch (oldRank) {
                case "CHUNIN" -> "CHUNIN";
                case "JOUNIN" -> "JOUNIN";
                case "ANBU" -> "ANBU";
                case "KAGE" -> "KAGE";
                default -> "GENIN";
            };
            data.putString("CampaignStage", stage);
        }
        if (!data.contains("CampaignKills")) data.putInt("CampaignKills", 0);
        if (!data.contains("CampaignBossDefeated")) data.putBoolean("CampaignBossDefeated", false);
        if (!data.contains("MissionActive")) data.putBoolean("MissionActive", false);
        if (!data.contains("MissionReady")) data.putBoolean("MissionReady", false);
        if (!data.contains("MissionId")) data.putString("MissionId", "");
        if (!data.contains("MissionRank")) data.putString("MissionRank", "");
        if (!data.contains("MissionProgress")) data.putInt("MissionProgress", 0);
        if (!data.contains("MissionRequiredKills")) data.putInt("MissionRequiredKills", 0);
        if (!data.contains("MissionRequiresBoss")) data.putBoolean("MissionRequiresBoss", false);
        if (!data.contains("MissionBossDone")) data.putBoolean("MissionBossDone", false);
        if (!data.contains("KageAccess")) data.putBoolean("KageAccess", false);
        if (!data.contains("KageAccessVillage")) data.putString("KageAccessVillage", "");
        for (String rank : new String[]{"GENIN", "CHUNIN", "JOUNIN", "ANBU", "KAGE"}) {
            if (!data.contains("Missions_" + rank)) data.putInt("Missions_" + rank, 0);
        }
        data.putInt("DataVersion", DATA_VERSION);
    }

    public static void ensure(Player player) { get(player); }

    public static void copy(Player original, Player replacement) {
        CompoundTag oldData = original.getPersistentData();
        if (oldData.contains(ROOT, CompoundTag.TAG_COMPOUND)) {
            replacement.getPersistentData().put(ROOT, oldData.getCompound(ROOT).copy());
        }
    }

    public static String getString(Player player, String key) { return get(player).getString(key); }
    public static int getInt(Player player, String key) { return get(player).getInt(key); }
    public static boolean getBoolean(Player player, String key) { return get(player).getBoolean(key); }
    public static void setString(Player player, String key, String value) { get(player).putString(key, value); }
    public static void setInt(Player player, String key, int value) { get(player).putInt(key, value); }
    public static void setBoolean(Player player, String key, boolean value) { get(player).putBoolean(key, value); }

    public static void addExperience(Player player, int amount) {
        if (amount > 0) {
            long result = (long) getInt(player, "Experience") + amount;
            setInt(player, "Experience", (int) Math.min(Integer.MAX_VALUE, result));
        }
    }

    public static void addReputation(Player player, int amount) {
        long result = (long) getInt(player, "Reputation") + amount;
        setInt(player, "Reputation", (int) Math.max(-100000, Math.min(100000, result)));
    }

    public static String summary(Player player) {
        CompoundTag data = get(player);
        return "§6=== Perfil Shinobi: §f" + player.getGameProfile().getName() + " §6===\n"
            + "§eRank: §f" + data.getString("Rank") + "\n"
            + "§eVila: §f" + data.getString("Village") + "\n"
            + "§eClã: §f" + data.getString("Clan") + "\n"
            + "§eOrganização: §f" + data.getString("Organization") + "\n"
            + "§eXP Ultimate: §f" + data.getInt("Experience") + "\n"
            + "§eReputação: §f" + data.getInt("Reputation") + "\n"
            + "§eMissões concluídas: §f" + data.getInt("MissionsCompleted");
    }
}
