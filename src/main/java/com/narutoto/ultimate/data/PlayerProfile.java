package com.narutoto.ultimate.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Stores Ultimate RPG data in the player's persistent Forge NBT. */
public final class PlayerProfile {
    private static final String ROOT = "NarutotoUltimate";
    private static final int DATA_VERSION = 1;

    private PlayerProfile() {}

    public static CompoundTag get(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, CompoundTag.TAG_COMPOUND)) {
            CompoundTag fresh = new CompoundTag();
            fresh.putInt("DataVersion", DATA_VERSION);
            fresh.putString("Rank", "ACADEMY");
            fresh.putString("Village", "NONE");
            fresh.putString("Clan", "NONE");
            fresh.putString("Organization", "NONE");
            fresh.putInt("Experience", 0);
            fresh.putInt("Reputation", 0);
            fresh.putInt("MissionsCompleted", 0);
            persistent.put(ROOT, fresh);
        }
        return persistent.getCompound(ROOT);
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
    public static void setString(Player player, String key, String value) { get(player).putString(key, value); }
    public static void setInt(Player player, String key, int value) { get(player).putInt(key, value); }

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
            + "§eXP: §f" + data.getInt("Experience") + "\n"
            + "§eReputação: §f" + data.getInt("Reputation") + "\n"
            + "§eMissões concluídas: §f" + data.getInt("MissionsCompleted");
    }
}
