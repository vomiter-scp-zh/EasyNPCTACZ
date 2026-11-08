package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
public abstract class TaczAmmoConsumptionMixin {
    @Shadow(remap = false) private LivingEntity shooter;
    @Shadow(remap = false) private ItemStack itemStack;
    @Shadow(remap = false) private AbstractGunItem abstractGunItem;
    @Shadow(remap = false) public abstract boolean isReloadingNeedConsumeAmmo();

    @Inject(method = "hasAmmoToConsume", at = @At("RETURN"), cancellable = true)
    private void easynpcTacz$checkVisibleAmmo(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && shooter instanceof Mob mob && !abstractGunItem.useDummyAmmo(itemStack)
                && NPCGunBridge.hasInventoryAmmo(mob, itemStack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "consumeAmmoFromPlayer", at = @At("HEAD"))
    private void easynpcTacz$feedOnlyDuringReload(int requested, CallbackInfoReturnable<Integer> cir) {
        if (shooter instanceof Mob mob && !abstractGunItem.useInventoryAmmo(itemStack)
                && !abstractGunItem.useDummyAmmo(itemStack) && isReloadingNeedConsumeAmmo()
                && IGunOperator.fromLivingEntity(mob).getDataHolder().reloadStateType.isReloading()) {
            NPCGunBridge.stockAmmoForReload(mob, itemStack, requested);
        }
    }

    @Inject(method = "consumeAmmoFromPlayer", at = @At("RETURN"), cancellable = true)
    private void easynpcTacz$consumeInventoryFedRounds(int requested, CallbackInfoReturnable<Integer> cir) {
        if (shooter instanceof Mob mob && abstractGunItem.useInventoryAmmo(itemStack)
                && !abstractGunItem.useDummyAmmo(itemStack) && cir.getReturnValue() < requested) {
            int consumed = NPCGunBridge.consumeInventoryAmmo(mob, itemStack, requested - cir.getReturnValue());
            if (consumed > 0) {
                cir.setReturnValue(cir.getReturnValue() + consumed);
            }
        }
    }
}
