package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractGunItem.class, remap = false)
public abstract class TaczAmmoAvailabilityMixin {
    @Inject(method = "canReload", at = @At("RETURN"), cancellable = true)
    private void easynpcTacz$checkReloadInventory(LivingEntity shooter, ItemStack gun,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && shooter instanceof Mob mob && gun == mob.getMainHandItem()
                && NPCGunBridge.canReloadFromInventory(mob)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasInventoryAmmo", at = @At("RETURN"), cancellable = true)
    private void easynpcTacz$checkInventoryFedAmmo(LivingEntity shooter, ItemStack gun, boolean needCheckAmmo,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && shooter instanceof Mob mob
                && gun.getItem() instanceof AbstractGunItem item && item.useInventoryAmmo(gun)
                && !item.useDummyAmmo(gun) && NPCGunBridge.hasInventoryAmmo(mob, gun)) {
            cir.setReturnValue(true);
        }
    }
}
