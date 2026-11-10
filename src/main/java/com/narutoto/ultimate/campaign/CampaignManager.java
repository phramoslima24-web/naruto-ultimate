package com.narutoto.ultimate.campaign;

import com.narutoto.ultimate.mission.MissionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/** Compatibility facade: campaign progress is now controlled by Kage missions. */
public final class CampaignManager {
    private CampaignManager() {}
    public static void onLivingDeath(LivingDeathEvent event) {
        MissionManager.onLivingDeath(event);
    }
    public static String status(ServerPlayer player) {
        return MissionManager.status(player);
    }
}
