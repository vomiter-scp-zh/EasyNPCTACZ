package com.vomiter.easynpc_tacz;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(EasyNPCTacz.MOD_ID)
public final class EasyNPCTacz {
    public static final String MOD_ID = "easynpc_tacz";

    public EasyNPCTacz() {
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
    }

    private void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        if (!entity.level().isClientSide && entity.tickCount % 10 == 0) {
            NPCGunBridge.tick(entity);
        }
    }
}
