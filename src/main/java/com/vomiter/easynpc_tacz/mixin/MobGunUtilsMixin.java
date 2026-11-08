package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.mobstacz.common.entity.MobGunUtils;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobGunUtils.class, remap = false)
public abstract class MobGunUtilsMixin {
    @Inject(method = "giveInitialAmmoStack", at = @At("HEAD"), cancellable = true)
    private static void easynpcTacz$disableFreeAmmo(Mob mob, ItemStack gun,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof EasyNPC<?>) {
            cir.setReturnValue(false);
        }
    }

}
