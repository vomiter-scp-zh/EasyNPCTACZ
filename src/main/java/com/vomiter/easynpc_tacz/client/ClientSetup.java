package com.vomiter.easynpc_tacz.client;

import com.vomiter.easynpc_tacz.EasyNPCTacz;
import com.vomiter.easynpc_tacz.config.NPCConfigRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EasyNPCTacz.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (NPCConfigRegistry.GUN_CONFIG.isPresent()) {
                MenuScreens.register(NPCConfigRegistry.GUN_CONFIG.get(), NPCGunConfigScreen::new);
            }
        });
    }
}
