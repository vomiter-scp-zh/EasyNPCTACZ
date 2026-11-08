package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import de.markusbordihn.easynpc.entity.easynpc.ai.goal.CustomMeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CustomMeleeAttackGoal.class, remap = false)
public abstract class EasyNPCMeleeAttackGoalMixin {
    @Shadow(remap = false) @Final private EasyNPC<?> easyNPC;

    @Inject(method = "isHoldingWeaponOfRangedAttack", at = @At("HEAD"), cancellable = true)
    private void easynpcTacz$honorMeleeFallback(CallbackInfoReturnable<Boolean> cir) {
        var mob = easyNPC.getPathfinderMob();
        if (mob != null && NPCGunBridge.isManaged(mob)) {
            cir.setReturnValue(!NPCGunBridge.canUseMelee(mob));
        }
    }
}
