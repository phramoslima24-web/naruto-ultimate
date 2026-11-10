package com.narutoto.ultimate;

import com.narutoto.ultimate.command.UltimateCommands;
import com.narutoto.ultimate.campaign.CampaignManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import com.narutoto.ultimate.data.PlayerProfile;
import com.narutoto.ultimate.integration.AddonDetector;
import com.narutoto.ultimate.mission.MissionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(NarutotoUltimate.MOD_ID)
public class NarutotoUltimate {
    public static final String MOD_ID = "narutoto_ultimate";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ServerLevel, Long> NEXT_RAID_TICK = new WeakHashMap<>();

    public NarutotoUltimate() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingHurt);
        LOGGER.info("Narutoto Ultimate RPG iniciado. Addons detectados: {}", AddonDetector.loadedSummary());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        UltimateCommands.register(event.getDispatcher());
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            PlayerProfile.ensure(event.getEntity());
            LOGGER.debug("Perfil Ultimate carregado para {}", event.getEntity().getGameProfile().getName());
        }
    }

    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level().isClientSide || !MissionManager.isKageNpc(event.getTarget())) return;
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            event.setCanceled(true);
            MissionManager.openBoard(player, event.getTarget());
        }
    }

    private void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Mob kage) || !MissionManager.isKageNpc(kage)) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != kage) {
            kage.setTarget(attacker);
            kage.getPersistentData().putBoolean("UltimateKageDefending", true);
        }
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 200 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) continue;
            long now = level.getGameTime();
            long next = NEXT_RAID_TICK.getOrDefault(level, now + 24000L);
            if (now < next) continue;
            // Rare raids: one check every 20 seconds, with a long randomized cooldown.
            NEXT_RAID_TICK.put(level, now + 24000L + level.random.nextInt(48000));
            var kages = level.getEntitiesOfClass(LivingEntity.class, new AABB(level.getSharedSpawnPos()).inflate(100000), MissionManager::isKageNpc);
            if (kages.isEmpty() || level.random.nextFloat() > 0.18F) continue;
            LivingEntity kage = kages.get(level.random.nextInt(kages.size()));
            var players = level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, kage.getBoundingBox().inflate(96));
            if (players.isEmpty()) continue;
            EntityType<?> raiderType = EntityType.byString("narutoto:missing_nin_wild").orElse(null);
            if (raiderType == null) raiderType = EntityType.byString("narutoto:missing_nin").orElse(null);
            if (raiderType == null) continue;
            int count = 5 + level.random.nextInt(2);
            List<Mob> raiders = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Entity spawned = raiderType.create(level);
                if (!(spawned instanceof Mob raider)) continue;
                double angle = (Math.PI * 2D * i / count) + level.random.nextDouble() * 0.5D;
                double radius = 18 + level.random.nextInt(9);
                double x = kage.getX() + Math.cos(angle) * radius;
                double z = kage.getZ() + Math.sin(angle) * radius;
                raider.moveTo(x, kage.getY(), z, level.random.nextFloat() * 360F, 0F);
                raider.setPersistenceRequired();
                raider.getPersistentData().putBoolean("UltimateRaidNukenin", true);
                raider.getPersistentData().putString("UltimateRaidVillage", kage.getPersistentData().getString("UltimateVillage"));
                raider.setTarget(players.get(level.random.nextInt(players.size())));
                level.addFreshEntity(raider);
                raiders.add(raider);
            }
            String defenderTag = switch (kage.getPersistentData().getString("UltimateVillage")) {
                case "KONOHA" -> "konoha_ninja";
                case "SUNA" -> "suna_ninja";
                case "KIRI" -> "kiri_ninja";
                case "IWA" -> "iwa_ninja";
                case "KUMO" -> "kumo_ninja";
                default -> "normal_ninja";
            };
            TagKey<EntityType<?>> villageNinjas = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge", defenderTag));
            var defenders = level.getEntitiesOfClass(Mob.class, kage.getBoundingBox().inflate(72), mob -> mob.getType().is(villageNinjas));
            for (Mob defender : defenders) {
                if (!raiders.isEmpty()) defender.setTarget(raiders.get(level.random.nextInt(raiders.size())));
            }
            for (var player : players) player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c§lALERTA DE INVASÃO! §fNukenins estão atacando a vila de " + kage.getPersistentData().getString("UltimateVillage") + "! Proteja o Kage e os moradores!"));
        }
    }

    private void onLivingDeath(LivingDeathEvent event) {
        CampaignManager.onLivingDeath(event);
    }

    private void onPlayerClone(PlayerEvent.Clone event) {
        PlayerProfile.copy(event.getOriginal(), event.getEntity());
    }
}
