package com.narutoto.ultimate;

import com.narutoto.ultimate.command.UltimateCommands;
import com.narutoto.ultimate.data.PlayerProfile;
import com.narutoto.ultimate.integration.AddonDetector;
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

    public NarutotoUltimate() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerClone);
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

    private void onPlayerClone(PlayerEvent.Clone event) {
        PlayerProfile.copy(event.getOriginal(), event.getEntity());
    }
}
