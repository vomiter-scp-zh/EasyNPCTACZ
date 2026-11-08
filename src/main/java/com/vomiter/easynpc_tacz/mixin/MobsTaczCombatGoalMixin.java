package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import com.vomiter.mobstacz.common.entity.ai.CrazyShooterAimGoal;
import com.vomiter.mobstacz.common.entity.ai.CrazyShooterReloadGoal;
import com.vomiter.mobstacz.common.entity.ai.CrazyShooterShootingGoal;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {CrazyShooterShootingGoal.class, CrazyShooterAimGoal.class,
        CrazyShooterReloadGoal.class}, remap = false)
public abstract class MobsTaczCombatGoalMixin {
    @Shadow(remap = false) @Final private Mob shooter;

    // Vanilla Goal overrides need SRG remapping even though the target classes belong to a mod.
    @Inject(method = {"canUse", "canContinueToUse"}, at = @At("HEAD"),
            cancellable = true, remap = true)
    private void easynpcTacz$requireGunAttackObjective(CallbackInfoReturnable<Boolean> cir) {
        if (!NPCGunBridge.isGunAttackEnabled(shooter)
                || (shooter instanceof EasyNPC<?> && !NPCGunBridge.isManaged(shooter))) {
            cir.setReturnValue(false);
        }
    }
}
