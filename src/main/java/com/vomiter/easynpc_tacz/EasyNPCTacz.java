package com.vomiter.easynpc_tacz;

import com.vomiter.easynpc_tacz.config.NPCConfigRegistry;
import com.vomiter.easynpc_tacz.config.NPCConfigStickItem;
import com.vomiter.easynpc_tacz.config.NPCGunSettings;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(EasyNPCTacz.MOD_ID)
public final class EasyNPCTacz {
    public static final String MOD_ID = "easynpc_tacz";

    public EasyNPCTacz() {
        NPCConfigRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onNPCInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onNPCInteractSpecific);
    }

    private void onNPCInteract(PlayerInteractEvent.EntityInteract event) {
        if (openConfig(event, event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        }
    }

    private void onNPCInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (openConfig(event, event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        }
    }

    private boolean openConfig(PlayerInteractEvent event, Entity target) {
        if (!(target instanceof Mob mob) || !(target instanceof EasyNPC<?>)
                || !NPCConfigRegistry.CONFIG_STICK.isPresent()
                || !event.getItemStack().is(NPCConfigRegistry.CONFIG_STICK.get())) return false;
        if (event.getEntity() instanceof ServerPlayer player) NPCConfigStickItem.open(player, mob);
        return true;
    }

    private void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide) {
            if (entity instanceof EasyNPC<?> && entity instanceof NPCGunSettings settings) {
                settings.enpcTacz$applySyncedOverrides();
            }
            return;
        }
        if (entity.tickCount % 10 == 0) {
            NPCGunBridge.tick(entity);
        }
    }
}
