package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import de.markusbordihn.easynpc.entity.easynpc.ai.goal.GunAttackGoal;
import de.markusbordihn.easynpc.entity.easynpc.ai.goal.StrafingRangedAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GunAttackGoal.class, remap = false)
public abstract class EasyNPCGunAttackGoalMixin extends StrafingRangedAttackGoal<EasyNPC<?>> {
    protected EasyNPCGunAttackGoalMixin(EasyNPC<?> npc, double speed, int interval, float radius) {
        super(npc, speed, interval, radius);
    }

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void easynpcTacz$delegateGunGoal(CallbackInfoReturnable<Boolean> cir) {
        if (NPCGunBridge.isManaged(pathfinderMob)) {
            cir.setReturnValue(false);
        }
    }
}
