package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import de.markusbordihn.easynpc.entity.easynpc.handlers.AttackHandler;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttackHandler.class, remap = false)
public abstract class EasyNPCAttackHandlerMixin {
    @Inject(method = "performGunAttack", at = @At("HEAD"), cancellable = true)
    private static void easynpcTacz$preventNativeProjectile(LivingEntity shooter, LivingEntity target,
                                                          float damage, CallbackInfo ci) {
        if (NPCGunBridge.isManaged(shooter)) {
            ci.cancel();
        }
    }
}
